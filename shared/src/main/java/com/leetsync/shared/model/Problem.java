package com.leetsync.shared.model;

public class Problem {
    
    private Long questionId;
    private Integer frontendQuestionId;
    private String titleSlug;
    private Long totalAccepted;
    private Long totalSubmitted;
    private Integer difficultyLevel;
    
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

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Double getAcRate() { return acRate; }
    public void setAcRate(Double acRate) { this.acRate = acRate; }
    
    public java.util.List<TopicTag> getTopicTags() { return topicTags; }
    public void setTopicTags(java.util.List<TopicTag> topicTags) { this.topicTags = topicTags; }
}