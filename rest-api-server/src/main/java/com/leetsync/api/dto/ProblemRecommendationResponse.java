package com.leetsync.api.dto;

import java.util.List;

public class ProblemRecommendationResponse {
    private int problemId;
    private String title;
    private String difficulty;
    private List<String> tags;
    private String reason;
    private double matchScore;

    public ProblemRecommendationResponse() {}

    public ProblemRecommendationResponse(int problemId, String title, String difficulty, List<String> tags, String reason, double matchScore) {
        this.problemId = problemId;
        this.title = title;
        this.difficulty = difficulty;
        this.tags = tags;
        this.reason = reason;
        this.matchScore = matchScore;
    }

    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
}