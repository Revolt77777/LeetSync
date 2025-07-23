package com.leetsync.recommendation.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@DynamoDbBean
public class ProblemRecommendation {
    private int problemId;
    private String title;
    private String difficulty;
    private List<String> tags;
    private String reason;
    private double matchScore;

    public ProblemRecommendation() {}

    public ProblemRecommendation(int problemId, String title, String difficulty, List<String> tags, String reason, double matchScore) {
        this.problemId = problemId;
        this.title = title;
        this.difficulty = difficulty;
        this.tags = tags;
        this.reason = reason;
        this.matchScore = matchScore;
    }

    @DynamoDbAttribute("problemId")
    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    @DynamoDbAttribute("title")
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @DynamoDbAttribute("difficulty")
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    @DynamoDbAttribute("tags")
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @DynamoDbAttribute("reason")
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @DynamoDbAttribute("matchScore")
    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
}