package com.kickmonitor.model;

public class DonationEvent {
    private String username;
    private int amount;
    private String currency; // "KICKs"
    private String message;
    private long createdAt;

    public DonationEvent() {}

    public DonationEvent(String username, int amount) {
        this.username = username;
        this.amount = amount;
        this.currency = "KICKs";
        this.createdAt = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
