package com.leetsync.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Problem {
    
    @JsonProperty("question_id")
    private Long questionId;
    
    @JsonProperty("frontend_question_id") 
    private Integer frontendQuestionId;
    
    @JsonProperty("question__title_slug")
    private String titleSlug;
    
    @JsonProperty("total_acs")
    private Long totalAccepted;
    
    @JsonProperty("total_submitted")
    private Long totalSubmitted;
    
    @JsonProperty("difficulty")
    private Integer difficultyLevel;
    
    @JsonProperty("frequency")
    private Double progress;
    
    private String difficulty;
    private Double acRate;
    private java.util.List<TopicTag> topicTags;
    
    public static class TopicTag {
        private String name;
        private String slug;
        
        public TopicTag() {}
        
        public TopicTag(String name, String slug) {
            this.name = name;
            this.slug = slug;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }
    
    public Problem() {}
    
    public Problem(Long questionId, Integer frontendQuestionId, String titleSlug, 
                   Long totalAccepted, Long totalSubmitted, Integer difficultyLevel, Double progress) {
        this.questionId = questionId;
        this.frontendQuestionId = frontendQuestionId;
        this.titleSlug = titleSlug;
        this.totalAccepted = totalAccepted;
        this.totalSubmitted = totalSubmitted;
        this.difficultyLevel = difficultyLevel;
        this.progress = progress;
        this.difficulty = mapDifficultyToString(difficultyLevel);
        this.acRate = calculateAcRate(totalAccepted, totalSubmitted);
    }
    
    private String mapDifficultyToString(Integer level) {
        if (level == null) return null;
        switch (level) {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            default: return "Unknown";
        }
    }
    
    private Double calculateAcRate(Long accepted, Long submitted) {
        if (accepted == null || submitted == null || submitted == 0) return 0.0;
        return (double) accepted / submitted * 100;
    }

    // Getters and setters
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    
    public Integer getFrontendQuestionId() { return frontendQuestionId; }
    public void setFrontendQuestionId(Integer frontendQuestionId) { this.frontendQuestionId = frontendQuestionId; }
    
    public String getTitleSlug() { return titleSlug; }
    public void setTitleSlug(String titleSlug) { this.titleSlug = titleSlug; }
    
    public Long getTotalAccepted() { return totalAccepted; }
    public void setTotalAccepted(Long totalAccepted) { 
        this.totalAccepted = totalAccepted;
        this.acRate = calculateAcRate(totalAccepted, this.totalSubmitted);
    }
    
    public Long getTotalSubmitted() { return totalSubmitted; }
    public void setTotalSubmitted(Long totalSubmitted) { 
        this.totalSubmitted = totalSubmitted;
        this.acRate = calculateAcRate(this.totalAccepted, totalSubmitted);
    }
    
    public Integer getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(Integer difficultyLevel) { 
        this.difficultyLevel = difficultyLevel;
        this.difficulty = mapDifficultyToString(difficultyLevel);
    }
    
    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Double getAcRate() { return acRate; }
    public void setAcRate(Double acRate) { this.acRate = acRate; }
    
    public java.util.List<TopicTag> getTopicTags() { return topicTags; }
    public void setTopicTags(java.util.List<TopicTag> topicTags) { this.topicTags = topicTags; }
}