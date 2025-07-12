package com.revolt7.leetsync.model;

public class AcSubmission {
    private long id;
    private String title;
    private long timestamp;

    public AcSubmission() {}

    public AcSubmission(long id, String title, long timestamp) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AcSubmission{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
