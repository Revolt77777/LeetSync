package com.revolt7.leetsync.service;

import com.revolt7.leetsync.model.AcSubmission;
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
    private DynamoService  service;

    @BeforeEach
    void setUp() {
        mockDynamo = mock(DynamoDbClient.class);          // create the mock
        service    = new DynamoService(mockDynamo);       // inject it via ctor

        // supply the table name that Spring would @Value-inject
        org.springframework.test.util.ReflectionTestUtils
                .setField(service, "tableName", "Submissions");
    }

    @Test
    void storeIfNew_insertsWhenNoDuplicate() {
        when(mockDynamo.putItem(any(PutItemRequest.class)))
                .thenReturn(PutItemResponse.builder().build());

        AcSubmission sub = new AcSubmission(
                123L, "Two Sum", "two-sum", Instant.now().getEpochSecond());

        assertTrue(service.storeIfNew(sub));

        // Verify the request actually hit DynamoDbClient
        ArgumentCaptor<PutItemRequest> cap = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(mockDynamo).putItem(cap.capture());
        assertEquals("attribute_not_exists(id)", cap.getValue().conditionExpression());
    }

    @Test
    void storeIfNew_returnsFalseOnDuplicate() {
        when(mockDynamo.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        AcSubmission sub = new AcSubmission(
                123L, null, null, Instant.now().getEpochSecond());

        assertFalse(service.storeIfNew(sub));
        verify(mockDynamo).putItem(any(PutItemRequest.class));
    }
}
