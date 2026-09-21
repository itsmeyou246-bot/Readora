package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ReaderController {

    private final BookRepository books;

    public ReaderController(BookRepository books) {
        this.books = books;
    }

    // =========================================================
    // LEGACY READER URL
    // =========================================================
    //
    // Old URLs such as:
    //
    //     /reader/5
    //
    // are redirected to the current reader:
    //
    //     /book-reader?bookId=5
    //
    // The actual reader access, premium ownership check,
    // page loading and reading progress are handled by
    // BookReaderController.
    //
    // =========================================================

    @GetMapping("/reader/{bookId}")
    public String reader(@PathVariable Long bookId) {

        Book book = books.findById(bookId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Book not found."
                        )
                );

        return "redirect:/book-reader?bookId=" + book.getId();
    }
}