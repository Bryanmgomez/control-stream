package com.kickmonitor.model;

public class FollowerEvent {
    private String username;
    private long followedAt;

    public FollowerEvent() {}

    public FollowerEvent(String username) {
        this.username = username;
        this.followedAt = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public long getFollowedAt() { return followedAt; }
    public void setFollowedAt(long followedAt) { this.followedAt = followedAt; }
}
