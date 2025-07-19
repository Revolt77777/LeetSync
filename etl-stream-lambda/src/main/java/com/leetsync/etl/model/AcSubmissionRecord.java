package com.leetsync.etl.model;

/**
 * Flattened record for Parquet output combining AcSubmission + Problem data
 */
public class AcSubmissionRecord {
    // From AcSubmission
    private String username;
    private String title;
    private String titleSlug;
    private Long timestamp;
    private Integer runtimeMs;
    private Double memoryMb;
    
    // From Problem (enrichment)
    private Integer difficultyLevel;
    private String[] tags;           // TopicTag names as array
    private Double acRate;
    private Long totalAccepted;
    private Long totalSubmitted;

    public AcSubmissionRecord() {}

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleSlug() { return titleSlug; }
    public void setTitleSlug(String titleSlug) { this.titleSlug = titleSlug; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public Integer getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(Integer difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public Double getAcRate() { return acRate; }
    public void setAcRate(Double acRate) { this.acRate = acRate; }

    public Long getTotalAccepted() { return totalAccepted; }
    public void setTotalAccepted(Long totalAccepted) { this.totalAccepted = totalAccepted; }

    public Long getTotalSubmitted() { return totalSubmitted; }
    public void setTotalSubmitted(Long totalSubmitted) { this.totalSubmitted = totalSubmitted; }

    public Integer getRuntimeMs() { return runtimeMs; }
    public void setRuntimeMs(Integer runtimeMs) { this.runtimeMs = runtimeMs; }

    public Double getMemoryMb() { return memoryMb; }
    public void setMemoryMb(Double memoryMb) { this.memoryMb = memoryMb; }
}