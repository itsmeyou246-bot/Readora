package com.readora.readora.dto;

public class AdminPlatformStats {

    private long totalUsers;
    private long totalAuthors;
    private long totalBooks;
    private long totalSubscriptions;
    private long totalPurchases;
    private long totalReviews;

    public AdminPlatformStats() {
    }

    public AdminPlatformStats(long totalUsers, long totalAuthors, long totalBooks,
                              long totalSubscriptions, long totalPurchases, long totalReviews) {
        this.totalUsers = totalUsers;
        this.totalAuthors = totalAuthors;
        this.totalBooks = totalBooks;
        this.totalSubscriptions = totalSubscriptions;
        this.totalPurchases = totalPurchases;
        this.totalReviews = totalReviews;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalAuthors() {
        return totalAuthors;
    }

    public void setTotalAuthors(long totalAuthors) {
        this.totalAuthors = totalAuthors;
    }

    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public long getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(long totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public long getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(long totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
