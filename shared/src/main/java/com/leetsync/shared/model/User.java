package com.leetsync.shared.model;

public class User {
    private String username;
    private long createdAt;
    private long lastSync;

    public User() {}

    public User(String username, long createdAt, long lastSync) {
        this.username = username;
        this.createdAt = createdAt;
        this.lastSync = lastSync;
    }

    public String getUsername() {
        return username;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastSync() {
        return lastSync;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", lastSync=" + lastSync +
                '}';
    }
}