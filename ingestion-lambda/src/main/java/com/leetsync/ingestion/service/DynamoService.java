package com.leetsync.ingestion.service;

import com.leetsync.shared.model.AcSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class DynamoService {

    private static final Logger log = LoggerFactory.getLogger(DynamoService.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoService(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public boolean storeIfNew(AcSubmission submission) {
        String idStr = Long.toString(submission.getId());
        log.info("Attempting to store submission with ID: {}", idStr);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.fromN(idStr));
        item.put("timestamp", AttributeValue.fromN(Long.toString(submission.getTimestamp())));
        item.put("title", AttributeValue.fromS(submission.getTitle()));
        item.put("titleSlug", AttributeValue.fromS(submission.getTitleSlug()));

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(id)")
                .build();

        try {
            dynamoDbClient.putItem(putRequest);
            log.info("Stored new submission with ID: {}", idStr);
            return true;
        } catch (ConditionalCheckFailedException e) {
            log.info("Submission with ID {} already exists. Skipping.", idStr);
            return false;
        } catch (DynamoDbException e) {
            log.error("DynamoDB error while storing ID {}: {}", idStr, e.getMessage());
            return false;
        }
    }
}
