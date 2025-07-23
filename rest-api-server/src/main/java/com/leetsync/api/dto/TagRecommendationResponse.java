package com.leetsync.api.dto;

public class TagRecommendationResponse {
    private String tag;
    private String reason;
    private double confidenceScore;
    private int estimatedDifficulty;

    public TagRecommendationResponse() {}

    public TagRecommendationResponse(String tag, String reason, double confidenceScore, int estimatedDifficulty) {
        this.tag = tag;
        this.reason = reason;
        this.confidenceScore = confidenceScore;
        this.estimatedDifficulty = estimatedDifficulty;
    }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public int getEstimatedDifficulty() { return estimatedDifficulty; }
    public void setEstimatedDifficulty(int estimatedDifficulty) { this.estimatedDifficulty = estimatedDifficulty; }
}