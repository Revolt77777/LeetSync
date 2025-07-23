package com.leetsync.ingestion.service;

// No User model needed - this service only reads usernames and updates timestamps
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserService {
    
    private final DynamoDbClient dynamoClient;
    private final String tableName;

    public UserService(DynamoDbClient dynamoClient, String tableName) {
        this.dynamoClient = dynamoClient;
        this.tableName = tableName;
    }

    public List<String> getAllUsernames() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse response = dynamoClient.scan(scanRequest);
        
        return response.items().stream()
                .map(item -> item.get("username").s())
                .collect(Collectors.toList());
    }

    public void updateLastSync(String username, long timestamp) {
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("username", AttributeValue.builder().s(username).build()))
                .updateExpression("SET lastSync = :timestamp")
                .expressionAttributeValues(Map.of(
                        ":timestamp", AttributeValue.builder().n(String.valueOf(timestamp)).build()
                ))
                .build();

        dynamoClient.updateItem(updateRequest);
    }
}