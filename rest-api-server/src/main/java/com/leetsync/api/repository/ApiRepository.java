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
                    .addAttribute(Long.class,   a -> a.name("id")
                            .getter(AcSubmission::getId)
                            .setter(AcSubmission::setId)
                            .tags(StaticAttributeTags.primaryPartitionKey()))
                    .addAttribute(String.class, a -> a.name("title")
                            .getter(AcSubmission::getTitle)
                            .setter(AcSubmission::setTitle))
                    .addAttribute(String.class, a -> a.name("titleSlug")
                            .getter(AcSubmission::getTitleSlug)
                            .setter(AcSubmission::setTitleSlug))
                    .addAttribute(Long.class,   a -> a.name("timestamp")
                            .getter(AcSubmission::getTimestamp)
                            .setter(AcSubmission::setTimestamp))
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

    public List<AcSubmission> findByProblemId(long problemId) {
        QueryConditional cond = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(problemId).build());
        return table.query(r -> r.queryConditional(cond))
                .items().stream().toList();
    }
}
