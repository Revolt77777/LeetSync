package com.leetsync.api.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import java.util.List;

/**
 * Daily stats model for API server with DynamoDB annotations
 * Optimized for API service needs
 */
@DynamoDbBean
public class DailyStats {
    
    private String username;
    private String statType;
    private Long ttl;
    private String date;
    private int yesterdaySolvedCount;
    private DifficultyBreakdown difficulty;
    private List<String> problemsSolved;
    
    public DailyStats() {}
    
    @DynamoDbPartitionKey
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    @DynamoDbSortKey
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    
    @DynamoDbAttribute("ttl")
    public Long getTtl() { return ttl; }
    public void setTtl(Long ttl) { this.ttl = ttl; }
    
    @DynamoDbAttribute("date")
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    @DynamoDbAttribute("yesterdaySolvedCount")
    public int getYesterdaySolvedCount() { return yesterdaySolvedCount; }
    public void setYesterdaySolvedCount(int yesterdaySolvedCount) { this.yesterdaySolvedCount = yesterdaySolvedCount; }
    
    @DynamoDbAttribute("difficulty")
    public DifficultyBreakdown getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyBreakdown difficulty) { this.difficulty = difficulty; }
    
    @DynamoDbAttribute("problemsSolved")
    public List<String> getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(List<String> problemsSolved) { this.problemsSolved = problemsSolved; }
    
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
}