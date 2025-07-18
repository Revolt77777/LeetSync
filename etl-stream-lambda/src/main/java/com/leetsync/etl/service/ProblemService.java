package com.leetsync.etl.service;

import com.leetsync.shared.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;

public class ProblemService {
    
    private static final Logger log = LoggerFactory.getLogger(ProblemService.class);
    
    private static final TableSchema<Problem> TABLE_SCHEMA =
            TableSchema.builder(Problem.class)
                    .newItemSupplier(Problem::new)
                    .addAttribute(String.class, a -> a.name("titleSlug")
                            .getter(Problem::getTitleSlug)
                            .setter(Problem::setTitleSlug)
                            .tags(StaticAttributeTags.primaryPartitionKey()))
                    .addAttribute(Long.class, a -> a.name("questionId")
                            .getter(Problem::getQuestionId)
                            .setter(Problem::setQuestionId))
                    .addAttribute(Integer.class, a -> a.name("frontendQuestionId")
                            .getter(Problem::getFrontendQuestionId)
                            .setter(Problem::setFrontendQuestionId))
                    .addAttribute(Long.class, a -> a.name("totalAccepted")
                            .getter(Problem::getTotalAccepted)
                            .setter(Problem::setTotalAccepted))
                    .addAttribute(Long.class, a -> a.name("totalSubmitted")
                            .getter(Problem::getTotalSubmitted)
                            .setter(Problem::setTotalSubmitted))
                    .addAttribute(Integer.class, a -> a.name("difficultyLevel")
                            .getter(Problem::getDifficultyLevel)
                            .setter(Problem::setDifficultyLevel))
                    .addAttribute(Double.class, a -> a.name("progress")
                            .getter(Problem::getProgress)
                            .setter(Problem::setProgress))
                    .addAttribute(String.class, a -> a.name("difficulty")
                            .getter(Problem::getDifficulty)
                            .setter(Problem::setDifficulty))
                    .addAttribute(Double.class, a -> a.name("acRate")
                            .getter(Problem::getAcRate)
                            .setter(Problem::setAcRate))
                    .build();
    
    private final DynamoDbTable<Problem> problemTable;

    public ProblemService(DynamoDbEnhancedClient enhancedClient, String problemsTableName) {
        this.problemTable = enhancedClient.table(problemsTableName, TABLE_SCHEMA);
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