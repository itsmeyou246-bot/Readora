package com.readora.readora.dto;

import com.readora.readora.model.User;

import java.time.Instant;

public class AdminAuthorResponse {

    private Long id;
    private String name;
    private String email;
    private String authorStatus;
    private Instant joinedAt;
    private long totalBooks;
    private long publishedBooks;

    public AdminAuthorResponse() {
    }

    public static AdminAuthorResponse fromEntity(User author, long totalBooks, long publishedBooks) {
        AdminAuthorResponse resp = new AdminAuthorResponse();
        resp.setId(author.getId());
        resp.setName(author.getName());
        resp.setEmail(author.getEmail());
        resp.setAuthorStatus(author.getAuthorStatus() != null ? author.getAuthorStatus() : "ACTIVE");
        resp.setJoinedAt(author.getCreatedAt());
        resp.setTotalBooks(totalBooks);
        resp.setPublishedBooks(publishedBooks);
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAuthorStatus() { return authorStatus; }
    public void setAuthorStatus(String authorStatus) { this.authorStatus = authorStatus; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

    public long getTotalBooks() { return totalBooks; }
    public void setTotalBooks(long totalBooks) { this.totalBooks = totalBooks; }

    public long getPublishedBooks() { return publishedBooks; }
    public void setPublishedBooks(long publishedBooks) { this.publishedBooks = publishedBooks; }
}