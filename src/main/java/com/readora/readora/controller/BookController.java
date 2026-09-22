package com.readora.readora.controller;

import com.readora.readora.dto.BookResponse;
import com.readora.readora.model.Book;
import com.readora.readora.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /*
     * GET /api/books
     *
     * Returns all published books.
     */
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {

        List<BookResponse> books = bookService
                .getAllBooks()
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(books);
    }

    /*
     * GET /api/books/{id}
     *
     * Returns one book.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable Long id
    ) {

        return bookService
                .getBookById(id)
                .map(book -> ResponseEntity.ok(convertToResponse(book)))
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * Convert Book entity into BookResponse DTO.
     */
    private BookResponse convertToResponse(Book book) {

        BookResponse response = new BookResponse();

        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setDescription(book.getDescription());
        response.setCategory(book.getCategory());
        response.setLanguage(book.getLanguage());
        response.setPrice(book.getPrice());
        response.setImage(book.getImage());
        response.setFilePath(book.getFilePath());
        response.setPremium(book.isPremium());
        response.setPageCount(book.getPageCount());
        response.setMood(book.getMood());
        response.setType(book.getType());
        response.setAverageRating(book.getAverageRating());
        response.setRatingCount(book.getRatingCount());
        response.setStatus(book.getStatus());
        response.setCreatedAt(book.getCreatedAt());

        return response;
    }
}