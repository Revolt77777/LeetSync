package com.leetsync.api.repository;

import com.leetsync.api.model.AcSubmission;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

/**
 * Repository for accessing AcSubmissions table using Enhanced Client with annotated models
 * Each service owns its models - true microservices pattern
 */
@Repository
public class AcSubmissionRepository {

    private final DynamoDbTable<AcSubmission> table;

    public AcSubmissionRepository(DynamoDbEnhancedClient enhancedClient) {
        // Use annotated model directly - no static schema needed
        String tableName = System.getenv("ACSUBMISSIONS_TABLE_NAME");
        if (tableName == null) {
            tableName = "AcSubmissions"; // Default fallback for local development
        }
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(AcSubmission.class));
    }

    /* ---------- Public API ---------- */

    public List<AcSubmission> findAll() {
        // TODO add pagination later
        return table.scan().items().stream().toList();
    }

    public List<AcSubmission> findByUsername(String username) {
        Key partitionKey = Key.builder().partitionValue(username).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(partitionKey);
        return table.query(queryConditional).items().stream().toList();
    }

    public void deleteAllByUsername(String username) {
        Key partitionKey = Key.builder().partitionValue(username).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(partitionKey);
        table.query(queryConditional).items().forEach(table::deleteItem);
    }
}
