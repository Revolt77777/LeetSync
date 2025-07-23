package com.leetsync.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.leetsync.recommendation.model.DailyStats;
import com.leetsync.recommendation.model.TotalStats;
import com.leetsync.recommendation.model.StreakStats;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStatsService {
    private static final Logger logger = LoggerFactory.getLogger(UserStatsService.class);
    private static final String STATS_TABLE = "UserStatsCache";
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<DailyStats> dailyTable;
    private final DynamoDbTable<TotalStats> totalTable;
    private final DynamoDbTable<StreakStats> streakTable;
    private final ObjectMapper objectMapper;
    
    public UserStatsService() {
        DynamoDbClient dynamoClient = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .build();
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoClient)
                .build();
        this.dailyTable = enhancedClient.table(STATS_TABLE, TableSchema.fromBean(DailyStats.class));
        this.totalTable = enhancedClient.table(STATS_TABLE, TableSchema.fromBean(TotalStats.class));
        this.streakTable = enhancedClient.table(STATS_TABLE, TableSchema.fromBean(StreakStats.class));
        this.objectMapper = new ObjectMapper();
    }
    
    
    public List<String> getActiveUsersFromYesterday() {
        try {
            LocalDate yesterday = LocalDate.now(SEATTLE_ZONE).minusDays(1);
            String yesterdayStatType = "DAILY#" + yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                    .filterExpression(Expression.builder()
                            .expression("statType = :statType")
                            .putExpressionValue(":statType", AttributeValue.builder().s(yesterdayStatType).build())
                            .build())
                    .build();
            
            List<String> usernames = dailyTable.scan(scanRequest).items().stream()
                    .map(DailyStats::getUsername)
                    .filter(username -> username != null)
                    .toList();
            
            logger.info("Found {} users with daily stats from {}", usernames.size(), yesterday);
            return usernames;
            
        } catch (Exception e) {
            logger.error("Error fetching active users from yesterday", e);
            throw new RuntimeException("Failed to fetch active users", e);
        }
    }
    
    public String getUserStatsAsJson(String username) {
        try {
            Map<String, Object> allStats = new HashMap<>();
            allStats.put("username", username);
            
            // Get total stats
            Key key = Key.builder().partitionValue(username).sortValue("TOTAL").build();
            TotalStats totalStats = totalTable.getItem(key);
            if (totalStats != null) {
                allStats.put("totalStats", totalStats);
            }
            
            // Get streak stats
            Key streakKey = Key.builder().partitionValue(username).sortValue("STREAKS").build();
            StreakStats streakStats = streakTable.getItem(streakKey);
            if (streakStats != null) {
                allStats.put("streakStats", streakStats);
            }
            
            // Get most recent daily stats (yesterday)
            LocalDate yesterday = LocalDate.now(SEATTLE_ZONE).minusDays(1);
            String dailyStatType = "DAILY#" + yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Key dailyKey = Key.builder().partitionValue(username).sortValue(dailyStatType).build();
            DailyStats dailyStats = dailyTable.getItem(dailyKey);
            if (dailyStats != null) {
                allStats.put("recentDailyStats", dailyStats);
            }
            
            return objectMapper.writeValueAsString(allStats);
            
        } catch (Exception e) {
            logger.error("Error fetching stats for user: {}", username, e);
            throw new RuntimeException("Failed to fetch user stats", e);
        }
    }
}