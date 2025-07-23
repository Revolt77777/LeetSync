package com.leetsync.api.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * User model for API server with DynamoDB annotations
 * Optimized for API service needs
 */
@DynamoDbBean
public class User {
    private String username;
    private long createdAt;
    private long lastSync;

    public User() {}

    public User(String username, long createdAt, long lastSync) {
        this.username = username;
        this.createdAt = createdAt;
        this.lastSync = lastSync;
    }

    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDbAttribute("createdAt")
    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbAttribute("lastSync")
    public long getLastSync() {
        return lastSync;
    }
    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", lastSync=" + lastSync +
                '}';
    }
}