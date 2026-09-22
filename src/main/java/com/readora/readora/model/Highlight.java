package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "highlights",
        indexes = {
                @Index(name = "idx_highlight_user", columnList = "user_id"),
                @Index(name = "idx_highlight_book", columnList = "book_id")
        }
)
public class Highlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    private String color = "yellow";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Highlight() {
    }

    public Highlight(User user, Book book, Integer pageNumber, String text, String color) {
        this.user = user;
        this.book = book;
        this.pageNumber = pageNumber;
        this.text = text;
        this.color = color != null ? color : "yellow";
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

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
