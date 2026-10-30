package com.kickmonitor.model;

import java.time.Instant;

public class StreamEvent {
    private String type;
    private Instant timestamp;
    private Object payload;

    public StreamEvent() {}

    public StreamEvent(String type, Object payload) {
        this.type = type;
        this.timestamp = Instant.now();
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}
