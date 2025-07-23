package com.leetsync.api.dto;

/**
 * Response DTO for difficulty stats endpoint
 * Contains difficulty breakdown for both yesterday and total stats
 */
public class DifficultyStatsResponse {
    
    private DifficultyBreakdown yesterday;
    private DifficultyBreakdown total;
    
    public DifficultyStatsResponse() {}
    
    public DifficultyStatsResponse(DifficultyBreakdown yesterday, DifficultyBreakdown total) {
        this.yesterday = yesterday;
        this.total = total;
    }
    
    // Getters and setters
    public DifficultyBreakdown getYesterday() { return yesterday; }
    public void setYesterday(DifficultyBreakdown yesterday) { this.yesterday = yesterday; }
    
    public DifficultyBreakdown getTotal() { return total; }
    public void setTotal(DifficultyBreakdown total) { this.total = total; }
    
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
        
        public DifficultyLevel getEasy() { return easy; }
        public void setEasy(DifficultyLevel easy) { this.easy = easy; }
        
        public DifficultyLevel getMedium() { return medium; }
        public void setMedium(DifficultyLevel medium) { this.medium = medium; }
        
        public DifficultyLevel getHard() { return hard; }
        public void setHard(DifficultyLevel hard) { this.hard = hard; }
    }
    
    public static class DifficultyLevel {
        private int count;
        private double percentage;
        
        public DifficultyLevel() {}
        
        public DifficultyLevel(int count, double percentage) {
            this.count = count;
            this.percentage = percentage;
        }
        
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
}