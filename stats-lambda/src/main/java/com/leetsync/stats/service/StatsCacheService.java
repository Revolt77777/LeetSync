package com.leetsync.stats.service;

import com.leetsync.stats.model.DailyStats;
import com.leetsync.stats.model.TotalStats;
import com.leetsync.stats.model.StreakStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DynamoDB cache service for storing stats using polymorphic mapping
 * Each stat type stores itself directly with sparse attributes
 */
public class StatsCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsCacheService.class);
    private static final String TABLE_NAME = "UserStatsCache";
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<DailyStats> dailyTable;
    private final DynamoDbTable<TotalStats> totalTable;
    private final DynamoDbTable<StreakStats> streakTable;
    
    public StatsCacheService() {
        DynamoDbClient dynamoClient = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .build();
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoClient)
                .build();
        this.dailyTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(DailyStats.class));
        this.totalTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(TotalStats.class));
        this.streakTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(StreakStats.class));
    }
    
    public StatsCacheService(DynamoDbClient dynamoClient) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoClient)
                .build();
        this.dailyTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(DailyStats.class));
        this.totalTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(TotalStats.class));
        this.streakTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(StreakStats.class));
    }
    
    public TotalStats getTotalStats(String username) {
        try {
            Key key = Key.builder()
                    .partitionValue(username)
                    .sortValue("TOTAL")
                    .build();
                    
            TotalStats totalStats = totalTable.getItem(key);
            
            if (totalStats == null) {
                return null;
            }
            
            return totalStats;
            
        } catch (Exception e) {
            logger.error("Error fetching total stats for user: {}", username, e);
            throw new RuntimeException("Failed to fetch total stats", e);
        }
    }
    
    public void saveTotalStats(String username, TotalStats totalStats) {
        try {
            totalStats.setUsername(username);
            totalStats.setStatType("TOTAL");
            
            totalTable.putItem(totalStats);
            
        } catch (Exception e) {
            logger.error("Error saving total stats for user: {}", username, e);
            throw new RuntimeException("Failed to save total stats", e);
        }
    }
    
    public void saveDailyStats(String username, LocalDate date, DailyStats dailyStats) {
        try {
            dailyStats.setUsername(username);
            dailyStats.setStatType("DAILY#" + date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            dailyStats.setTtl(Instant.now().plusSeconds(7 * 24 * 60 * 60).getEpochSecond());
            
            dailyTable.putItem(dailyStats);
            
        } catch (Exception e) {
            logger.error("Error saving daily stats for user: {} on {}", username, date, e);
            throw new RuntimeException("Failed to save daily stats", e);
        }
    }
    
    public List<DailyStats> getRecentDailyStats(String username, int days) {
        LocalDate endDate = LocalDate.now(SEATTLE_ZONE).minusDays(1);
        LocalDate startDate = endDate.minusDays(days - 1);
        
        try {
            String startStatType = "DAILY#" + startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String endStatType = "DAILY#" + endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            QueryConditional queryConditional = QueryConditional
                    .sortBetween(
                            Key.builder().partitionValue(username).sortValue(startStatType).build(),
                            Key.builder().partitionValue(username).sortValue(endStatType).build()
                    );
            
            QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                    .queryConditional(queryConditional)
                    .scanIndexForward(true)
                    .build();
            
            List<DailyStats> dailyStats = new ArrayList<>();
            dailyTable.query(request).items().forEach(stats -> {
                if (stats.getStatType().startsWith("DAILY#")) {
                    dailyStats.add(stats);
                }
            });
            
            return dailyStats;
            
        } catch (Exception e) {
            logger.error("Error fetching recent daily stats for user: {}", username, e);
            throw new RuntimeException("Failed to fetch recent daily stats", e);
        }
    }
    
    
    public void saveStreakStats(String username, StreakStats streakStats) {
        try {
            streakStats.setUsername(username);
            streakStats.setStatType("STREAKS");
            
            streakTable.putItem(streakStats);
            
        } catch (Exception e) {
            logger.error("Error saving streak stats for user: {}", username, e);
            throw new RuntimeException("Failed to save streak stats", e);
        }
    }
    
    public StreakStats getStreakStats(String username) {
        try {
            Key key = Key.builder()
                    .partitionValue(username)
                    .sortValue("STREAKS")
                    .build();
                    
            return streakTable.getItem(key);
            
        } catch (Exception e) {
            logger.error("Error fetching streak stats for user: {}", username, e);
            throw new RuntimeException("Failed to fetch streak stats", e);
        }
    }
    
    public void close() {
        // Enhanced client cleanup is handled automatically
    }
}