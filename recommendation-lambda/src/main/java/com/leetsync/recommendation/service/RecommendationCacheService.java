package com.leetsync.recommendation.service;

import com.leetsync.recommendation.model.UserRecommendations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class RecommendationCacheService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationCacheService.class);
    private static final String TABLE_NAME = "RecommendationsCache";
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    private static final long TTL_HOURS = 24;
    
    private final DynamoDbTable<UserRecommendations> table;
    
    public RecommendationCacheService() {
        DynamoDbClient dynamoClient = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoClient)
                .build();
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(UserRecommendations.class));
    }
    
    
    public UserRecommendations getRecommendations(String username) {
        try {
            String today = LocalDate.now(SEATTLE_ZONE).toString();
            Key key = Key.builder()
                    .partitionValue(username)
                    .sortValue(today)
                    .build();
            
            return table.getItem(key);
            
        } catch (Exception e) {
            logger.error("Error fetching recommendations for user: {}", username, e);
            return null;
        }
    }
    
    public void saveRecommendations(UserRecommendations recommendations) {
        try {
            long now = Instant.now().getEpochSecond();
            long ttl = now + (TTL_HOURS * 3600);
            
            recommendations.setGeneratedAt(now);
            recommendations.setTtl(ttl);
            
            table.putItem(recommendations);
            logger.info("Saved recommendations for user: {}", recommendations.getUsername());
            
        } catch (Exception e) {
            logger.error("Error saving recommendations for user: {}", recommendations.getUsername(), e);
            throw new RuntimeException("Failed to save recommendations", e);
        }
    }
}