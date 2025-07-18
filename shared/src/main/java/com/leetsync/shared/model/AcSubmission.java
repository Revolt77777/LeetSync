package com.leetsync.shared.model;

public class AcSubmission {
    private String username;
    private String title;
    private String titleSlug;
    private long timestamp;

    public AcSubmission() {}

    public AcSubmission(String username, String title, String titleSlug, long timestamp) {
        this.username = username;
        this.title = title;
        this.titleSlug = titleSlug;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleSlug() {
        return titleSlug;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AcSubmission{" +
                "username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", titleSlug='" + titleSlug + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
