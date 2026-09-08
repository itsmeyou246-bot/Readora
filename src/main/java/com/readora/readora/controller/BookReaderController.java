package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.model.BookPage;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookPageRepository;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class BookReaderController {
    private final BookRepository books;
    private final BookPageRepository pages;
    private final LibraryService libraryService;

    public BookReaderController(BookRepository books, BookPageRepository pages, LibraryService libraryService) {
        this.books = books;
        this.pages = pages;
        this.libraryService = libraryService;
    }

    @GetMapping({"/book-reader", "/book-reader.html"})
    public String readerPage(@RequestParam("bookId") Long bookId, Model model) {
        Book book = books.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));
        model.addAttribute("bookId", book.getId());
        return "book-reader";
    }

    @GetMapping("/api/reader/{bookId}/access")
    @ResponseBody
    public ResponseEntity<?> access(@PathVariable Long bookId, Authentication authentication) {
        User user = currentUser(authentication);
        Book book = findBook(bookId);
        if (!libraryService.owns(user, bookId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Purchase required before you can read this book."));
        }
        return ResponseEntity.ok(Map.of(
                "bookId", book.getId(),
                "title", book.getTitle(),
                "author", book.getAuthor(),
                "lastReadPage", libraryService.currentPage(user, book),
                "pageCount", pages.countByBookId(bookId)
        ));
    }

    @GetMapping("/api/reader/{bookId}/pages/{pageNumber}")
    @ResponseBody
    public ResponseEntity<?> page(@PathVariable Long bookId,
                                   @PathVariable int pageNumber,
                                   Authentication authentication) {
        User user = currentUser(authentication);
        Book book = findBook(bookId);
        if (!libraryService.owns(user, bookId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Purchase required before you can read this book."));
        }
        if (pageNumber < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "Page number must be at least 1."));
        }

        BookPage bookPage = pages.findByBookIdAndPageNumber(bookId, pageNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found."));
        long pageCount = pages.countByBookId(bookId);
        libraryService.saveProgress(user, book, pageNumber);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pageId", bookPage.getPageId());
        response.put("bookId", bookId);
        response.put("pageNumber", bookPage.getPageNumber());
        response.put("pageCount", pageCount);
        response.put("chapterLabel", bookPage.getChapterLabel());
        response.put("textContent", bookPage.getTextContent());
        response.put("imageUrl", bookPage.getImageUrl());
        response.put("imageTitle", bookPage.getImageTitle());
        response.put("imageSubtitle", bookPage.getImageSubtitle());
        response.put("imageCaption", bookPage.getImageCaption());
        response.put("imagePhotoCredit", bookPage.getImagePhotoCredit());
        response.put("hasPrevious", pageNumber > 1);
        response.put("hasNext", pageNumber < pageCount);
        return ResponseEntity.ok(response);
    }

    private Book findBook(Long bookId) {
        return books.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
