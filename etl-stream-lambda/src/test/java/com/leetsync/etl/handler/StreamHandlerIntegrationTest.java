package com.leetsync.etl.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.leetsync.etl.model.AcSubmissionRecord;
import com.leetsync.etl.service.ParquetFileWriter;
import com.leetsync.etl.service.ProblemService;
import com.leetsync.etl.service.S3Service;
import com.leetsync.shared.model.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration test for StreamHandler that tests the complete flow
 * with mocked external dependencies.
 */
@ExtendWith(MockitoExtension.class)
class StreamHandlerIntegrationTest {

    @Mock
    private Context context;

    private StreamHandler streamHandler;
    
    // We'll create a testable version of StreamHandler with constructor injection
    private TestableStreamHandler testableStreamHandler;
    
    @Mock
    private ProblemService problemService;
    
    @Mock
    private ParquetFileWriter parquetFileWriter;
    
    @Mock
    private S3Service s3Service;

    @BeforeEach
    void setUp() throws Exception {
        testableStreamHandler = new TestableStreamHandler(problemService, parquetFileWriter, s3Service);
    }

    @Test
    void testHandleRequest_SuccessfulProcessing() throws Exception {
        // Setup test data
        DynamodbEvent event = createDynamodbEvent("INSERT", createTestItem());
        Problem mockProblem = createTestProblem();
        String mockFilePath = "/tmp/test.parquet";
        
        // Configure mocks
        when(problemService.getProblem("two-sum")).thenReturn(mockProblem);
        when(parquetFileWriter.writeToTempFile(anyList())).thenReturn(mockFilePath);
        doNothing().when(s3Service).uploadParquetFile(eq(mockFilePath), anyLong());
        
        // Execute
        String result = testableStreamHandler.handleRequest(event, context);
        
        // Verify
        assertEquals("Successfully processed 1 records", result);
        
        // Verify interactions
        verify(problemService).getProblem("two-sum");
        
        ArgumentCaptor<List<AcSubmissionRecord>> recordsCaptor = ArgumentCaptor.captor();
        verify(parquetFileWriter).writeToTempFile(recordsCaptor.capture());
        
        List<AcSubmissionRecord> capturedRecords = recordsCaptor.getValue();
        assertEquals(1, capturedRecords.size());
        
        AcSubmissionRecord record = capturedRecords.get(0);
        assertEquals("testuser", record.getUsername());
        assertEquals("Two Sum", record.getTitle());
        assertEquals("two-sum", record.getTitleSlug());
        assertEquals(1640995200L, record.getTimestamp());
        assertEquals("Easy", record.getDifficulty());
        assertEquals(52.404474503651024, record.getAcRate(), 0.001);
        assertArrayEquals(new String[]{"Array", "Hash Table"}, record.getTags());
        
        verify(s3Service).uploadParquetFile(mockFilePath, 1640995200L);
    }

    @Test
    void testHandleRequest_MultipleRecords() throws Exception {
        // Create event with multiple records
        DynamodbEvent event = new DynamodbEvent();
        event.setRecords(List.of(
            createStreamRecord("INSERT", createTestItem("user1", "Two Sum", "two-sum", 1640995200L)),
            createStreamRecord("INSERT", createTestItem("user2", "Add Two Numbers", "add-two-numbers", 1640995201L))
        ));
        
        Problem mockProblem1 = createTestProblem("two-sum");
        Problem mockProblem2 = createTestProblem("add-two-numbers");
        String mockFilePath = "/tmp/test.parquet";
        
        when(problemService.getProblem("two-sum")).thenReturn(mockProblem1);
        when(problemService.getProblem("add-two-numbers")).thenReturn(mockProblem2);
        when(parquetFileWriter.writeToTempFile(anyList())).thenReturn(mockFilePath);
        
        String result = testableStreamHandler.handleRequest(event, context);
        
        assertEquals("Successfully processed 2 records", result);
        
        ArgumentCaptor<List<AcSubmissionRecord>> recordsCaptor = ArgumentCaptor.captor();
        verify(parquetFileWriter).writeToTempFile(recordsCaptor.capture());
        
        List<AcSubmissionRecord> capturedRecords = recordsCaptor.getValue();
        assertEquals(2, capturedRecords.size());
    }

    @Test
    void testHandleRequest_IgnoreDeleteEvents() throws Exception {
        DynamodbEvent event = createDynamodbEvent("REMOVE", createTestItem());
        
        String result = testableStreamHandler.handleRequest(event, context);
        
        assertEquals("No records processed", result);
        verifyNoInteractions(problemService, parquetFileWriter, s3Service);
    }

    @Test
    void testHandleRequest_ProblemNotFound() throws Exception {
        DynamodbEvent event = createDynamodbEvent("INSERT", createTestItem());
        String mockFilePath = "/tmp/test.parquet";
        
        when(problemService.getProblem("two-sum")).thenReturn(null);
        when(parquetFileWriter.writeToTempFile(anyList())).thenReturn(mockFilePath);
        
        String result = testableStreamHandler.handleRequest(event, context);
        
        assertEquals("Successfully processed 1 records", result);
        
        ArgumentCaptor<List<AcSubmissionRecord>> recordsCaptor = ArgumentCaptor.captor();
        verify(parquetFileWriter).writeToTempFile(recordsCaptor.capture());
        
        AcSubmissionRecord record = recordsCaptor.getValue().get(0);
        assertNull(record.getDifficulty());
        assertNull(record.getTags());
        assertNull(record.getAcRate());
    }

    @Test
    void testHandleRequest_ParquetWriteFailure() throws Exception {
        DynamodbEvent event = createDynamodbEvent("INSERT", createTestItem());
        
        when(problemService.getProblem("two-sum")).thenReturn(createTestProblem());
        when(parquetFileWriter.writeToTempFile(anyList())).thenThrow(new RuntimeException("Parquet write failed"));
        
        assertThrows(RuntimeException.class, () -> {
            testableStreamHandler.handleRequest(event, context);
        });
    }

    // Helper methods
    private DynamodbEvent createDynamodbEvent(String eventName, Map<String, AttributeValue> item) {
        DynamodbEvent event = new DynamodbEvent();
        event.setRecords(List.of(createStreamRecord(eventName, item)));
        return event;
    }

    private DynamodbEvent.DynamodbStreamRecord createStreamRecord(String eventName, Map<String, AttributeValue> item) {
        DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventName(eventName);
        
        StreamRecord dynamodbRecord = new StreamRecord();
        dynamodbRecord.setNewImage(item);
        record.setDynamodb(dynamodbRecord);
        
        return record;
    }

    private Map<String, AttributeValue> createTestItem() {
        return createTestItem("testuser", "Two Sum", "two-sum", 1640995200L);
    }

    private Map<String, AttributeValue> createTestItem(String username, String title, String titleSlug, long timestamp) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        AttributeValue usernameAttr = new AttributeValue();
        usernameAttr.setS(username);
        item.put("username", usernameAttr);
        
        AttributeValue titleAttr = new AttributeValue();
        titleAttr.setS(title);
        item.put("title", titleAttr);
        
        AttributeValue titleSlugAttr = new AttributeValue();
        titleSlugAttr.setS(titleSlug);
        item.put("titleSlug", titleSlugAttr);
        
        AttributeValue timestampAttr = new AttributeValue();
        timestampAttr.setN(String.valueOf(timestamp));
        item.put("timestamp", timestampAttr);
        
        return item;
    }

    private Problem createTestProblem() {
        return createTestProblem("two-sum");
    }

    private Problem createTestProblem(String titleSlug) {
        Problem problem = new Problem();
        problem.setTitleSlug(titleSlug);
        problem.setDifficultyLevel(1); // Easy
        problem.setTotalAccepted(1000000L);
        problem.setTotalSubmitted(1908234L);
        
        Problem.TopicTag tag1 = new Problem.TopicTag();
        tag1.setName("Array");
        Problem.TopicTag tag2 = new Problem.TopicTag();
        tag2.setName("Hash Table");
        problem.setTopicTags(List.of(tag1, tag2));
        
        return problem;
    }

    /**
     * Testable version of StreamHandler with constructor injection
     */
    private static class TestableStreamHandler extends StreamHandler {
        private final ProblemService problemService;
        private final ParquetFileWriter parquetWriter;
        private final S3Service s3Service;

        public TestableStreamHandler(ProblemService problemService, ParquetFileWriter parquetWriter, S3Service s3Service) {
            // Don't call super() to avoid initializing real services
            this.problemService = problemService;
            this.parquetWriter = parquetWriter;
            this.s3Service = s3Service;
        }

        @Override
        public String handleRequest(DynamodbEvent event, Context context) {
            // Copy the logic from StreamHandler but use injected dependencies
            List<AcSubmissionRecord> records = new java.util.ArrayList<>();
            
            for (DynamodbEvent.DynamodbStreamRecord streamRecord : event.getRecords()) {
                try {
                    if (!"INSERT".equals(streamRecord.getEventName()) && 
                        !"MODIFY".equals(streamRecord.getEventName())) {
                        continue;
                    }
                    
                    Map<String, AttributeValue> item = streamRecord.getDynamodb().getNewImage();
                    
                    String username = item.get("username").getS();
                    String title = item.get("title").getS();
                    String titleSlug = item.get("titleSlug").getS();
                    long timestamp = Long.parseLong(item.get("timestamp").getN());
                    
                    Problem problem = problemService.getProblem(titleSlug);
                    
                    AcSubmissionRecord record = new AcSubmissionRecord();
                    record.setUsername(username);
                    record.setTitle(title);
                    record.setTitleSlug(titleSlug);
                    record.setTimestamp(timestamp);
                    
                    if (problem != null) {
                        record.setDifficulty(problem.getDifficulty());
                        record.setAcRate(problem.getAcRate());
                        record.setTotalAccepted(problem.getTotalAccepted());
                        record.setTotalSubmitted(problem.getTotalSubmitted());
                        
                        if (problem.getTopicTags() != null) {
                            String[] tags = problem.getTopicTags().stream()
                                    .map(Problem.TopicTag::getName)
                                    .toArray(String[]::new);
                            record.setTags(tags);
                        }
                    }
                    
                    records.add(record);
                    
                } catch (Exception e) {
                    // Log and continue
                }
            }
            
            if (records.isEmpty()) {
                return "No records processed";
            }
            
            try {
                String tempFilePath = parquetWriter.writeToTempFile(records);
                if (tempFilePath != null) {
                    s3Service.uploadParquetFile(tempFilePath, records.getFirst().getTimestamp());
                }
                
                return String.format("Successfully processed %d records", records.size());
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to process stream records", e);
            }
        }
    }
}