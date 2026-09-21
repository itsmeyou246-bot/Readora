package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "purchases", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Book book;

    @Column(nullable = false)
    private Instant purchasedAt = Instant.now();

    protected Purchase() {
    }

    public Purchase(User user, Book book) {
        this.user = user;
        this.book = book;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public Instant getPurchasedAt() { return purchasedAt; }
}
