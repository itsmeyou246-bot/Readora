package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorite_user_book",
                        columnNames = {"user_id", "book_id"}
                )
        },
        indexes = {
                @Index(name = "idx_favorite_user", columnList = "user_id"),
                @Index(name = "idx_favorite_book", columnList = "book_id")
        }
)
public class Favorite {

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
    private Instant createdAt = Instant.now();

    public Favorite() {
    }

    public Favorite(User user, Book book) {
        this.user = user;
        this.book = book;
        this.createdAt = Instant.now();
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
