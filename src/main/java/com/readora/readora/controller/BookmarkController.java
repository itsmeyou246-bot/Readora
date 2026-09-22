package com.readora.readora.controller;

import com.readora.readora.dto.BookmarkRequest;
import com.readora.readora.model.Book;
import com.readora.readora.model.Bookmark;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.service.BookmarkService;
import com.readora.readora.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final LibraryService libraryService;
    private final BookRepository bookRepository;

    public BookmarkController(BookmarkService bookmarkService,
                              LibraryService libraryService,
                              BookRepository bookRepository) {
        this.bookmarkService = bookmarkService;
        this.libraryService = libraryService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getBookmarks(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(bookmarkService.getUserBookmarks(user));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkBookmark(
            @RequestParam Long bookId,
            @RequestParam Integer page,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        boolean bookmarked = bookmarkService.isBookmarked(user, book, page);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addBookmark(
            @RequestBody BookmarkRequest request,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        if (request.getBookId() == null || request.getPageNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book ID and Page Number are required.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        Bookmark bookmark = bookmarkService.addBookmark(user, book, request.getPageNumber(), request.getTitle());
        return ResponseEntity.ok(Map.of(
                "id", bookmark.getId(),
                "bookId", book.getId(),
                "pageNumber", bookmark.getPageNumber(),
                "message", "Bookmark saved."
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBookmarkById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        bookmarkService.deleteBookmarkById(user, id);
        return ResponseEntity.ok(Map.of("message", "Bookmark removed."));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteBookmarkByPage(
            @RequestParam Long bookId,
            @RequestParam Integer page,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        bookmarkService.removeBookmark(user, book, page);
        return ResponseEntity.ok(Map.of("message", "Bookmark removed."));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
