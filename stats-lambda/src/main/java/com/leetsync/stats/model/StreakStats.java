package com.leetsync.stats.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Streak tracking - stored as STREAKS
 * Contains current streak and longest streak information
 */
@DynamoDbBean
public class StreakStats {
    
    // DynamoDB keys
    private String username;
    private String statType;
    
    // Streak attributes
    private int currentStreak;
    private int longestStreak;
    
    public StreakStats() {}
    
    public StreakStats(String username, int currentStreak, int longestStreak) {
        this.username = username;
        this.statType = "STREAKS";
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }
    
    
    // DynamoDB key getters/setters
    @DynamoDbPartitionKey
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    @DynamoDbSortKey
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    
    // Stats attribute getters/setters
    @DynamoDbAttribute("currentStreak")
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    @DynamoDbAttribute("longestStreak")
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
}