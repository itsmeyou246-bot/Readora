package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.model.Highlight;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.service.HighlightService;
import com.readora.readora.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/highlights")
public class HighlightController {

    private final HighlightService highlightService;
    private final LibraryService libraryService;
    private final BookRepository bookRepository;

    public HighlightController(HighlightService highlightService,
                               LibraryService libraryService,
                               BookRepository bookRepository) {
        this.highlightService = highlightService;
        this.libraryService = libraryService;
        this.bookRepository = bookRepository;
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Highlight>> getHighlights(@PathVariable Long bookId,
                                                         Authentication authentication) {
        User user = currentUser(authentication);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        return ResponseEntity.ok(highlightService.getHighlightsForBook(user, book));
    }

    @PostMapping
    public ResponseEntity<Highlight> addHighlight(@RequestBody Map<String, Object> body,
                                                  Authentication authentication) {
        User user = currentUser(authentication);
        Long bookId = Long.valueOf(String.valueOf(body.get("bookId")));
        Integer pageNumber = Integer.valueOf(String.valueOf(body.get("pageNumber")));
        String text = String.valueOf(body.get("text"));
        String color = body.containsKey("color") ? String.valueOf(body.get("color")) : "yellow";

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        Highlight highlight = highlightService.addHighlight(user, book, pageNumber, text, color);
        return ResponseEntity.ok(highlight);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteHighlight(@PathVariable Long id,
                                                               Authentication authentication) {
        User user = currentUser(authentication);
        highlightService.deleteHighlight(user, id);
        return ResponseEntity.ok(Map.of("message", "Highlight deleted."));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
