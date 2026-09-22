package com.readora.readora.controller;

import com.readora.readora.dto.BookResponse;
import com.readora.readora.model.User;
import com.readora.readora.service.FavoriteService;
import com.readora.readora.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final LibraryService libraryService;

    public FavoriteController(FavoriteService favoriteService,
                              LibraryService libraryService) {
        this.favoriteService = favoriteService;
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getFavorites(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(favoriteService.getUserFavorites(user));
    }

    @GetMapping("/check/{bookId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(@PathVariable Long bookId,
                                                              Authentication authentication) {
        User user = currentUser(authentication);
        boolean favorited = favoriteService.isFavorite(user, bookId);
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long bookId,
                                                              Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(favoriteService.toggleFavorite(user, bookId));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable Long bookId,
                                                              Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(favoriteService.toggleFavorite(user, bookId));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
