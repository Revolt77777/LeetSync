package com.leetsync.problem.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.leetsync.shared.model.Problem;
import com.leetsync.problem.service.LeetCodeProblemService;
import com.leetsync.problem.service.ProblemDynamoService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProblemScanHandler implements RequestHandler<Void, String> {

    private final LeetCodeProblemService leetCodeProblemService;
    private final ProblemDynamoService problemDynamoService;

    public ProblemScanHandler() {
        this.leetCodeProblemService = new LeetCodeProblemService();
        this.problemDynamoService = new ProblemDynamoService(
                DynamoDbClient.create(), 
                System.getenv("PROBLEMS_TABLE_NAME")
        );
    }

    @Override
    public String handleRequest(Void input, Context context) {
        try {
            // 1. Fetch all problems from LeetCode
            List<Problem> allProblems = leetCodeProblemService.fetchAllProblems();
            context.getLogger().log("Fetched " + allProblems.size() + " problems from LeetCode");
            
            // 2. Batch check existing problems in DynamoDB
            Set<String> existingTitleSlugs = problemDynamoService.getExistingTitleSlugs(
                allProblems.stream().map(Problem::getTitleSlug).collect(Collectors.toSet())
            );
            context.getLogger().log("Found " + existingTitleSlugs.size() + " existing problems in database");
            
            // 3. Filter to only NEW problems
            List<Problem> newProblems = allProblems.stream()
                .filter(p -> !existingTitleSlugs.contains(p.getTitleSlug()))
                .collect(Collectors.toList());
            context.getLogger().log("Identified " + newProblems.size() + " new problems");
            
            // 4. For each new problem, fetch topicTags and store
            int storedCount = 0;
            for (Problem problem : newProblems) {
                try {
                    // Fetch topicTags for this problem
                    List<Problem.TopicTag> topicTags = leetCodeProblemService.fetchTopicTags(problem.getTitleSlug());
                    problem.setTopicTags(topicTags);
                    
                    // Store the problem
                    problemDynamoService.storeProblem(problem);
                    storedCount++;
                    
                    context.getLogger().log("Stored problem: " + problem.getTitleSlug() + " with " + topicTags.size() + " topic tags");
                } catch (Exception e) {
                    context.getLogger().log("Failed to process problem " + problem.getTitleSlug() + ": " + e.getMessage());
                }
            }
            
            String result = String.format("Problem scan completed. Total: %d, Existing: %d, New: %d, Stored: %d", 
                    allProblems.size(), existingTitleSlugs.size(), newProblems.size(), storedCount);
            
            context.getLogger().log(result);
            return result;
            
        } catch (IOException | InterruptedException e) {
            String error = "Failed to scan problems: " + e.getMessage();
            context.getLogger().log(error);
            throw new RuntimeException(error, e);
        }
    }
}