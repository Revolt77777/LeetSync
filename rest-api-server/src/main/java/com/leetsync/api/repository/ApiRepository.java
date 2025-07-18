package com.leetsync.api.repository;

import com.leetsync.shared.model.AcSubmission;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

/**
 * Data-access class for AcSubmission records.
 * Uses the DynamoDB Enhanced Client with a static TableSchema so the
 * shared model stays annotation-free.
 */
@Repository
public class ApiRepository {

    private static final TableSchema<AcSubmission> TABLE_SCHEMA =
            TableSchema.builder(AcSubmission.class)
                    .newItemSupplier(AcSubmission::new)
                    .addAttribute(String.class, a -> a.name("username")
                            .getter(AcSubmission::getUsername)
                            .setter(AcSubmission::setUsername)
                            .tags(StaticAttributeTags.primaryPartitionKey()))
                    .addAttribute(Long.class,   a -> a.name("timestamp")
                            .getter(AcSubmission::getTimestamp)
                            .setter(AcSubmission::setTimestamp)
                            .tags(StaticAttributeTags.primarySortKey()))
                    .addAttribute(String.class, a -> a.name("titleSlug")
                            .getter(AcSubmission::getTitleSlug)
                            .setter(AcSubmission::setTitleSlug))
                    .addAttribute(String.class, a -> a.name("title")
                            .getter(AcSubmission::getTitle)
                            .setter(AcSubmission::setTitle))
                    .build();

    private final DynamoDbTable<AcSubmission> table;

    public ApiRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("AcSubmissions", TABLE_SCHEMA);
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
