package com.leetsync.etl.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.leetsync.etl.model.AcSubmissionRecord;
import com.leetsync.etl.service.ParquetFileWriter;
import com.leetsync.etl.service.ProblemService;
import com.leetsync.etl.service.S3Service;
import com.leetsync.shared.model.AcSubmission;
import com.leetsync.shared.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreamHandler implements RequestHandler<DynamodbEvent, String> {
    
    private static final Logger log = LoggerFactory.getLogger(StreamHandler.class);
    
    private final ProblemService problemService;
    private final ParquetFileWriter parquetWriter;
    private final S3Service s3Service;

    public StreamHandler() {
        DynamoDbClient dynamoClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoClient)
                .build();
        
        String problemsTableName = System.getenv("PROBLEMS_TABLE_NAME");
        String bucketName = System.getenv("DEST_BUCKET");
        
        this.problemService = new ProblemService(enhancedClient, problemsTableName);
        this.parquetWriter = new ParquetFileWriter();
        this.s3Service = new S3Service(S3Client.create(), bucketName);
    }

    @Override
    public String handleRequest(DynamodbEvent event, Context context) {
        log.info("Processing {} DynamoDB stream records", event.getRecords().size());
        
        List<AcSubmissionRecord> records = new ArrayList<>();
        
        for (DynamodbEvent.DynamodbStreamRecord streamRecord : event.getRecords()) {
            try {
                // Only process INSERT and MODIFY events
                if (!"INSERT".equals(streamRecord.getEventName()) && 
                    !"MODIFY".equals(streamRecord.getEventName())) {
                    continue;
                }
                
                AcSubmission submission = parseAcSubmission(streamRecord.getDynamodb().getNewImage());
                if (submission == null) {
                    log.warn("Failed to parse AcSubmission from stream record");
                    continue;
                }
                
                // Enrich with problem data
                Problem problem = problemService.getProblem(submission.getTitleSlug());
                
                AcSubmissionRecord record = createEnrichedRecord(submission, problem);
                records.add(record);
                
            } catch (Exception e) {
                log.error("Error processing stream record: {}", e.getMessage());
                // Continue processing other records
            }
        }
        
        if (records.isEmpty()) {
            log.info("No valid records to process");
            return "No records processed";
        }
        
        try {
            // Write to Parquet file
            String tempFilePath = parquetWriter.writeToTempFile(records);
            if (tempFilePath != null) {
                // Upload to S3
                s3Service.uploadParquetFile(tempFilePath, records.getFirst().getTimestamp());
            }
            
            return String.format("Successfully processed %d records", records.size());
            
        } catch (Exception e) {
            log.error("Error writing/uploading Parquet file: {}", e.getMessage());
            throw new RuntimeException("Failed to process stream records", e);
        }
    }
    
    private AcSubmission parseAcSubmission(Map<String, AttributeValue> item) {
        try {
            String username = item.get("username").getS();
            String title = item.get("title").getS();
            String titleSlug = item.get("titleSlug").getS();
            long timestamp = Long.parseLong(item.get("timestamp").getN());
            
            return new AcSubmission(username, title, titleSlug, timestamp);
            
        } catch (Exception e) {
            log.error("Error parsing AcSubmission: {}", e.getMessage());
            return null;
        }
    }
    
    private AcSubmissionRecord createEnrichedRecord(AcSubmission submission, Problem problem) {
        AcSubmissionRecord record = new AcSubmissionRecord();
        
        // Copy submission data
        record.setUsername(submission.getUsername());
        record.setTitle(submission.getTitle());
        record.setTitleSlug(submission.getTitleSlug());
        record.setTimestamp(submission.getTimestamp());
        
        // Enrich with problem data (if available)
        if (problem != null) {
            record.setDifficulty(problem.getDifficulty());
            record.setAcRate(problem.getAcRate());
            record.setTotalAccepted(problem.getTotalAccepted());
            record.setTotalSubmitted(problem.getTotalSubmitted());
            
            // Convert topic tags to string array
            if (problem.getTopicTags() != null) {
                String[] tags = problem.getTopicTags().stream()
                        .map(Problem.TopicTag::getName)
                        .toArray(String[]::new);
                record.setTags(tags);
            }
        }
        
        return record;
    }
}