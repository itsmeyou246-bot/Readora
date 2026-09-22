package com.readora.readora.dto;

import java.util.ArrayList;
import java.util.List;

public class AuthorDashboardResponse {

    private String authorName;
    private String authorEmail;
    private int totalWorks;
    private int publishedWorks;
    private int pipelineWorks;
    private long totalReads;
    private double totalRoyalties;
    private double averageRating;
    private int totalReviews;

    private TopBookDto topBook;
    private List<WeeklyReadDto> weeklyReads = new ArrayList<>();
    private List<AuthorReviewDto> recentReviews = new ArrayList<>();
    private List<AuthorHighlightDto> recentHighlights = new ArrayList<>();
    private List<AuthorNoticeDto> notices = new ArrayList<>();

    public AuthorDashboardResponse() {
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public int getTotalWorks() {
        return totalWorks;
    }

    public void setTotalWorks(int totalWorks) {
        this.totalWorks = totalWorks;
    }

    public int getPublishedWorks() {
        return publishedWorks;
    }

    public void setPublishedWorks(int publishedWorks) {
        this.publishedWorks = publishedWorks;
    }

    public int getPipelineWorks() {
        return pipelineWorks;
    }

    public void setPipelineWorks(int pipelineWorks) {
        this.pipelineWorks = pipelineWorks;
    }

    public long getTotalReads() {
        return totalReads;
    }

    public void setTotalReads(long totalReads) {
        this.totalReads = totalReads;
    }

    public double getTotalRoyalties() {
        return totalRoyalties;
    }

    public void setTotalRoyalties(double totalRoyalties) {
        this.totalRoyalties = totalRoyalties;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public TopBookDto getTopBook() {
        return topBook;
    }

    public void setTopBook(TopBookDto topBook) {
        this.topBook = topBook;
    }

    public List<WeeklyReadDto> getWeeklyReads() {
        return weeklyReads;
    }

    public void setWeeklyReads(List<WeeklyReadDto> weeklyReads) {
        this.weeklyReads = weeklyReads;
    }

    public List<AuthorReviewDto> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<AuthorReviewDto> recentReviews) {
        this.recentReviews = recentReviews;
    }

    public List<AuthorHighlightDto> getRecentHighlights() {
        return recentHighlights;
    }

    public void setRecentHighlights(List<AuthorHighlightDto> recentHighlights) {
        this.recentHighlights = recentHighlights;
    }

    public List<AuthorNoticeDto> getNotices() {
        return notices;
    }

    public void setNotices(List<AuthorNoticeDto> notices) {
        this.notices = notices;
    }

    // ==========================================
    // NESTED DTOs
    // ==========================================

    public static class TopBookDto {
        private Long id;
        private String title;
        private String category;
        private String meta;
        private double rating;
        private int reviewCount;
        private long catalogReads;
        private double accruedRoyalties;
        private String image;

        public TopBookDto() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getMeta() {
            return meta;
        }

        public void setMeta(String meta) {
            this.meta = meta;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public int getReviewCount() {
            return reviewCount;
        }

        public void setReviewCount(int reviewCount) {
            this.reviewCount = reviewCount;
        }

        public long getCatalogReads() {
            return catalogReads;
        }

        public void setCatalogReads(long catalogReads) {
            this.catalogReads = catalogReads;
        }

        public double getAccruedRoyalties() {
            return accruedRoyalties;
        }

        public void setAccruedRoyalties(double accruedRoyalties) {
            this.accruedRoyalties = accruedRoyalties;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }

    public static class WeeklyReadDto {
        private String day;
        private long reads;

        public WeeklyReadDto() {
        }

        public WeeklyReadDto(String day, long reads) {
            this.day = day;
            this.reads = reads;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public long getReads() {
            return reads;
        }

        public void setReads(long reads) {
            this.reads = reads;
        }
    }

    public static class AuthorReviewDto {
        private Long id;
        private String initials;
        private String name;
        private String loc;
        private int stars;
        private String quote;
        private String book;
        private String time;

        public AuthorReviewDto() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getInitials() {
            return initials;
        }

        public void setInitials(String initials) {
            this.initials = initials;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLoc() {
            return loc;
        }

        public void setLoc(String loc) {
            this.loc = loc;
        }

        public int getStars() {
            return stars;
        }

        public void setStars(int stars) {
            this.stars = stars;
        }

        public String getQuote() {
            return quote;
        }

        public void setQuote(String quote) {
            this.quote = quote;
        }

        public String getBook() {
            return book;
        }

        public void setBook(String book) {
            this.book = book;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public static class AuthorHighlightDto {
        private Long id;
        private String book;
        private String count;
        private String quote;
        private String ref;

        public AuthorHighlightDto() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getBook() {
            return book;
        }

        public void setBook(String book) {
            this.book = book;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getQuote() {
            return quote;
        }

        public void setQuote(String quote) {
            this.quote = quote;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }
    }

    public static class AuthorNoticeDto {
        private Long id;
        private String text;
        private String time;

        public AuthorNoticeDto() {
        }

        public AuthorNoticeDto(Long id, String text, String time) {
            this.id = id;
            this.text = text;
            this.time = time;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
