package com.leetsync.recommendation.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.List;

@DynamoDbBean
public class UserRecommendations {
    private String username;
    private String date;
    private List<TagRecommendation> tagRecommendations;
    private List<ProblemRecommendation> problemRecommendations;
    private long generatedAt;
    private long ttl;

    public UserRecommendations() {}

    public UserRecommendations(String username, String date, List<TagRecommendation> tagRecommendations,
                             List<ProblemRecommendation> problemRecommendations, long generatedAt, long ttl) {
        this.username = username;
        this.date = date;
        this.tagRecommendations = tagRecommendations;
        this.problemRecommendations = problemRecommendations;
        this.generatedAt = generatedAt;
        this.ttl = ttl;
    }

    @DynamoDbPartitionKey
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @DynamoDbSortKey
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @DynamoDbAttribute("tagRecommendations")
    public List<TagRecommendation> getTagRecommendations() { return tagRecommendations; }
    public void setTagRecommendations(List<TagRecommendation> tagRecommendations) { this.tagRecommendations = tagRecommendations; }

    @DynamoDbAttribute("problemRecommendations")
    public List<ProblemRecommendation> getProblemRecommendations() { return problemRecommendations; }
    public void setProblemRecommendations(List<ProblemRecommendation> problemRecommendations) { this.problemRecommendations = problemRecommendations; }

    @DynamoDbAttribute("generatedAt")
    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }

    @DynamoDbAttribute("ttl")
    public long getTtl() { return ttl; }
    public void setTtl(long ttl) { this.ttl = ttl; }
}