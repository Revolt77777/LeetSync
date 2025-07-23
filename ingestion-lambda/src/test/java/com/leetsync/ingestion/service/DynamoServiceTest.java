package com.leetsync.ingestion.service;

import com.leetsync.ingestion.model.AcSubmission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DynamoServiceTest {

    private DynamoDbEnhancedClient mockEnhancedClient;
    private DynamoDbTable<AcSubmission> mockTable;
    private DynamoService service;

    @BeforeEach
    void setUp() {
        mockEnhancedClient = mock(DynamoDbEnhancedClient.class);
        mockTable = mock(DynamoDbTable.class);
        when(mockEnhancedClient.<AcSubmission>table(eq("Submissions"), any())).thenReturn(mockTable);
        service = new DynamoService(mockEnhancedClient, "Submissions");
    }

    @Test
    void storeIfNew_insertsWhenNoDuplicate() {
        // Enhanced client doesn't return anything for successful putItem
        doNothing().when(mockTable).putItem(any(PutItemEnhancedRequest.class));

        AcSubmission sub = new AcSubmission(
                "testuser", "Two Sum", "two-sum", Instant.now().getEpochSecond(), 12, 19.1);

        assertTrue(service.storeIfNew(sub));

        // Verify the request actually hit Enhanced client table
        verify(mockTable).putItem(any(PutItemEnhancedRequest.class));
    }

    @Test
    void storeIfNew_returnsFalseOnDuplicate() {
        doThrow(ConditionalCheckFailedException.builder().build())
                .when(mockTable).putItem(any(PutItemEnhancedRequest.class));

        AcSubmission sub = new AcSubmission(
                "testuser", "Test", "test", Instant.now().getEpochSecond(), 5, 20.5);

        assertFalse(service.storeIfNew(sub));
        verify(mockTable).putItem(any(PutItemEnhancedRequest.class));
    }
}
