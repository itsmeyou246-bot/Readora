package com.readora.readora.service;

import com.readora.readora.model.*;
import com.readora.readora.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LibraryService {
    private final UserRepository users;
    private final BookRepository books;
    private final PurchaseRepository purchases;
    private final ReadingProgressRepository progress;

    public LibraryService(UserRepository users, BookRepository books, PurchaseRepository purchases,
                          ReadingProgressRepository progress) {
        this.users = users;
        this.books = books;
        this.purchases = purchases;
        this.progress = progress;
    }

    public User user(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user was not found."));
    }

    public Book bookForPurchase(Map<String, Object> data) {
        Long id = Long.valueOf(String.valueOf(data.get("id")));
        return books.findById(id).orElseGet(() -> books.save(new Book(
                id,
                String.valueOf(data.get("title")),
                String.valueOf(data.get("author")),
                fileName(String.valueOf(data.get("title")))
        )));
    }

    public boolean owns(User user, Long bookId) {
        return purchases.existsByUserAndBookId(user, bookId);
    }

    public List<Map<String, Object>> library(User user) {
        return purchases.findByUserOrderByPurchasedAtDesc(user).stream().map(purchase -> {
            Book book = purchase.getBook();
            int page = progress.findByUserIdAndBookId(user.getId(), book.getId())
                    .map(ReadingProgress::getCurrentPage).orElse(1);
            return Map.<String, Object>of("id", book.getId(), "title", book.getTitle(), "author", book.getAuthor(),
                    "filePath", book.getFilePath(), "currentPage", page);
        }).toList();
    }

    public int currentPage(User user, Book book) {
        return progress.findByUserIdAndBookId(user.getId(), book.getId())
                .map(ReadingProgress::getCurrentPage).orElse(1);
    }

    public void saveProgress(User user, Book book, int page) {
        ReadingProgress value = progress.findByUserIdAndBookId(user.getId(), book.getId())
                .orElseGet(() -> new ReadingProgress(user, book, page));
        value.setCurrentPage(Math.max(1, page));
        progress.save(value);
    }

    private String fileName(String title) {
        return title.replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "") + ".pdf";
    }
}