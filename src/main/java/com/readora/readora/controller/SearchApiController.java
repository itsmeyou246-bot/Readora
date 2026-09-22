package com.readora.readora.controller;

import com.readora.readora.dto.BookResponse;
import com.readora.readora.dto.BookSearchRequest;
import com.readora.readora.model.Book;
import com.readora.readora.service.BookSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class SearchApiController {

    private final BookSearchService searchService;

    public SearchApiController(BookSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String premium,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy
    ) {

        BookSearchRequest request = new BookSearchRequest();

        request.setQ(q);
        request.setCategory(category);
        request.setContentType(contentType);
        request.setLanguage(language);
        request.setPremium(premium);
        request.setMinRating(minRating);
        request.setSortBy(sortBy);

        List<Book> results = searchService.search(request);

        List<BookResponse> response = results.stream()
                .map(BookResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }
}