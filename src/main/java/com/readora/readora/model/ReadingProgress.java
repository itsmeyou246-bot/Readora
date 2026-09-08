package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reading_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
public class ReadingProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Book book;

    @Column(nullable = false)
    private int currentPage = 1;

    @Column(nullable = false)
    private Instant lastReadAt = Instant.now();

    protected ReadingProgress() {
    }

    public ReadingProgress(User user, Book book, int currentPage) {
        this.user = user;
        this.book = book;
        this.currentPage = currentPage;
    }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        this.lastReadAt = Instant.now();
    }
}
