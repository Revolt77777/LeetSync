package com.leetsync.problem.service;

import com.leetsync.shared.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ProblemDynamoService {

    private static final Logger log = LoggerFactory.getLogger(ProblemDynamoService.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public ProblemDynamoService(DynamoDbClient dynamoDbClient, String tableName) {
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

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("titleSlug", AttributeValue.fromS(problem.getTitleSlug())); // Primary key
        
        // Map the new fields according to the updated Problem model
        if (problem.getQuestionId() != null) {
            item.put("questionId", AttributeValue.fromN(problem.getQuestionId().toString()));
        }
        if (problem.getFrontendQuestionId() != null) {
            item.put("frontendQuestionId", AttributeValue.fromN(problem.getFrontendQuestionId().toString()));
        }
        if (problem.getTotalAccepted() != null) {
            item.put("totalAccepted", AttributeValue.fromN(problem.getTotalAccepted().toString()));
        }
        if (problem.getTotalSubmitted() != null) {
            item.put("totalSubmitted", AttributeValue.fromN(problem.getTotalSubmitted().toString()));
        }
        if (problem.getDifficultyLevel() != null) {
            item.put("difficultyLevel", AttributeValue.fromN(problem.getDifficultyLevel().toString()));
        }
        if (problem.getProgress() != null) {
            item.put("progress", AttributeValue.fromN(problem.getProgress().toString()));
        }
        if (problem.getDifficulty() != null) {
            item.put("difficulty", AttributeValue.fromS(problem.getDifficulty()));
        }
        if (problem.getAcRate() != null) {
            item.put("acRate", AttributeValue.fromN(problem.getAcRate().toString()));
        }
        
        // Store topic tags if present
        if (problem.getTopicTags() != null && !problem.getTopicTags().isEmpty()) {
            List<AttributeValue> topicTagsList = new ArrayList<>();
            for (Problem.TopicTag tag : problem.getTopicTags()) {
                Map<String, AttributeValue> tagMap = new HashMap<>();
                tagMap.put("name", AttributeValue.fromS(tag.getName()));
                tagMap.put("slug", AttributeValue.fromS(tag.getSlug()));
                topicTagsList.add(AttributeValue.fromM(tagMap));
            }
            item.put("topicTags", AttributeValue.fromL(topicTagsList));
        }

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(putRequest);
            log.debug("Stored problem with titleSlug: {}", problem.getTitleSlug());
            return true;
        } catch (DynamoDbException e) {
            log.error("DynamoDB error while storing problem {}: {}", problem.getTitleSlug(), e.getMessage());
            return false;
        }
    }
}