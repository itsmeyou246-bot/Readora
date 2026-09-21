package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(
        name = "reading_analytics",
        indexes = {
                @Index(name = "idx_analytics_user", columnList = "userId")
        }
)
public class ReadingAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long bookId;

    private Integer pagesRead = 0;

    private Integer readingMinutes = 0;

    private Integer booksCompleted = 0;

    private Integer streakDays = 0;

    private Integer longestStreak = 0;

    private LocalDate lastReadDate;

    private Instant updatedAt = Instant.now();

    public ReadingAnalytics() {
    }

    public ReadingAnalytics(Long userId) {
        this.userId = userId;
        this.pagesRead = 0;
        this.readingMinutes = 0;
        this.booksCompleted = 0;
        this.streakDays = 0;
        this.longestStreak = 0;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getPagesRead() {
        return pagesRead != null ? pagesRead : 0;
    }

    public void setPagesRead(Integer pagesRead) {
        this.pagesRead = pagesRead;
    }

    public Integer getReadingMinutes() {
        return readingMinutes != null ? readingMinutes : 0;
    }

    public void setReadingMinutes(Integer readingMinutes) {
        this.readingMinutes = readingMinutes;
    }

    public Integer getBooksCompleted() {
        return booksCompleted != null ? booksCompleted : 0;
    }

    public void setBooksCompleted(Integer booksCompleted) {
        this.booksCompleted = booksCompleted;
    }

    public Integer getStreakDays() {
        return streakDays != null ? streakDays : 0;
    }

    public void setStreakDays(Integer streakDays) {
        this.streakDays = streakDays;
    }

    public Integer getLongestStreak() {
        return longestStreak != null ? longestStreak : 0;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(LocalDate lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}