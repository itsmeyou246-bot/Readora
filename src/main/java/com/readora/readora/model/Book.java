package com.readora.readora.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String filePath;

    protected Book() {
    }

    public Book(Long id, String title, String author, String filePath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.filePath = filePath;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getFilePath() { return filePath; }
}
