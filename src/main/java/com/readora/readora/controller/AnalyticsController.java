package com.readora.readora.controller;

import com.readora.readora.model.User;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.ReadingAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final ReadingAnalyticsService analyticsService;
    private final LibraryService libraryService;

    public AnalyticsController(ReadingAnalyticsService analyticsService,
                               LibraryService libraryService) {
        this.analyticsService = analyticsService;
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary(user));
    }

    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> recordSession(@RequestBody Map<String, Integer> body,
                                                             Authentication authentication) {
        User user = currentUser(authentication);
        int minutes = body.getOrDefault("minutes", 0);
        int pages = body.getOrDefault("pages", 0);

        analyticsService.recordReadingActivity(user, pages, minutes);
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary(user));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
