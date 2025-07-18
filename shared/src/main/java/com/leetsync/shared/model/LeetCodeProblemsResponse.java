package com.leetsync.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeetCodeProblemsResponse {
    
    @JsonProperty("num_total")
    private Integer numTotal;
    
    @JsonProperty("stat_status_pairs")
    private List<StatStatusPair> statStatusPairs;
    
    public LeetCodeProblemsResponse() {}
    
    public Integer getNumTotal() { return numTotal; }
    public void setNumTotal(Integer numTotal) { this.numTotal = numTotal; }
    
    public List<StatStatusPair> getStatStatusPairs() { return statStatusPairs; }
    public void setStatStatusPairs(List<StatStatusPair> statStatusPairs) { this.statStatusPairs = statStatusPairs; }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatStatusPair {
        private Stat stat;
        private Difficulty difficulty;
        
        @JsonProperty("paid_only")
        private Boolean paidOnly;
        
        private Double frequency;
        
        public Stat getStat() { return stat; }
        public void setStat(Stat stat) { this.stat = stat; }
        
        public Difficulty getDifficulty() { return difficulty; }
        public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
        
        public Boolean getPaidOnly() { return paidOnly; }
        public void setPaidOnly(Boolean paidOnly) { this.paidOnly = paidOnly; }
        
        public Double getFrequency() { return frequency; }
        public void setFrequency(Double frequency) { this.frequency = frequency; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stat {
        @JsonProperty("question_id")
        private Long questionId;
        
        @JsonProperty("frontend_question_id")
        private Integer frontendQuestionId;
        
        @JsonProperty("question__title")
        private String questionTitle;
        
        @JsonProperty("question__title_slug")
        private String questionTitleSlug;
        
        @JsonProperty("question__hide")
        private Boolean questionHide;
        
        @JsonProperty("total_acs")
        private Long totalAcs;
        
        @JsonProperty("total_submitted")
        private Long totalSubmitted;
        
        @JsonProperty("is_new_question")
        private Boolean isNewQuestion;
        
        // Getters and setters
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        
        public Integer getFrontendQuestionId() { return frontendQuestionId; }
        public void setFrontendQuestionId(Integer frontendQuestionId) { this.frontendQuestionId = frontendQuestionId; }
        
        public String getQuestionTitle() { return questionTitle; }
        public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
        
        public String getQuestionTitleSlug() { return questionTitleSlug; }
        public void setQuestionTitleSlug(String questionTitleSlug) { this.questionTitleSlug = questionTitleSlug; }
        
        public Boolean getQuestionHide() { return questionHide; }
        public void setQuestionHide(Boolean questionHide) { this.questionHide = questionHide; }
        
        public Long getTotalAcs() { return totalAcs; }
        public void setTotalAcs(Long totalAcs) { this.totalAcs = totalAcs; }
        
        public Long getTotalSubmitted() { return totalSubmitted; }
        public void setTotalSubmitted(Long totalSubmitted) { this.totalSubmitted = totalSubmitted; }
        
        public Boolean getIsNewQuestion() { return isNewQuestion; }
        public void setIsNewQuestion(Boolean isNewQuestion) { this.isNewQuestion = isNewQuestion; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Difficulty {
        private Integer level;
        
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
    }
    
    // For topicTags GraphQL query response
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopicTagsResponse {
        @JsonProperty("data")
        private TopicTagsData data;
        
        public TopicTagsData getData() { return data; }
        public void setData(TopicTagsData data) { this.data = data; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopicTagsData {
        @JsonProperty("question")
        private QuestionWithTags question;
        
        public QuestionWithTags getQuestion() { return question; }
        public void setQuestion(QuestionWithTags question) { this.question = question; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionWithTags {
        @JsonProperty("topicTags")
        private List<TopicTag> topicTags;
        
        public List<TopicTag> getTopicTags() { return topicTags; }
        public void setTopicTags(List<TopicTag> topicTags) { this.topicTags = topicTags; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopicTag {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("slug")
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
}