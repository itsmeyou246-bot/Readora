package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.model.Purchase;
import com.readora.readora.model.User;
import com.readora.readora.repository.PurchaseRepository;
import com.readora.readora.security.JwtService;
import com.readora.readora.service.LibraryService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class LibraryController {
    private final JwtService jwt;
    private final LibraryService library;
    private final PurchaseRepository purchases;

    public LibraryController(JwtService jwt, LibraryService library, PurchaseRepository purchases) {
        this.jwt = jwt;
        this.library = library;
        this.purchases = purchases;
    }

    @GetMapping("/library")
    public ResponseEntity<?> getLibrary(@RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<User> user = authenticatedUser(auth);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in."));
        }
        return ResponseEntity.ok(library.library(user.get()));
    }

    @PostMapping("/purchases")
    public ResponseEntity<?> purchase(@RequestHeader(value = "Authorization", required = false) String auth,
                                      @RequestBody Map<String, Object> request) {
        Optional<User> authenticated = authenticatedUser(auth);
        if (authenticated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in before purchasing."));
        }
        Object rawBooks = request.get("books");
        if (!(rawBooks instanceof List<?> bookList) || bookList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "The purchase contains no books."));
        }
        User user = authenticated.get();
        for (Object rawBook : bookList) {
            if (!(rawBook instanceof Map<?, ?> rawMap)) continue;
            Map<String, Object> bookData = new HashMap<>();
            rawMap.forEach((key, value) -> bookData.put(String.valueOf(key), value));
            Book book = library.bookForPurchase(bookData);
            if (!library.owns(user, book.getId())) {
                purchases.save(new Purchase(user, book));
            }
        }
        return ResponseEntity.ok(Map.of("message", "Purchase recorded."));
    }

    private Optional<User> authenticatedUser(String auth) {
        try {
            return jwt.extractEmail(auth).map(library::user);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
