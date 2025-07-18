package com.leetsync.ingestion.service;

import com.leetsync.shared.model.AcSubmission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamoServiceTest {

    private DynamoDbClient mockDynamo;
    private DynamoService service;

    @BeforeEach
    void setUp() {
        mockDynamo = mock(DynamoDbClient.class);
        service = new DynamoService(mockDynamo, "Submissions");  // pass table name directly
    }

    @Test
    void storeIfNew_insertsWhenNoDuplicate() {
        when(mockDynamo.putItem(any(PutItemRequest.class)))
                .thenReturn(PutItemResponse.builder().build());

        AcSubmission sub = new AcSubmission(
                "testuser", "Two Sum", "two-sum", Instant.now().getEpochSecond());

        assertTrue(service.storeIfNew(sub));

        // Verify the request actually hit DynamoDbClient
        ArgumentCaptor<PutItemRequest> cap = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(mockDynamo).putItem(cap.capture());
        assertEquals("attribute_not_exists(username) AND attribute_not_exists(#ts)", cap.getValue().conditionExpression());
    }

    @Test
    void storeIfNew_returnsFalseOnDuplicate() {
        when(mockDynamo.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        AcSubmission sub = new AcSubmission(
                "testuser", "Test", "test", Instant.now().getEpochSecond());

        assertFalse(service.storeIfNew(sub));
        verify(mockDynamo).putItem(any(PutItemRequest.class));
    }
}
