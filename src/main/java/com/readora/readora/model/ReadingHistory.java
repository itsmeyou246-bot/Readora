package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "reading_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_history_user_book",
                        columnNames = {"user_id", "book_id"}
                )
        },
        indexes = {
                @Index(name = "idx_history_user", columnList = "user_id"),
                @Index(name = "idx_history_last_read", columnList = "last_read_at")
        }
)
public class ReadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "last_page", nullable = false)
    private Integer lastPage = 1;

    @Column(nullable = false, length = 30)
    private String status = "IN_PROGRESS";

    @Column(nullable = false)
    private Instant startedAt = Instant.now();

    @Column(nullable = false)
    private Instant lastReadAt = Instant.now();

    public ReadingHistory() {
    }

    public ReadingHistory(User user, Book book, Integer lastPage, String status) {
        this.user = user;
        this.book = book;
        this.lastPage = lastPage != null ? lastPage : 1;
        this.status = status != null ? status : "IN_PROGRESS";
        this.startedAt = Instant.now();
        this.lastReadAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getLastPage() {
        return lastPage;
    }

    public void setLastPage(Integer lastPage) {
        this.lastPage = lastPage;
        this.lastReadAt = Instant.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
