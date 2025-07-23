package com.leetsync.problem.service;

import com.leetsync.problem.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProblemDynamoService {

    private static final Logger log = LoggerFactory.getLogger(ProblemDynamoService.class);

    private final DynamoDbTable<Problem> table;
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public ProblemDynamoService(DynamoDbEnhancedClient enhancedClient, DynamoDbClient dynamoDbClient, String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Problem.class));
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public int storeProblems(List<Problem> problems) {
        int storedCount = 0;
        
        for (Problem problem : problems) {
            try {
                boolean stored = storeProblem(problem);
                if (stored) {
                    storedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to store problem {}: {}", problem.getTitleSlug(), e.getMessage());
            }
        }
        
        log.info("Successfully stored {} out of {} problems", storedCount, problems.size());
        return storedCount;
    }

    public Set<String> getExistingTitleSlugs(Set<String> titleSlugs) {
        Set<String> existingTitleSlugs = new HashSet<>();
        
        if (titleSlugs == null || titleSlugs.isEmpty()) {
            return existingTitleSlugs;
        }
        
        // Convert Set to List for batch processing
        List<String> titleSlugsList = new ArrayList<>(titleSlugs);
        
        // DynamoDB BatchGetItem has a limit of 100 items per request
        int batchSize = 100;
        for (int i = 0; i < titleSlugsList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, titleSlugsList.size());
            List<String> batch = titleSlugsList.subList(i, endIndex);
            
            try {
                Set<String> batchExisting = getBatchExistingTitleSlugs(batch);
                existingTitleSlugs.addAll(batchExisting);
            } catch (Exception e) {
                log.error("Failed to check existing title slugs for batch: {}", e.getMessage());
            }
        }
        
        log.info("Found {} existing title slugs out of {} checked", existingTitleSlugs.size(), titleSlugs.size());
        return existingTitleSlugs;
    }

    private Set<String> getBatchExistingTitleSlugs(List<String> titleSlugs) {
        Set<String> existingTitleSlugs = new HashSet<>();
        
        // Create keys for batch get request
        List<Map<String, AttributeValue>> keys = titleSlugs.stream()
                .map(titleSlug -> {
                    Map<String, AttributeValue> key = new HashMap<>();
                    key.put("titleSlug", AttributeValue.fromS(titleSlug));
                    return key;
                })
                .collect(Collectors.toList());
        
        KeysAndAttributes keysAndAttributes = KeysAndAttributes.builder()
                .keys(keys)
                .projectionExpression("titleSlug")
                .build();
        
        BatchGetItemRequest batchGetRequest = BatchGetItemRequest.builder()
                .requestItems(Map.of(tableName, keysAndAttributes))
                .build();
        
        try {
            BatchGetItemResponse response = dynamoDbClient.batchGetItem(batchGetRequest);
            
            if (response.responses().containsKey(tableName)) {
                for (Map<String, AttributeValue> item : response.responses().get(tableName)) {
                    AttributeValue titleSlugValue = item.get("titleSlug");
                    if (titleSlugValue != null) {
                        existingTitleSlugs.add(titleSlugValue.s());
                    }
                }
            }
            
            log.debug("Retrieved {} existing title slugs from batch of {}", existingTitleSlugs.size(), titleSlugs.size());
        } catch (DynamoDbException e) {
            log.error("DynamoDB error while checking existing title slugs: {}", e.getMessage());
        }
        
        return existingTitleSlugs;
    }

    public boolean storeProblem(Problem problem) {
        if (problem.getTitleSlug() == null) {
            log.warn("Skipping problem with null title slug");
            return false;
        }

        try {
            table.putItem(problem);
            return true;
        } catch (Exception e) {
            log.error("DynamoDB error while storing problem {}: {}", problem.getTitleSlug(), e.getMessage());
            return false;
        }
    }
}