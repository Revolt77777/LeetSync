package com.leetsync.stats.service;

import com.leetsync.stats.model.DailyStats;
import com.leetsync.stats.model.TotalStats;
import com.leetsync.stats.model.StreakStats;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.athena.model.ResultSet;
import software.amazon.awssdk.services.athena.model.Row;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Stats calculation service
 * Single Athena query per user, calculates all stats efficiently
 */
public class StatsCalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsCalculationService.class);
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    
    private final AthenaQueryService athenaService;
    private final StatsCacheService cacheService;
    
    public StatsCalculationService(AthenaQueryService athenaService, StatsCacheService cacheService) {
        this.athenaService = athenaService;
        this.cacheService = cacheService;
    }
    
    /**
     * Calculate all stats for a user based on yesterday's data
     * Single entry point that calculates daily, total, and streak stats
     */
    public void calculateAllStatsForUser(String username, LocalDate yesterday) {
        logger.info("Calculating all stats for user: {} on {}", username, yesterday);
        
        // Step 1: Get yesterday's raw data from Athena
        ResultSet athenaResults = athenaService.getYesterdayDailyStats(username, yesterday);
        YesterdayRawData rawData = parseAthenaResults(athenaResults);
        
        if (rawData.totalSolvedCount == 0) {
            logger.debug("User {} had no submissions on {}, skipping", username, yesterday);
            return;
        }
        
        logger.info("User {} solved {} problems yesterday", username, rawData.totalSolvedCount);
        
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DailyStats dailyStats = calculateDailyStats(username, dateStr, rawData);
        
        // Step 3: Calculate total stats (fetch existing + update)
        TotalStats totalStats = calculateTotalStats(username, rawData);
        
        // Step 4: Calculate streak stats
        StreakStats streakStats = calculateStreakStats(username, yesterday, rawData.totalSolvedCount > 0);
        
        // Step 5: Save all stats
        cacheService.saveDailyStats(username, yesterday, dailyStats);
        cacheService.saveTotalStats(username, totalStats);
        cacheService.saveStreakStats(username, streakStats);
        
        logger.info("Successfully calculated and saved all stats for user: {}", username);
    }
    
    /**
     * Parse Athena results into structured data
     */
    private YesterdayRawData parseAthenaResults(ResultSet athenaResults) {
        YesterdayRawData data = new YesterdayRawData();
        
        // Skip header row at index 0
        for (int i = 1; i < athenaResults.rows().size(); i++) {
            Row row = athenaResults.rows().get(i);
            if (row.data().size() < 6) continue;
            
            String metricType = getStringValue(row, 0);
            String metricKey = getStringValue(row, 1);
            int count = getIntValue(row, 2);
            double totalRuntime = getDoubleValue(row, 3);
            double totalMemory = getDoubleValue(row, 4);
            double avgDifficulty = getDoubleValue(row, 5);
            
            switch (metricType) {
                case "summary":
                    data.totalSolvedCount = count;
                    break;
                    
                case "difficulty":
                    data.difficultyBreakdown.put(metricKey, count);
                    break;
                    
                case "tag":
                    if (count > 0) { // Only include tags that were actually used
                        data.tagTotals.put(metricKey, new TagTotalData(count, totalRuntime, totalMemory, avgDifficulty));
                    }
                    break;
                    
                case "problems":
                    data.problemTitles.add(metricKey);
                    break;
            }
        }
        
        return data;
    }
    
    private DailyStats calculateDailyStats(String username, String date, YesterdayRawData rawData) {
        int total = rawData.totalSolvedCount;
        
        // Calculate difficulty breakdown with percentages
        int easyCount = rawData.difficultyBreakdown.getOrDefault("1", 0);
        int mediumCount = rawData.difficultyBreakdown.getOrDefault("2", 0);
        int hardCount = rawData.difficultyBreakdown.getOrDefault("3", 0);
        
        double easyPct = total > 0 ? (double) easyCount / total * 100 : 0;
        double mediumPct = total > 0 ? (double) mediumCount / total * 100 : 0;
        double hardPct = total > 0 ? (double) hardCount / total * 100 : 0;
        
        DailyStats.DifficultyBreakdown difficulty = new DailyStats.DifficultyBreakdown(
            new DailyStats.DifficultyLevel(easyCount, easyPct),
            new DailyStats.DifficultyLevel(mediumCount, mediumPct),
            new DailyStats.DifficultyLevel(hardCount, hardPct)
        );
        
        return new DailyStats(username, date, total, difficulty, rawData.problemTitles);
    }
    
    /**
     * Calculate total stats by fetching existing and adding yesterday's data
     */
    private TotalStats calculateTotalStats(String username, YesterdayRawData rawData) {
        // Fetch existing total stats (returns null if doesn't exist)
        TotalStats existing = cacheService.getTotalStats(username);
        
        if (existing == null) {
            logger.info("Creating initial total stats for user: {}", username);
            // Initialize from yesterday's data
            return createInitialTotalStats(username, rawData);
        } else {
            logger.info("Updating existing total stats for user: {}", username);
            // Update existing with yesterday's data
            return updateTotalStats(username, existing, rawData);
        }
    }
    
    private TotalStats createInitialTotalStats(String username, YesterdayRawData rawData) {
        int total = rawData.totalSolvedCount;
        
        // Same difficulty breakdown as daily (100% of total so far)
        int easyCount = rawData.difficultyBreakdown.getOrDefault("1", 0);
        int mediumCount = rawData.difficultyBreakdown.getOrDefault("2", 0);
        int hardCount = rawData.difficultyBreakdown.getOrDefault("3", 0);
        
        double easyPct = total > 0 ? (double) easyCount / total * 100 : 0;
        double mediumPct = total > 0 ? (double) mediumCount / total * 100 : 0;
        double hardPct = total > 0 ? (double) hardCount / total * 100 : 0;
        
        TotalStats.DifficultyBreakdown difficulty = new TotalStats.DifficultyBreakdown(
            new TotalStats.DifficultyLevel(easyCount, easyPct),
            new TotalStats.DifficultyLevel(mediumCount, mediumPct),
            new TotalStats.DifficultyLevel(hardCount, hardPct)
        );
        
        // Convert tag totals to averages
        Map<String, TotalStats.TagAverage> tagAverages = new HashMap<>();
        for (Map.Entry<String, TagTotalData> entry : rawData.tagTotals.entrySet()) {
            TagTotalData data = entry.getValue();
            tagAverages.put(entry.getKey(), new TotalStats.TagAverage(
                data.count,
                data.totalRuntimeMs / data.count, // average
                data.totalMemoryMb / data.count,  // average
                data.totalDifficultyLevel / data.count // average
            ));
        }
        
        return new TotalStats(username, total, difficulty, tagAverages);
    }
    
    private TotalStats updateTotalStats(String username, TotalStats existing, YesterdayRawData rawData) {
        // Update total count
        int newTotal = existing.getTotalSolvedCount() + rawData.totalSolvedCount;
        
        // Update difficulty counts and recalculate percentages
        int newEasy = existing.getDifficulty().getEasy().getCount() + rawData.difficultyBreakdown.getOrDefault("1", 0);
        int newMedium = existing.getDifficulty().getMedium().getCount() + rawData.difficultyBreakdown.getOrDefault("2", 0);
        int newHard = existing.getDifficulty().getHard().getCount() + rawData.difficultyBreakdown.getOrDefault("3", 0);
        
        double easyPct = newTotal > 0 ? (double) newEasy / newTotal * 100 : 0;
        double mediumPct = newTotal > 0 ? (double) newMedium / newTotal * 100 : 0;
        double hardPct = newTotal > 0 ? (double) newHard / newTotal * 100 : 0;
        
        TotalStats.DifficultyBreakdown difficulty = new TotalStats.DifficultyBreakdown(
            new TotalStats.DifficultyLevel(newEasy, easyPct),
            new TotalStats.DifficultyLevel(newMedium, mediumPct),
            new TotalStats.DifficultyLevel(newHard, hardPct)
        );
        
        // Update tag averages using running average formula
        Map<String, TotalStats.TagAverage> updatedTags = new HashMap<>(existing.getTags());
        
        for (Map.Entry<String, TagTotalData> entry : rawData.tagTotals.entrySet()) {
            String tag = entry.getKey();
            TagTotalData yesterdayData = entry.getValue();
            
            TotalStats.TagAverage existingTag = updatedTags.get(tag);
            if (existingTag == null) {
                // New tag, just convert totals to averages
                updatedTags.put(tag, new TotalStats.TagAverage(
                    yesterdayData.count,
                    yesterdayData.totalRuntimeMs / yesterdayData.count,
                    yesterdayData.totalMemoryMb / yesterdayData.count,
                    yesterdayData.totalDifficultyLevel / yesterdayData.count
                ));
            } else {
                // Update existing tag average using weighted average
                int oldCount = existingTag.getCount();
                int newCount = oldCount + yesterdayData.count;
                
                double newAvgRuntime = (existingTag.getAverageRuntimeMs() * oldCount + yesterdayData.totalRuntimeMs) / newCount;
                double newAvgMemory = (existingTag.getAverageMemoryMb() * oldCount + yesterdayData.totalMemoryMb) / newCount;
                double newAvgDifficulty = (existingTag.getAverageDifficultyLevel() * oldCount + yesterdayData.totalDifficultyLevel) / newCount;
                
                updatedTags.put(tag, new TotalStats.TagAverage(newCount, newAvgRuntime, newAvgMemory, newAvgDifficulty));
            }
        }
        
        return new TotalStats(username, newTotal, difficulty, updatedTags);
    }
    
    /**
     * Calculate streak information
     */
    private StreakStats calculateStreakStats(String username, LocalDate yesterday, boolean solvedYesterday) {
        // Fetch existing streak info (returns null if doesn't exist)
        StreakStats existing = cacheService.getStreakStats(username);
        
        if (existing == null) {
            // Initialize streaks
            if (solvedYesterday) {
                return new StreakStats(username, 1, 1);
            } else {
                return new StreakStats(username, 0, 0);
            }
        } else {
            // Update existing streaks
            int newCurrentStreak;
            int newLongestStreak = existing.getLongestStreak();
            
            if (solvedYesterday) {
                newCurrentStreak = existing.getCurrentStreak() + 1;
                newLongestStreak = Math.max(newLongestStreak, newCurrentStreak);
            } else {
                newCurrentStreak = 0;
            }
            
            return new StreakStats(username, newCurrentStreak, newLongestStreak);
        }
    }
    
    // Helper methods for parsing Athena results
    private String getStringValue(Row row, int index) {
        return row.data().get(index).varCharValue();
    }
    
    private int getIntValue(Row row, int index) {
        String value = row.data().get(index).varCharValue();
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 0;
    }
    
    private double getDoubleValue(Row row, int index) {
        String value = row.data().get(index).varCharValue();
        return value != null && !value.isEmpty() ? Double.parseDouble(value) : 0.0;
    }
    
    // Helper classes for internal data structures
    private static class YesterdayRawData {
        int totalSolvedCount = 0;
        Map<String, Integer> difficultyBreakdown = new HashMap<>(); // difficultyLevel -> count
        Map<String, TagTotalData> tagTotals = new HashMap<>(); // tag -> totals
        List<String> problemTitles = new ArrayList<>(); // problem titles solved
    }
    
    private static class TagTotalData {
        int count;
        double totalRuntimeMs;
        double totalMemoryMb;
        double totalDifficultyLevel;
        
        TagTotalData(int count, double totalRuntimeMs, double totalMemoryMb, double totalDifficultyLevel) {
            this.count = count;
            this.totalRuntimeMs = totalRuntimeMs;
            this.totalMemoryMb = totalMemoryMb;
            this.totalDifficultyLevel = totalDifficultyLevel;
        }
    }
}