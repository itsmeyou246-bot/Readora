package com.readora.readora.controller;

import com.readora.readora.dto.AuthorBookRequest;
import com.readora.readora.dto.AuthorDashboardResponse;
import com.readora.readora.dto.AuthorStatsResponse;
import com.readora.readora.dto.BookPageRequest;
import com.readora.readora.dto.BookResponse;
import com.readora.readora.model.Book;
import com.readora.readora.model.BookPage;
import com.readora.readora.model.User;
import com.readora.readora.service.AuthorService;
import com.readora.readora.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/author")
@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
public class AuthorController {

    private final AuthorService authorService;
    private final LibraryService libraryService;

    public AuthorController(AuthorService authorService,
                            LibraryService libraryService) {
        this.authorService = authorService;
        this.libraryService = libraryService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookResponse>> getMyBooks(Authentication authentication) {
        User author = currentUser(authentication);
        List<Book> books = authorService.getAuthorBooks(author);
        List<BookResponse> response = books.stream().map(BookResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/books")
    public ResponseEntity<BookResponse> createBook(@RequestBody AuthorBookRequest request,
                                                   Authentication authentication) {
        User author = currentUser(authentication);
        Book created = authorService.createBook(author, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.fromEntity(created));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id,
                                                   @RequestBody AuthorBookRequest request,
                                                   Authentication authentication) {
        User author = currentUser(authentication);
        Book updated = authorService.updateBook(author, id, request);
        return ResponseEntity.ok(BookResponse.fromEntity(updated));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable Long id,
                                                          Authentication authentication) {
        User author = currentUser(authentication);
        authorService.deleteBook(author, id);
        return ResponseEntity.ok(Map.of("message", "Book deleted successfully."));
    }

    @PostMapping("/books/{id}/pages")
    public ResponseEntity<BookPage> addPage(@PathVariable Long id,
                                            @RequestBody BookPageRequest request,
                                            Authentication authentication) {
        User author = currentUser(authentication);
        BookPage page = authorService.addBookPage(author, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(page);
    }

    @GetMapping("/stats")
    public ResponseEntity<AuthorStatsResponse> getStats(Authentication authentication) {
        User author = currentUser(authentication);
        return ResponseEntity.ok(authorService.getAuthorStats(author));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AuthorDashboardResponse> getDashboard(Authentication authentication) {
        User author = currentUser(authentication);
        return ResponseEntity.ok(authorService.getAuthorDashboardData(author));
    }

    @GetMapping("/pipeline")
    public ResponseEntity<Map<String, Object>> getPipeline(Authentication authentication) {
        User author = currentUser(authentication);
        return ResponseEntity.ok(authorService.getPipelineData(author));
    }

    @GetMapping("/royalties")
    public ResponseEntity<Map<String, Object>> getRoyalties(Authentication authentication) {
        User author = currentUser(authentication);
        return ResponseEntity.ok(authorService.getRoyaltiesData(author));
    }

    @GetMapping("/reader-analytics")
    public ResponseEntity<Map<String, Object>> getReaderAnalytics(Authentication authentication) {
        User author = currentUser(authentication);
        return ResponseEntity.ok(authorService.getReaderAnalytics(author));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
