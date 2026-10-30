package com.kickmonitor.model;

public class ChatMessage {
    private String id;
    private String username;
    private String content;
    private String color;
    private boolean subscriber;
    private boolean moderator;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String id, String username, String content) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public boolean isSubscriber() { return subscriber; }
    public void setSubscriber(boolean subscriber) { this.subscriber = subscriber; }
    public boolean isModerator() { return moderator; }
    public void setModerator(boolean moderator) { this.moderator = moderator; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
