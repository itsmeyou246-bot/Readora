package com.readora.readora.controller;

import com.readora.readora.dto.BookResponse;
import com.readora.readora.dto.MoodRequest;
import com.readora.readora.model.Book;
import com.readora.readora.model.User;
import com.readora.readora.repository.UserRepository;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final LibraryService libraryService;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    LibraryService libraryService,
                                    UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.libraryService = libraryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<BookResponse>> getRecommendations(
            @RequestParam(required = false) String mood,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        List<Book> books = recommendationService.getRecommendationsForUser(user, mood);
        List<BookResponse> response = books.stream().map(BookResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mood")
    public ResponseEntity<Map<String, String>> getCurrentMood(Authentication authentication) {
        User user = currentUser(authentication);
        String mood = user.getCurrentMood() != null ? user.getCurrentMood() : "Calm";
        return ResponseEntity.ok(Map.of("mood", mood));
    }

    @PostMapping("/mood")
    public ResponseEntity<Map<String, String>> setMood(@RequestBody MoodRequest request,
                                                       Authentication authentication) {
        User user = currentUser(authentication);
        String newMood = (request != null && request.getMood() != null && !request.getMood().isBlank())
                ? request.getMood().trim()
                : "Calm";

        user.setCurrentMood(newMood);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("mood", newMood, "message", "Mood updated successfully."));
    }

    @GetMapping("/mood/recommendations")
    public ResponseEntity<List<BookResponse>> getMoodRecommendations(@RequestParam(required = false) String mood) {
        List<Book> books = recommendationService.getRecommendationsByMood(mood);
        List<BookResponse> response = books.stream().map(BookResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
