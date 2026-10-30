package com.kickmonitor.model;

public class SubscriptionEvent {
    private String username;
    private String type; // "NEW_SUB", "GIFTED", "RENEWAL"
    private int months;
    private int quantity;
    private long createdAt;

    public SubscriptionEvent() {}

    public SubscriptionEvent(String username, String type) {
        this.username = username;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getMonths() { return months; }
    public void setMonths(int months) { this.months = months; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
