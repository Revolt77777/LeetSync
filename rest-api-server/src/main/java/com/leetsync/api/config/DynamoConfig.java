package com.leetsync.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoConfig {

    /** Low-level SDK client (uses default credential chain). */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_WEST_2)   // or omit for default
                .build();
    }

    /** Enhanced client that Spring can autowire. */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient base) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(base)
                .build();
    }
}
