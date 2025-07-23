package com.leetsync.api.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Streak stats model for API server with DynamoDB annotations
 * Optimized for API service needs
 */
@DynamoDbBean
public class StreakStats {
    
    private String username;
    private String statType;
    private int currentStreak;
    private int longestStreak;
    
    public StreakStats() {}
    
    @DynamoDbPartitionKey
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    @DynamoDbSortKey
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    
    @DynamoDbAttribute("currentStreak")
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    @DynamoDbAttribute("longestStreak")
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
}