package com.leetsync.ingestion.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * AcSubmission model for ingestion service with DynamoDB annotations
 * This service owns this model and can evolve it independently
 */
@DynamoDbBean
public class AcSubmission {
    private String username;
    private String title;
    private String titleSlug;
    private long timestamp;
    private Integer runtimeMs;
    private Double memoryMb;

    public AcSubmission() {}

    public AcSubmission(String username, String title, String titleSlug, long timestamp, Integer runtimeMs, Double memoryMb) {
        this.username = username;
        this.title = title;
        this.titleSlug = titleSlug;
        this.timestamp = timestamp;
        this.runtimeMs = runtimeMs;
        this.memoryMb = memoryMb;
    }

    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDbSortKey
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDbAttribute("title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDbAttribute("titleSlug")
    public String getTitleSlug() {
        return titleSlug;
    }
    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    @DynamoDbAttribute("runtimeMs")
    public Integer getRuntimeMs() {
        return runtimeMs;
    }
    public void setRuntimeMs(Integer runtimeMs) {
        this.runtimeMs = runtimeMs;
    }

    @DynamoDbAttribute("memoryMb")
    public Double getMemoryMb() {
        return memoryMb;
    }
    public void setMemoryMb(Double memoryMb) {
        this.memoryMb = memoryMb;
    }

    @Override
    public String toString() {
        return "AcSubmission{" +
                "username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", titleSlug='" + titleSlug + '\'' +
                ", timestamp=" + timestamp +
                ", runtimeMs=" + runtimeMs +
                ", memoryMb=" + memoryMb +
                '}';
    }
}