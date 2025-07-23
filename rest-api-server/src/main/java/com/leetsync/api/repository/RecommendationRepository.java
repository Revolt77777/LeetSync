package com.leetsync.api.repository;

import com.leetsync.api.model.UserRecommendations;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Repository
public class RecommendationRepository {
    
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    
    private final DynamoDbTable<UserRecommendations> recommendationsTable;
    
    public RecommendationRepository(DynamoDbEnhancedClient enhancedClient) {
        String tableName = System.getenv("RECOMMENDATIONS_CACHE_TABLE_NAME");
        if (tableName == null) {
            tableName = "RecommendationsCache";
        }
        
        this.recommendationsTable = enhancedClient.table(tableName, TableSchema.fromBean(UserRecommendations.class));
    }
    
    public Optional<UserRecommendations> getRecommendations(String username) {
        String today = LocalDate.now(SEATTLE_ZONE).toString();
        Key key = Key.builder().partitionValue(username).sortValue(today).build();
        UserRecommendations recommendations = recommendationsTable.getItem(key);
        return Optional.ofNullable(recommendations);
    }
}