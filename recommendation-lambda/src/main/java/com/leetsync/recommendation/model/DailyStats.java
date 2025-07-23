package com.leetsync.recommendation.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import java.util.List;

/**
 * Daily stats for a specific date - stored as DAILY#{date}
 * Contains only yesterday's summary and difficulty breakdown
 */
@DynamoDbBean
public class DailyStats {
    
    // DynamoDB keys
    private String username;
    private String statType;
    private Long ttl;
    
    // Daily stats attributes
    private String date; // YYYY-MM-DD
    private int yesterdaySolvedCount;
    private DifficultyBreakdown difficulty;
    private List<String> problemsSolved; // List of problem titles solved yesterday
    
    public DailyStats() {}
    
    public DailyStats(String username, String date, int yesterdaySolvedCount, DifficultyBreakdown difficulty, List<String> problemsSolved) {
        this.username = username;
        this.statType = "DAILY#" + date;
        this.date = date;
        this.yesterdaySolvedCount = yesterdaySolvedCount;
        this.difficulty = difficulty;
        this.problemsSolved = problemsSolved;
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
        
        public DifficultyBreakdown(DifficultyLevel easy, DifficultyLevel medium, DifficultyLevel hard) {
            this.easy = easy;
            this.medium = medium;
            this.hard = hard;
        }
        
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
        private double percentage; // percentage of yesterday's total
        
        public DifficultyLevel() {}
        
        public DifficultyLevel(int count, double percentage) {
            this.count = count;
            this.percentage = percentage;
        }
        
        @DynamoDbAttribute("count")
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        
        @DynamoDbAttribute("percentage")
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
}