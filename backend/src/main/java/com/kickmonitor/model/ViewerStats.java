package com.kickmonitor.model;

public class ViewerStats {
    private int current;
    private int max;
    private long updatedAt;

    public ViewerStats() {}

    public ViewerStats(int current, int max) {
        this.current = current;
        this.max = max;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
    public int getMax() { return max; }
    public void setMax(int max) { this.max = max; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
