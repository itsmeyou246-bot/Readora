package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "books",
        indexes = {
                @Index(name = "idx_books_category", columnList = "category"),
                @Index(name = "idx_books_type", columnList = "type"),
                @Index(name = "idx_books_mood", columnList = "mood")
        }
)
public class Book {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String language;

    private Double price = 0.0;

    private String image;

    @Column(nullable = false)
    private String filePath;

    private boolean premium = false;

    private Integer pageCount = 0;

    private String mood = "Calm";

    private String type = "book";

    private Double averageRating = 0.0;

    private Integer ratingCount = 0;

    @Column(nullable = false, length = 30)
    private String status = "published";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private User authorUser;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Book() {
    }

    public Book(
            Long id,
            String title,
            String author,
            String filePath
    ) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Book(
            Long id,
            String title,
            String author,
            String description,
            String category,
            String language,
            Double price,
            String image,
            String filePath,
            boolean premium,
            Integer pageCount
    ) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.language = language;
        this.price = price != null ? price : 0.0;
        this.image = image;
        this.filePath = filePath;
        this.premium = premium;
        this.pageCount = pageCount != null ? pageCount : 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public User getAuthorUser() {
        return authorUser;
    }

    public void setAuthorUser(User authorUser) {
        this.authorUser = authorUser;
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

    public String getStatus() {
        return status != null ? status : "published";
    }

    public void setStatus(String status) {
        this.status = status;
    }
}