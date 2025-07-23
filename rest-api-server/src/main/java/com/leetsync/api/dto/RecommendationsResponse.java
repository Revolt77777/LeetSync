package com.leetsync.api.dto;

import java.util.List;

public class RecommendationsResponse {
    private String username;
    private String date;
    private List<TagRecommendationResponse> tagRecommendations;
    private List<ProblemRecommendationResponse> problemRecommendations;
    private long generatedAt;

    public RecommendationsResponse() {}

    public RecommendationsResponse(String username, String date, List<TagRecommendationResponse> tagRecommendations,
                                 List<ProblemRecommendationResponse> problemRecommendations, long generatedAt) {
        this.username = username;
        this.date = date;
        this.tagRecommendations = tagRecommendations;
        this.problemRecommendations = problemRecommendations;
        this.generatedAt = generatedAt;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<TagRecommendationResponse> getTagRecommendations() { return tagRecommendations; }
    public void setTagRecommendations(List<TagRecommendationResponse> tagRecommendations) { this.tagRecommendations = tagRecommendations; }

    public List<ProblemRecommendationResponse> getProblemRecommendations() { return problemRecommendations; }
    public void setProblemRecommendations(List<ProblemRecommendationResponse> problemRecommendations) { this.problemRecommendations = problemRecommendations; }

    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }
}