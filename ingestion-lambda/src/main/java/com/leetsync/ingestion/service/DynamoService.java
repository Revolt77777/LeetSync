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
        String username = submission.getUsername();
        String titleSlug = submission.getTitleSlug();
        long timestamp = submission.getTimestamp();
        log.info("Attempting to store submission for user: {} with titleSlug: {} and timestamp: {}", username, titleSlug, timestamp);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("username", AttributeValue.fromS(username));
        item.put("timestamp", AttributeValue.fromN(Long.toString(timestamp)));
        item.put("titleSlug", AttributeValue.fromS(titleSlug));
        item.put("title", AttributeValue.fromS(submission.getTitle()));
        item.put("runtimeMs", AttributeValue.fromN(Integer.toString(submission.getRuntimeMs())));
        item.put("memoryMb", AttributeValue.fromN(Double.toString(submission.getMemoryMb())));

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(username) AND attribute_not_exists(#ts)")
                .expressionAttributeNames(Map.of("#ts", "timestamp"))
                .build();

        try {
            dynamoDbClient.putItem(putRequest);
            log.info("Stored new submission for user: {} with titleSlug: {} and timestamp: {}", username, titleSlug, timestamp);
            return true;
        } catch (ConditionalCheckFailedException e) {
            log.info("Submission for user {} with titleSlug {} and timestamp {} already exists. Skipping.", username, titleSlug, timestamp);
            return false;
        } catch (DynamoDbException e) {
            log.error("DynamoDB error while storing submission for user {} with titleSlug {} and timestamp {}: {}", username, titleSlug, timestamp, e.getMessage());
            return false;
        }
    }
}
