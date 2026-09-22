package com.readora.readora.dto;

import com.readora.readora.model.Book;

import java.time.Instant;

public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String description;
    private String category;
    private String language;
    private Double price;
    private String image;
    private String filePath;
    private boolean premium;
    private Integer pageCount;
    private String mood;
    private String type;
    private Double averageRating;
    private Integer ratingCount;
    private String status;
    private Instant createdAt;

    public BookResponse() {
    }

    public static BookResponse fromEntity(Book book) {
        BookResponse resp = new BookResponse();
        resp.setId(book.getId());
        resp.setTitle(book.getTitle());
        resp.setAuthor(book.getAuthor());
        resp.setDescription(book.getDescription());
        resp.setCategory(book.getCategory());
        resp.setLanguage(book.getLanguage());
        resp.setPrice(book.getPrice());
        resp.setImage(book.getImage());
        resp.setFilePath(book.getFilePath());
        resp.setPremium(book.isPremium());
        resp.setPageCount(book.getPageCount());
        resp.setMood(book.getMood());
        resp.setType(book.getType());
        resp.setAverageRating(book.getAverageRating());
        resp.setRatingCount(book.getRatingCount());
        resp.setStatus(book.getStatus());
        resp.setCreatedAt(book.getCreatedAt());
        return resp;
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

    public String getStatus() {
        return status != null ? status : "published";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
