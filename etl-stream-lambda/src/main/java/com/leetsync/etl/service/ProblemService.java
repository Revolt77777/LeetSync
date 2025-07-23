package com.leetsync.etl.service;

import com.leetsync.etl.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class ProblemService {
    
    private static final Logger log = LoggerFactory.getLogger(ProblemService.class);
    
    private final DynamoDbTable<Problem> problemTable;

    public ProblemService(DynamoDbEnhancedClient enhancedClient, String problemsTableName) {
        // Use annotated model directly - no complex static schema needed
        this.problemTable = enhancedClient.table(problemsTableName, TableSchema.fromBean(Problem.class));
    }

    public Problem getProblem(String titleSlug) {
        try {
            Key key = Key.builder().partitionValue(titleSlug).build();
            Problem problem = problemTable.getItem(key);
            
            if (problem == null) {
                log.warn("Problem not found for titleSlug: {}", titleSlug);
            }
            
            return problem;
            
        } catch (Exception e) {
            log.error("Error fetching problem for titleSlug {}: {}", titleSlug, e.getMessage());
            return null;
        }
    }
}