package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.security.JwtService;
import com.readora.readora.service.LibraryService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Controller
public class ReaderController {
    private final BookRepository books;
    private final JwtService jwt;
    private final LibraryService library;

    public ReaderController(BookRepository books, JwtService jwt, LibraryService library) {
        this.books = books;
        this.jwt = jwt;
        this.library = library;
    }

    @GetMapping("/reader/{bookId}")
    public String reader(@PathVariable Long bookId,
                          Model model) {
        Book book = books.findById(bookId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("bookId", book.getId());
        model.addAttribute("bookTitle", book.getTitle());
        model.addAttribute("bookAuthor", book.getAuthor());
        return "reader";
    }

    @GetMapping("/api/reader/{bookId}")
    @ResponseBody
    public ResponseEntity<?> details(@PathVariable Long bookId,
                                     @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = authenticated(auth).orElse(null);
        Book book = books.findById(bookId).orElse(null);
        if (user == null || book == null || !library.owns(user, bookId)) {
            return ResponseEntity.status(user == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This book is not unlocked for this account."));
        }
        return ResponseEntity.ok(Map.of("id", book.getId(), "title", book.getTitle(), "author", book.getAuthor(),
                "filePath", book.getFilePath(), "currentPage", library.currentPage(user, book)));
    }

    @GetMapping("/api/reader/{bookId}/file")
    @ResponseBody
    public ResponseEntity<Resource> file(@PathVariable Long bookId,
                                         @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = authenticated(auth).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Book book = books.findById(bookId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!library.owns(user, bookId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Resource resource = new ClassPathResource("static/books/" + book.getFilePath());
        if (!resource.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book PDF is not available.");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(resource);
    }

    @GetMapping("/books/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Void> directBookAccess(@PathVariable String fileName) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/api/reader/{bookId}/progress")
    @ResponseBody
    public ResponseEntity<?> progress(@PathVariable Long bookId,
                                      @RequestHeader(value = "Authorization", required = false) String auth,
                                      @RequestBody Map<String, Integer> body) {
        User user = authenticated(auth).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Book book = books.findById(bookId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!library.owns(user, bookId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Integer page = body.get("currentPage");
        if (page == null || page < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page.");
        library.saveProgress(user, book, page);
        return ResponseEntity.ok(Map.of("currentPage", page));
    }

    private Optional<User> authenticated(String auth) {
        try {
            return jwt.extractEmail(auth).map(library::user);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
