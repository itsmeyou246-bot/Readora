package com.readora.readora.dto;

import java.time.Instant;
import java.util.List;

public class AdminAnalyticsResponse {

    private long totalUsers;
    private long totalBooks;
    private long totalPurchases;
    private long totalSubscriptions;
    private List<DailyActivityDto> readingActivity;
    private List<ContentShareDto> contentDistribution;
    private List<PopularBookDto> popularContent;
    private List<ActivityEventDto> recentActivity;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long v) { this.totalUsers = v; }
    public long getTotalBooks() { return totalBooks; }
    public void setTotalBooks(long v) { this.totalBooks = v; }
    public long getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(long v) { this.totalPurchases = v; }
    public long getTotalSubscriptions() { return totalSubscriptions; }
    public void setTotalSubscriptions(long v) { this.totalSubscriptions = v; }
    public List<DailyActivityDto> getReadingActivity() { return readingActivity; }
    public void setReadingActivity(List<DailyActivityDto> v) { this.readingActivity = v; }
    public List<ContentShareDto> getContentDistribution() { return contentDistribution; }
    public void setContentDistribution(List<ContentShareDto> v) { this.contentDistribution = v; }
    public List<PopularBookDto> getPopularContent() { return popularContent; }
    public void setPopularContent(List<PopularBookDto> v) { this.popularContent = v; }
    public List<ActivityEventDto> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<ActivityEventDto> v) { this.recentActivity = v; }

    public static class DailyActivityDto {
        private String label;
        private long pagesRead;
        private long activeReaders;
        public DailyActivityDto() {}
        public DailyActivityDto(String label, long pagesRead, long activeReaders) {
            this.label = label; this.pagesRead = pagesRead; this.activeReaders = activeReaders;
        }
        public String getLabel() { return label; }
        public void setLabel(String v) { this.label = v; }
        public long getPagesRead() { return pagesRead; }
        public void setPagesRead(long v) { this.pagesRead = v; }
        public long getActiveReaders() { return activeReaders; }
        public void setActiveReaders(long v) { this.activeReaders = v; }
    }

    public static class ContentShareDto {
        private String label;
        private long count;
        private double percent;
        public ContentShareDto() {}
        public ContentShareDto(String label, long count, double percent) {
            this.label = label; this.count = count; this.percent = percent;
        }
        public String getLabel() { return label; }
        public void setLabel(String v) { this.label = v; }
        public long getCount() { return count; }
        public void setCount(long v) { this.count = v; }
        public double getPercent() { return percent; }
        public void setPercent(double v) { this.percent = v; }
    }

    public static class PopularBookDto {
        private String title;
        private String author;
        private String category;
        private long readers;
        private double rating;
        private double completionRate;
        public PopularBookDto() {}
        public PopularBookDto(String title, String author, String category,
                              long readers, double rating, double completionRate) {
            this.title = title; this.author = author; this.category = category;
            this.readers = readers; this.rating = rating; this.completionRate = completionRate;
        }
        public String getTitle() { return title; }
        public void setTitle(String v) { this.title = v; }
        public String getAuthor() { return author; }
        public void setAuthor(String v) { this.author = v; }
        public String getCategory() { return category; }
        public void setCategory(String v) { this.category = v; }
        public long getReaders() { return readers; }
        public void setReaders(long v) { this.readers = v; }
        public double getRating() { return rating; }
        public void setRating(double v) { this.rating = v; }
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double v) { this.completionRate = v; }
    }

    public static class ActivityEventDto {
        private String icon;
        private String text;
        private String subtext;
        private Instant timestamp;
        private String timeAgo;
        public ActivityEventDto() {}
        public ActivityEventDto(String icon, String text, String subtext, Instant timestamp, String timeAgo) {
            this.icon = icon; this.text = text; this.subtext = subtext;
            this.timestamp = timestamp; this.timeAgo = timeAgo;
        }
        public String getIcon() { return icon; }
        public void setIcon(String v) { this.icon = v; }
        public String getText() { return text; }
        public void setText(String v) { this.text = v; }
        public String getSubtext() { return subtext; }
        public void setSubtext(String v) { this.subtext = v; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant v) { this.timestamp = v; }
        public String getTimeAgo() { return timeAgo; }
        public void setTimeAgo(String v) { this.timeAgo = v; }
    }
}