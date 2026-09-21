package com.readora.readora.controller;

import com.readora.readora.dto.ReadingProgressRequest;
import com.readora.readora.model.ReadingProgress;
import com.readora.readora.model.User;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.ReadingProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/reading-progress")
public class ReadingProgressController {

    private final ReadingProgressService service;
    private final LibraryService libraryService;

    public ReadingProgressController(ReadingProgressService service,
                                     LibraryService libraryService) {
        this.service = service;
        this.libraryService = libraryService;
    }

    @PostMapping
    public ResponseEntity<?> saveProgress(
            @RequestBody(required = false) ReadingProgressRequest requestBody,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer currentPage,
            @RequestParam(required = false) Integer totalPages,
            Authentication authentication) {

        User user = currentUser(authentication);

        // Allow either RequestBody or RequestParam
        Long finalBookId = requestBody != null && requestBody.getBookId() != null
                ? requestBody.getBookId()
                : bookId;
        Integer finalCurrentPage = requestBody != null && requestBody.getCurrentPage() != null
                ? requestBody.getCurrentPage()
                : currentPage;
        Integer finalTotalPages = requestBody != null && requestBody.getTotalPages() != null
                ? requestBody.getTotalPages()
                : (totalPages != null ? totalPages : 1);

        // Security check: if client sent userId, it must match authenticated user
        if (userId != null && !user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only save your own reading progress."));
        }

        if (finalBookId == null || finalCurrentPage == null || finalCurrentPage < 1 ||
                finalTotalPages == null || finalTotalPages < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid reading progress data."));
        }

        ReadingProgress progress = service.saveProgress(
                user.getId(),
                finalBookId,
                finalCurrentPage,
                finalTotalPages
        );

        return ResponseEntity.ok(Map.of(
                "id", progress.getId(),
                "userId", progress.getUserId(),
                "bookId", progress.getBookId(),
                "currentPage", progress.getCurrentPage(),
                "totalPages", progress.getTotalPages(),
                "percentage", progress.getPercentage()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getProgress(
            @RequestParam(required = false) Long userId,
            @RequestParam Long bookId,
            Authentication authentication) {

        User user = currentUser(authentication);

        if (userId != null && !user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only view your own reading progress."));
        }

        return service.getProgress(user.getId(), bookId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "id", p.getId(),
                        "userId", p.getUserId(),
                        "bookId", p.getBookId(),
                        "currentPage", p.getCurrentPage(),
                        "totalPages", p.getTotalPages(),
                        "percentage", p.getPercentage()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}