package com.readora.readora.controller;

import com.readora.readora.dto.ReviewRequest;
import com.readora.readora.dto.ReviewResponse;
import com.readora.readora.model.User;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final LibraryService libraryService;

    public ReviewController(ReviewService reviewService,
                            LibraryService libraryService) {
        this.reviewService = reviewService;
        this.libraryService = libraryService;
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewsForBook(bookId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createOrUpdateReview(@RequestBody ReviewRequest request,
                                                               Authentication authentication) {
        User user = currentUser(authentication);
        ReviewResponse response = reviewService.addOrUpdateReview(user, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long id,
                                                            Authentication authentication) {
        User user = currentUser(authentication);
        reviewService.deleteReview(user, id);
        return ResponseEntity.ok(Map.of("message", "Review deleted."));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        try {
            return libraryService.user(authentication.getName());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }
    }
}
