package com.leetsync.ingestion.service;

import com.leetsync.ingestion.model.AcSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

public class DynamoService {

    private static final Logger log = LoggerFactory.getLogger(DynamoService.class);

    private final DynamoDbTable<AcSubmission> table;

    public DynamoService(DynamoDbEnhancedClient enhancedClient, String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(AcSubmission.class));
    }

    public boolean storeIfNew(AcSubmission submission) {
        String username = submission.getUsername();
        String titleSlug = submission.getTitleSlug();
        long timestamp = submission.getTimestamp();
        log.info("Attempting to store submission for user: {} with titleSlug: {} and timestamp: {}", username, titleSlug, timestamp);

        Expression conditionExpression = Expression.builder()
                .expression("attribute_not_exists(username) AND attribute_not_exists(#ts)")
                .putExpressionName("#ts", "timestamp")
                .build();

        PutItemEnhancedRequest<AcSubmission> putRequest = PutItemEnhancedRequest.builder(AcSubmission.class)
                .item(submission)
                .conditionExpression(conditionExpression)
                .build();

        try {
            table.putItem(putRequest);
            log.info("Stored new submission for user: {} with titleSlug: {} and timestamp: {}", username, titleSlug, timestamp);
            return true;
        } catch (ConditionalCheckFailedException e) {
            log.info("Submission for user {} with titleSlug {} and timestamp {} already exists. Skipping.", username, titleSlug, timestamp);
            return false;
        } catch (Exception e) {
            log.error("DynamoDB error while storing submission for user {} with titleSlug {} and timestamp {}: {}", username, titleSlug, timestamp, e.getMessage());
            return false;
        }
    }
}
