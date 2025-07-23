package com.leetsync.api.dto;

import java.util.List;

/**
 * Response DTO for stats summary endpoint
 * Combines data from daily, total, and streak stats
 */
public class StatsSummaryResponse {
    
    // Daily stats
    private int yesterdaySolvedCount;
    private List<String> problemsSolved;
    
    // Total stats
    private int totalSolvedCount;
    
    // Streak stats
    private int currentStreak;
    private int longestStreak;
    
    public StatsSummaryResponse() {}
    
    public StatsSummaryResponse(int yesterdaySolvedCount, List<String> problemsSolved, 
                               int totalSolvedCount, int currentStreak, int longestStreak) {
        this.yesterdaySolvedCount = yesterdaySolvedCount;
        this.problemsSolved = problemsSolved;
        this.totalSolvedCount = totalSolvedCount;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }
    
    // Getters and setters
    public int getYesterdaySolvedCount() { return yesterdaySolvedCount; }
    public void setYesterdaySolvedCount(int yesterdaySolvedCount) { this.yesterdaySolvedCount = yesterdaySolvedCount; }
    
    public List<String> getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(List<String> problemsSolved) { this.problemsSolved = problemsSolved; }
    
    public int getTotalSolvedCount() { return totalSolvedCount; }
    public void setTotalSolvedCount(int totalSolvedCount) { this.totalSolvedCount = totalSolvedCount; }
    
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
}