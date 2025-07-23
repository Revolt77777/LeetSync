package com.leetsync.api.repository;

import com.leetsync.api.model.DailyStats;
import com.leetsync.api.model.TotalStats;
import com.leetsync.api.model.StreakStats;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Repository for accessing UserStatsCache table using Enhanced Client with annotated models
 * Each service owns its models - true microservices pattern
 */
@Repository
public class StatsRepository {
    
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    
    private final DynamoDbTable<DailyStats> dailyStatsTable;
    private final DynamoDbTable<TotalStats> totalStatsTable;
    private final DynamoDbTable<StreakStats> streakStatsTable;
    
    public StatsRepository(DynamoDbEnhancedClient enhancedClient) {
        String tableName = System.getenv("STATS_CACHE_TABLE_NAME");
        if (tableName == null) {
            tableName = "UserStatsCache"; // Default name
        }
        
        // Use annotated models directly - no static schemas needed
        this.dailyStatsTable = enhancedClient.table(tableName, TableSchema.fromBean(DailyStats.class));
        this.totalStatsTable = enhancedClient.table(tableName, TableSchema.fromBean(TotalStats.class));
        this.streakStatsTable = enhancedClient.table(tableName, TableSchema.fromBean(StreakStats.class));
    }
    
    /**
     * Get most recent daily stats (yesterday's stats)
     * Uses Seattle timezone to match stats calculation
     */
    public Optional<DailyStats> getRecentDailyStats(String username) {
        LocalDate yesterday = LocalDate.now(SEATTLE_ZONE).minusDays(1);
        String statType = "DAILY#" + yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        Key key = Key.builder().partitionValue(username).sortValue(statType).build();
        DailyStats stats = dailyStatsTable.getItem(key);
        return Optional.ofNullable(stats);
    }
    
    /**
     * Get total stats for user
     */
    public Optional<TotalStats> getTotalStats(String username) {
        Key key = Key.builder().partitionValue(username).sortValue("TOTAL").build();
        TotalStats stats = totalStatsTable.getItem(key);
        return Optional.ofNullable(stats);
    }
    
    /**
     * Get streak stats for user
     */
    public Optional<StreakStats> getStreakStats(String username) {
        Key key = Key.builder().partitionValue(username).sortValue("STREAKS").build();
        StreakStats stats = streakStatsTable.getItem(key);
        return Optional.ofNullable(stats);
    }
}