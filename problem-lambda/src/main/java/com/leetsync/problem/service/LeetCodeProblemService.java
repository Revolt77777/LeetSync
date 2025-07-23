package com.leetsync.problem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetsync.problem.model.LeetCodeProblemsResponse;
import com.leetsync.problem.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LeetCodeProblemService {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeProblemService.class);
    private static final String PROBLEMS_API_URL = "https://leetcode.com/api/problems/all/";
    private static final String GRAPHQL_API_URL = "https://leetcode.com/graphql";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LeetCodeProblemService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<Problem> fetchAllProblems() throws IOException, InterruptedException {
        log.info("Fetching all problems from LeetCode API");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROBLEMS_API_URL))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch problems. Status: " + response.statusCode());
        }

        LeetCodeProblemsResponse problemsResponse = objectMapper.readValue(response.body(), LeetCodeProblemsResponse.class);
        
        log.info("Successfully fetched {} total problems", problemsResponse.getNumTotal());
        
        return convertToProblems(problemsResponse);
    }

    private List<Problem> convertToProblems(LeetCodeProblemsResponse response) {
        List<Problem> problems = new ArrayList<>();
        
        for (LeetCodeProblemsResponse.StatStatusPair pair : response.getStatStatusPairs()) {
            LeetCodeProblemsResponse.Stat stat = pair.getStat();
            
            Problem problem = new Problem(
                    stat.getQuestionId(),
                    stat.getFrontendQuestionId(),
                    stat.getQuestionTitleSlug(),
                    stat.getTotalAcs(),
                    stat.getTotalSubmitted(),
                    pair.getDifficulty() != null ? pair.getDifficulty().getLevel() : null
            );
            
            problems.add(problem);
        }
        
        log.info("Converted {} problems for processing", problems.size());
        return problems;
    }

    public List<Problem.TopicTag> fetchTopicTags(String titleSlug) throws IOException, InterruptedException {
        log.info("Fetching topic tags for problem: {}", titleSlug);
        
        String query = "query getQuestionDetail($titleSlug: String!) { " +
                "question(titleSlug: $titleSlug) { " +
                "topicTags { name slug } " +
                "} " +
                "}";
        
        String requestBody = String.format(
                "{\"query\": \"%s\", \"variables\": {\"titleSlug\": \"%s\"}}",
                query.replace("\"", "\\\""),
                titleSlug
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GRAPHQL_API_URL))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch topic tags. Status: " + response.statusCode());
        }

        LeetCodeProblemsResponse.TopicTagsResponse topicTagsResponse = 
                objectMapper.readValue(response.body(), LeetCodeProblemsResponse.TopicTagsResponse.class);
        
        List<Problem.TopicTag> topicTags = new ArrayList<>();
        if (topicTagsResponse.getData() != null && 
            topicTagsResponse.getData().getQuestion() != null && 
            topicTagsResponse.getData().getQuestion().getTopicTags() != null) {
            
            for (LeetCodeProblemsResponse.TopicTag tag : topicTagsResponse.getData().getQuestion().getTopicTags()) {
                topicTags.add(new Problem.TopicTag(tag.getName(), tag.getSlug()));
            }
        }
        
        log.info("Successfully fetched {} topic tags for problem: {}", topicTags.size(), titleSlug);
        return topicTags;
    }
}