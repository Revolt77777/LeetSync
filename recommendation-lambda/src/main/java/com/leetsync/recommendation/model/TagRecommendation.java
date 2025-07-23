package com.leetsync.recommendation.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class TagRecommendation {
    private String tag;
    private String reason;
    private double confidenceScore;
    private int estimatedDifficulty;

    public TagRecommendation() {}

    public TagRecommendation(String tag, String reason, double confidenceScore, int estimatedDifficulty) {
        this.tag = tag;
        this.reason = reason;
        this.confidenceScore = confidenceScore;
        this.estimatedDifficulty = estimatedDifficulty;
    }

    @DynamoDbAttribute("tag")
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    @DynamoDbAttribute("reason")
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @DynamoDbAttribute("confidenceScore")
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    @DynamoDbAttribute("estimatedDifficulty")
    public int getEstimatedDifficulty() { return estimatedDifficulty; }
    public void setEstimatedDifficulty(int estimatedDifficulty) { this.estimatedDifficulty = estimatedDifficulty; }
}