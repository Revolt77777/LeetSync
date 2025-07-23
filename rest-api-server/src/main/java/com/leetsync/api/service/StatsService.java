package com.leetsync.api.service;

import com.leetsync.api.dto.DifficultyStatsResponse;
import com.leetsync.api.dto.StatsSummaryResponse;
import com.leetsync.api.dto.TagStatsResponse;
import com.leetsync.api.model.DailyStats;
import com.leetsync.api.model.StreakStats;
import com.leetsync.api.model.TotalStats;
import com.leetsync.api.repository.StatsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for handling stats business logic
 * Converts repository models to API response DTOs
 */
@Service
public class StatsService {
    
    private final StatsRepository statsRepository;
    
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }
    
    /**
     * Get summary stats combining daily, total, and streak data
     */
    public Optional<StatsSummaryResponse> getSummaryStats(String username) {
        try {
            // Fetch all required data
            Optional<DailyStats> dailyStats = statsRepository.getRecentDailyStats(username);
            Optional<TotalStats> totalStats = statsRepository.getTotalStats(username);
            Optional<StreakStats> streakStats = statsRepository.getStreakStats(username);
            
            // If no total stats exist, user has no data
            if (totalStats.isEmpty()) {
                return Optional.empty();
            }
            
            // Build summary response with available data
            int yesterdaySolvedCount = dailyStats.map(DailyStats::getYesterdaySolvedCount).orElse(0);
            var problemsSolved = dailyStats.map(DailyStats::getProblemsSolved).orElse(java.util.Collections.emptyList());
            int totalSolvedCount = totalStats.get().getTotalSolvedCount();
            int currentStreak = streakStats.map(StreakStats::getCurrentStreak).orElse(0);
            int longestStreak = streakStats.map(StreakStats::getLongestStreak).orElse(0);
            
            StatsSummaryResponse response = new StatsSummaryResponse(
                yesterdaySolvedCount, problemsSolved, totalSolvedCount, currentStreak, longestStreak
            );
            
            return Optional.of(response);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get summary stats for user: " + username, e);
        }
    }
    
    /**
     * Get difficulty breakdown stats for yesterday and total
     */
    public Optional<DifficultyStatsResponse> getDifficultyStats(String username) {
        try {
            Optional<DailyStats> dailyStats = statsRepository.getRecentDailyStats(username);
            Optional<TotalStats> totalStats = statsRepository.getTotalStats(username);
            
            if (totalStats.isEmpty()) {
                return Optional.empty();
            }
            
            // Convert daily difficulty breakdown
            DifficultyStatsResponse.DifficultyBreakdown yesterdayBreakdown = null;
            if (dailyStats.isPresent() && dailyStats.get().getDifficulty() != null) {
                yesterdayBreakdown = convertToDifficultyBreakdown(dailyStats.get().getDifficulty());
            }
            
            // Convert total difficulty breakdown
            DifficultyStatsResponse.DifficultyBreakdown totalBreakdown = null;
            if (totalStats.get().getDifficulty() != null) {
                totalBreakdown = convertToDifficultyBreakdown(totalStats.get().getDifficulty());
            }
            
            DifficultyStatsResponse response = new DifficultyStatsResponse(yesterdayBreakdown, totalBreakdown);
            return Optional.of(response);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get difficulty stats for user: " + username, e);
        }
    }
    
    /**
     * Get tag performance stats from total stats
     */
    public Optional<TagStatsResponse> getTagStats(String username) {
        try {
            Optional<TotalStats> totalStats = statsRepository.getTotalStats(username);
            
            if (totalStats.isEmpty() || totalStats.get().getTags() == null) {
                return Optional.empty();
            }
            
            // Convert tag averages to response format
            var tags = totalStats.get().getTags().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        entry -> new TagStatsResponse.TagStats(
                            entry.getValue().getCount(),
                            entry.getValue().getAverageRuntimeMs(),
                            entry.getValue().getAverageMemoryMb(),
                            entry.getValue().getAverageDifficultyLevel()
                        )
                    ));
            
            TagStatsResponse response = new TagStatsResponse(tags);
            return Optional.of(response);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tag stats for user: " + username, e);
        }
    }
    
    /**
     * Helper method to convert model difficulty breakdown to DTO
     */
    private DifficultyStatsResponse.DifficultyBreakdown convertToDifficultyBreakdown(DailyStats.DifficultyBreakdown modelBreakdown) {
        if (modelBreakdown == null) return null;
        
        DifficultyStatsResponse.DifficultyLevel easy = convertToDifficultyLevel(modelBreakdown.getEasy());
        DifficultyStatsResponse.DifficultyLevel medium = convertToDifficultyLevel(modelBreakdown.getMedium());  
        DifficultyStatsResponse.DifficultyLevel hard = convertToDifficultyLevel(modelBreakdown.getHard());
        
        return new DifficultyStatsResponse.DifficultyBreakdown(easy, medium, hard);
    }
    
    private DifficultyStatsResponse.DifficultyBreakdown convertToDifficultyBreakdown(TotalStats.DifficultyBreakdown modelBreakdown) {
        if (modelBreakdown == null) return null;
        
        DifficultyStatsResponse.DifficultyLevel easy = convertToDifficultyLevel(modelBreakdown.getEasy());
        DifficultyStatsResponse.DifficultyLevel medium = convertToDifficultyLevel(modelBreakdown.getMedium());  
        DifficultyStatsResponse.DifficultyLevel hard = convertToDifficultyLevel(modelBreakdown.getHard());
        
        return new DifficultyStatsResponse.DifficultyBreakdown(easy, medium, hard);
    }
    
    private DifficultyStatsResponse.DifficultyLevel convertToDifficultyLevel(DailyStats.DifficultyLevel modelLevel) {
        if (modelLevel == null) return new DifficultyStatsResponse.DifficultyLevel(0, 0.0);
        return new DifficultyStatsResponse.DifficultyLevel(modelLevel.getCount(), modelLevel.getPercentage());
    }
    
    private DifficultyStatsResponse.DifficultyLevel convertToDifficultyLevel(TotalStats.DifficultyLevel modelLevel) {
        if (modelLevel == null) return new DifficultyStatsResponse.DifficultyLevel(0, 0.0);
        return new DifficultyStatsResponse.DifficultyLevel(modelLevel.getCount(), modelLevel.getPercentage());
    }
}