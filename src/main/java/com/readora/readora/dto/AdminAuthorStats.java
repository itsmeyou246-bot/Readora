package com.readora.readora.dto;

public class AdminAuthorStats {

    private long totalAuthors;
    private long activeAuthors;
    private long publishedContentCount;

    public AdminAuthorStats() {
    }

    public AdminAuthorStats(long totalAuthors, long activeAuthors, long publishedContentCount) {
        this.totalAuthors = totalAuthors;
        this.activeAuthors = activeAuthors;
        this.publishedContentCount = publishedContentCount;
    }

    public long getTotalAuthors() { return totalAuthors; }
    public void setTotalAuthors(long totalAuthors) { this.totalAuthors = totalAuthors; }

    public long getActiveAuthors() { return activeAuthors; }
    public void setActiveAuthors(long activeAuthors) { this.activeAuthors = activeAuthors; }

    public long getPublishedContentCount() { return publishedContentCount; }
    public void setPublishedContentCount(long publishedContentCount) { this.publishedContentCount = publishedContentCount; }
}