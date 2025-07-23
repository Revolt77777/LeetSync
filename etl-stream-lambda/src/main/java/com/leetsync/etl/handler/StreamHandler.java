package com.leetsync.etl.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.leetsync.etl.model.AcSubmissionRecord;
import com.leetsync.etl.service.ParquetFileWriter;
import com.leetsync.etl.service.ProblemService;
import com.leetsync.etl.service.S3Service;
import com.leetsync.etl.model.Problem;
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
                
                AcSubmissionRecord record = parseAndEnrichRecord(streamRecord.getDynamodb().getNewImage());
                if (record == null) {
                    log.warn("Failed to parse and enrich record from stream");
                    continue;
                }
                
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
    
    private AcSubmissionRecord parseAndEnrichRecord(Map<String, AttributeValue> item) {
        try {
            // Parse submission data directly into record
            AcSubmissionRecord record = new AcSubmissionRecord();
            record.setUsername(item.get("username").getS());
            record.setTitle(item.get("title").getS());
            record.setTitleSlug(item.get("titleSlug").getS());
            record.setTimestamp(Long.parseLong(item.get("timestamp").getN()));
            record.setRuntimeMs(Integer.parseInt(item.get("runtimeMs").getN()));
            record.setMemoryMb(Double.parseDouble(item.get("memoryMb").getN()));
            
            // Enrich with problem data
            Problem problem = problemService.getProblem(record.getTitleSlug());
            if (problem != null) {
                record.setDifficultyLevel(problem.getDifficultyLevel());
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
            
        } catch (Exception e) {
            log.error("Error parsing and enriching record: {}", e.getMessage());
            return null;
        }
    }
}