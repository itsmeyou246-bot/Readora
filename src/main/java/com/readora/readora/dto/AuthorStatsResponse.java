package com.readora.readora.dto;

public class AuthorStatsResponse {

    private int totalBooks;
    private int totalPages;
    private int totalReviews;
    private double averageRating;

    public AuthorStatsResponse() {
    }

    public AuthorStatsResponse(int totalBooks, int totalPages, int totalReviews, double averageRating) {
        this.totalBooks = totalBooks;
        this.totalPages = totalPages;
        this.totalReviews = totalReviews;
        this.averageRating = averageRating;
    }

    public int getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}
