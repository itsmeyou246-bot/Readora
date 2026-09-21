package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

// Separate file from AdminController.java - does not modify it.
// Adds the one capability AdminController doesn't have yet: editing
// an existing book's status/price/premium flag as an admin.
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookActionsController {

    private static final List<String> VALID_STATUSES = List.of("published", "scheduled", "draft");

    private final BookRepository bookRepository;

    public AdminBookActionsController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @PutMapping("/books/{id}/status")
    public ResponseEntity<Map<String, Object>> updateBookStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        Object statusValue = body.get("status");
        if (statusValue != null) {
            String status = String.valueOf(statusValue).toLowerCase();
            if (!VALID_STATUSES.contains(status)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Status must be one of: published, scheduled, draft.");
            }
            book.setStatus(status);
        }

        Object premiumValue = body.get("premium");
        if (premiumValue != null) {
            book.setPremium(Boolean.parseBoolean(String.valueOf(premiumValue)));
        }

        Object priceValue = body.get("price");
        if (priceValue != null) {
            try {
                book.setPrice(Double.parseDouble(String.valueOf(priceValue)));
            } catch (NumberFormatException ignored) {
                // leave price untouched if not a valid number
            }
        }

        bookRepository.save(book);

        return ResponseEntity.ok(Map.of(
                "id", book.getId(),
                "status", book.getStatus(),
                "premium", book.isPremium(),
                "price", book.getPrice() != null ? book.getPrice() : 0
        ));
    }
}