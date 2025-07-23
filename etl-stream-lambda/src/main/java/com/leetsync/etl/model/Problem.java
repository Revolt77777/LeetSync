package com.leetsync.etl.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;

/**
 * Problem model for ETL service with DynamoDB annotations
 * This service owns this model and can evolve it independently
 * Optimized for ETL data enrichment needs
 */
@DynamoDbBean
public class Problem {
    
    private Long questionId;
    private Integer frontendQuestionId;
    private String titleSlug;
    private Long totalAccepted;
    private Long totalSubmitted;
    private Integer difficultyLevel;
    private String difficulty;
    private Double acRate;
    private List<TopicTag> topicTags;
    
    @DynamoDbBean
    public static class TopicTag {
        private String name;
        private String slug;
        
        public TopicTag() {}
        
        public TopicTag(String name, String slug) {
            this.name = name;
            this.slug = slug;
        }
        
        @DynamoDbAttribute("name")
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        @DynamoDbAttribute("slug")
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }
    
    public Problem() {}
    
    public Problem(Long questionId, Integer frontendQuestionId, String titleSlug, 
                   Long totalAccepted, Long totalSubmitted, Integer difficultyLevel) {
        this.questionId = questionId;
        this.frontendQuestionId = frontendQuestionId;
        this.titleSlug = titleSlug;
        this.totalAccepted = totalAccepted;
        this.totalSubmitted = totalSubmitted;
        this.difficultyLevel = difficultyLevel;
        this.difficulty = mapDifficultyToString(difficultyLevel);
        this.acRate = calculateAcRate(totalAccepted, totalSubmitted);
    }
    
    private String mapDifficultyToString(Integer level) {
        if (level == null) return null;
        return switch (level) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            case 3 -> "Hard";
            default -> "Unknown";
        };
    }
    
    private Double calculateAcRate(Long accepted, Long submitted) {
        if (accepted == null || submitted == null || submitted == 0) return null;
        return (double) accepted / submitted * 100;
    }
    
    @DynamoDbAttribute("questionId")
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    
    @DynamoDbAttribute("frontendQuestionId")
    public Integer getFrontendQuestionId() { return frontendQuestionId; }
    public void setFrontendQuestionId(Integer frontendQuestionId) { this.frontendQuestionId = frontendQuestionId; }
    
    @DynamoDbPartitionKey
    public String getTitleSlug() { return titleSlug; }
    public void setTitleSlug(String titleSlug) { this.titleSlug = titleSlug; }
    
    @DynamoDbAttribute("totalAccepted")
    public Long getTotalAccepted() { return totalAccepted; }
    public void setTotalAccepted(Long totalAccepted) { 
        this.totalAccepted = totalAccepted;
        this.acRate = calculateAcRate(totalAccepted, this.totalSubmitted);
    }
    
    @DynamoDbAttribute("totalSubmitted")
    public Long getTotalSubmitted() { return totalSubmitted; }
    public void setTotalSubmitted(Long totalSubmitted) { 
        this.totalSubmitted = totalSubmitted;
        this.acRate = calculateAcRate(this.totalAccepted, totalSubmitted);
    }
    
    @DynamoDbAttribute("difficultyLevel")
    public Integer getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(Integer difficultyLevel) { 
        this.difficultyLevel = difficultyLevel;
        this.difficulty = mapDifficultyToString(difficultyLevel);
    }

    @DynamoDbAttribute("difficulty")
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    @DynamoDbAttribute("acRate")
    public Double getAcRate() { return acRate; }
    public void setAcRate(Double acRate) { this.acRate = acRate; }
    
    @DynamoDbAttribute("topicTags")
    public List<TopicTag> getTopicTags() { return topicTags; }
    public void setTopicTags(List<TopicTag> topicTags) { this.topicTags = topicTags; }
}