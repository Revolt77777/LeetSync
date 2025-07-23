package com.leetsync.api.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import java.util.Map;

/**
 * Total stats model for API server with DynamoDB annotations
 * Optimized for API service needs
 */
@DynamoDbBean
public class TotalStats {
    
    private String username;
    private String statType;
    private int totalSolvedCount;
    private DifficultyBreakdown difficulty;
    private Map<String, TagAverage> tags;
    
    public TotalStats() {}
    
    @DynamoDbPartitionKey
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    @DynamoDbSortKey
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    
    @DynamoDbAttribute("totalSolvedCount")
    public int getTotalSolvedCount() { return totalSolvedCount; }
    public void setTotalSolvedCount(int totalSolvedCount) { this.totalSolvedCount = totalSolvedCount; }
    
    @DynamoDbAttribute("difficulty")
    public DifficultyBreakdown getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyBreakdown difficulty) { this.difficulty = difficulty; }
    
    @DynamoDbAttribute("tags")
    public Map<String, TagAverage> getTags() { return tags; }
    public void setTags(Map<String, TagAverage> tags) { this.tags = tags; }
    
    @DynamoDbBean
    public static class DifficultyBreakdown {
        private DifficultyLevel easy;
        private DifficultyLevel medium;
        private DifficultyLevel hard;
        
        public DifficultyBreakdown() {}
        
        @DynamoDbAttribute("easy")
        public DifficultyLevel getEasy() { return easy; }
        public void setEasy(DifficultyLevel easy) { this.easy = easy; }
        
        @DynamoDbAttribute("medium")
        public DifficultyLevel getMedium() { return medium; }
        public void setMedium(DifficultyLevel medium) { this.medium = medium; }
        
        @DynamoDbAttribute("hard")
        public DifficultyLevel getHard() { return hard; }
        public void setHard(DifficultyLevel hard) { this.hard = hard; }
    }
    
    @DynamoDbBean
    public static class DifficultyLevel {
        private int count;
        private double percentage;
        
        public DifficultyLevel() {}
        
        @DynamoDbAttribute("count")
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        
        @DynamoDbAttribute("percentage")
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
    
    @DynamoDbBean
    public static class TagAverage {
        private int count;
        private double averageRuntimeMs;
        private double averageMemoryMb;
        private double averageDifficultyLevel;
        
        public TagAverage() {}
        
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