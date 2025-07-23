package com.leetsync.api.dto;

import java.util.Map;

/**
 * Response DTO for tag stats endpoint
 * Contains tag performance metrics from total stats
 */
public class TagStatsResponse {
    
    private Map<String, TagStats> tags;
    
    public TagStatsResponse() {}
    
    public TagStatsResponse(Map<String, TagStats> tags) {
        this.tags = tags;
    }
    
    // Getters and setters
    public Map<String, TagStats> getTags() { return tags; }
    public void setTags(Map<String, TagStats> tags) { this.tags = tags; }
    
    public static class TagStats {
        private int count;
        private double averageRuntimeMs;
        private double averageMemoryMb;
        private double averageDifficultyLevel;
        
        public TagStats() {}
        
        public TagStats(int count, double averageRuntimeMs, double averageMemoryMb, double averageDifficultyLevel) {
            this.count = count;
            this.averageRuntimeMs = averageRuntimeMs;
            this.averageMemoryMb = averageMemoryMb;
            this.averageDifficultyLevel = averageDifficultyLevel;
        }
        
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        
        public double getAverageRuntimeMs() { return averageRuntimeMs; }
        public void setAverageRuntimeMs(double averageRuntimeMs) { this.averageRuntimeMs = averageRuntimeMs; }
        
        public double getAverageMemoryMb() { return averageMemoryMb; }
        public void setAverageMemoryMb(double averageMemoryMb) { this.averageMemoryMb = averageMemoryMb; }
        
        public double getAverageDifficultyLevel() { return averageDifficultyLevel; }
        public void setAverageDifficultyLevel(double averageDifficultyLevel) { this.averageDifficultyLevel = averageDifficultyLevel; }
    }
}