package com.readora.readora.dto;

import com.readora.readora.model.Review;
import java.time.Instant;

public class ReviewResponse {

    private Long id;
    private Long bookId;
    private String reviewerName;
    private Integer rating;
    private String comment;
    private Instant createdAt;

    public ReviewResponse() {
    }

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse resp = new ReviewResponse();
        resp.setId(review.getId());
        resp.setBookId(review.getBook().getId());
        resp.setReviewerName(review.getUser().getName());
        resp.setRating(review.getRating());
        resp.setComment(review.getComment());
        resp.setCreatedAt(review.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
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
}
