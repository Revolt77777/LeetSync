package com.leetsync.stats.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import java.util.Map;

/**
 * 7-day rolling stats - stored as SEVEN_DAY
 * Contains 7-day average and tag averages
 */
@DynamoDbBean
public class SevenDayStats {
    
    // DynamoDB keys
    private String username;
    private String statType;
    private Long ttl;
    
    // 7-day stats attributes
    private double sevenDayAverage; // average problems solved per day over 7 days
    private Map<String, TagAverage> tags; // 7-day tag averages
    
    public SevenDayStats() {}
    
    public SevenDayStats(String username, double sevenDayAverage, Map<String, TagAverage> tags) {
        this.username = username;
        this.statType = "SEVEN_DAY";
        this.sevenDayAverage = sevenDayAverage;
        this.tags = tags;
        // Set 25-hour TTL
        this.ttl = java.time.Instant.now().plusSeconds(25 * 60 * 60).getEpochSecond();
    }
    
    
    // DynamoDB key getters/setters
    @DynamoDbPartitionKey
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    @DynamoDbSortKey
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    
    @DynamoDbAttribute("ttl")
    public Long getTtl() { return ttl; }
    public void setTtl(Long ttl) { this.ttl = ttl; }
    
    // Stats attribute getters/setters
    @DynamoDbAttribute("sevenDayAverage")
    public double getSevenDayAverage() { return sevenDayAverage; }
    public void setSevenDayAverage(double sevenDayAverage) { this.sevenDayAverage = sevenDayAverage; }
    
    @DynamoDbAttribute("tags")
    public Map<String, TagAverage> getTags() { return tags; }
    public void setTags(Map<String, TagAverage> tags) { this.tags = tags; }
    
    @DynamoDbBean
    public static class TagAverage {
        private int count; // total count over 7 days
        private double averageRuntimeMs;
        private double averageMemoryMb;
        private double averageDifficultyLevel;
        
        public TagAverage() {}
        
        public TagAverage(int count, double averageRuntimeMs, double averageMemoryMb, double averageDifficultyLevel) {
            this.count = count;
            this.averageRuntimeMs = averageRuntimeMs;
            this.averageMemoryMb = averageMemoryMb;
            this.averageDifficultyLevel = averageDifficultyLevel;
        }
        
        @DynamoDbAttribute("count")
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        
        @DynamoDbAttribute("averageRuntimeMs")
        public double getAverageRuntimeMs() { return averageRuntimeMs; }
        public void setAverageRuntimeMs(double averageRuntimeMs) { this.averageRuntimeMs = averageRuntimeMs; }
        
        @DynamoDbAttribute("averageMemoryMb")
        public double getAverageMemoryMb() { return averageMemoryMb; }
        public void setAverageMemoryMb(double averageMemoryMb) { this.averageMemoryMb = averageMemoryMb; }
        
        @DynamoDbAttribute("averageDifficultyLevel")
        public double getAverageDifficultyLevel() { return averageDifficultyLevel; }
        public void setAverageDifficultyLevel(double averageDifficultyLevel) { this.averageDifficultyLevel = averageDifficultyLevel; }
    }
}