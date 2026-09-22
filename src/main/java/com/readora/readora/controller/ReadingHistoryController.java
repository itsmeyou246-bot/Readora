package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.model.ReadingHistory;
import com.readora.readora.model.User;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.ReadingHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class ReadingHistoryController {

    private final ReadingHistoryService historyService;
    private final LibraryService libraryService;

    public ReadingHistoryController(ReadingHistoryService historyService,
                                    LibraryService libraryService) {
        this.historyService = historyService;
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getHistory(Authentication authentication) {
        User user = currentUser(authentication);
        List<ReadingHistory> histories = historyService.getUserHistory(user);

        List<Map<String, Object>> response = histories.stream().map(h -> {
            Book b = h.getBook();
            int total = b.getPageCount() != null && b.getPageCount() > 0 ? b.getPageCount() : 1;
            int pct = Math.min(100, Math.round((float) h.getLastPage() / total * 100));

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", h.getId());
            map.put("bookId", b.getId());
            map.put("title", b.getTitle());
            map.put("author", b.getAuthor());
            map.put("image", b.getImage() != null ? b.getImage() : "img/Book.png");
            map.put("lastPage", h.getLastPage());
            map.put("totalPages", total);
            map.put("progress", pct);
            map.put("status", h.getStatus());
            map.put("lastReadAt", h.getLastReadAt().toString());
            map.put("link", "/book-reader?bookId=" + b.getId());
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
