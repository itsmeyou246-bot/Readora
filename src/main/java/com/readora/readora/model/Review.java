package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_book",
                        columnNames = {"user_id", "book_id"}
                )
        },
        indexes = {
                @Index(name = "idx_review_book", columnList = "book_id"),
                @Index(name = "idx_review_user", columnList = "user_id")
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer rating = 5;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Review() {
    }

    public Review(User user, Book book, Integer rating, String comment) {
        this.user = user;
        this.book = book;
        this.rating = rating != null ? Math.max(1, Math.min(5, rating)) : 5;
        this.comment = comment;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating != null ? Math.max(1, Math.min(5, rating)) : 5;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
