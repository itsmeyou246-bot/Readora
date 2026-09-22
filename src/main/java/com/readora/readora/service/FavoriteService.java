package com.readora.readora.service;

import com.readora.readora.dto.BookResponse;
import com.readora.readora.model.Book;
import com.readora.readora.model.Favorite;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.repository.FavoriteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final BookRepository bookRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, BookRepository bookRepository) {
        this.favoriteRepository = favoriteRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Map<String, Object> toggleFavorite(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        boolean exists = favoriteRepository.existsByUserAndBook(user, book);
        if (exists) {
            favoriteRepository.deleteByUserAndBook(user, book);
            return Map.of("favorited", false, "message", "Removed from favorites.");
        } else {
            favoriteRepository.save(new Favorite(user, book));
            return Map.of("favorited", true, "message", "Added to favorites.");
        }
    }

    public boolean isFavorite(User user, Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return false;
        return favoriteRepository.existsByUserAndBook(user, book);
    }

    public List<BookResponse> getUserFavorites(User user) {
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(f -> BookResponse.fromEntity(f.getBook()))
                .toList();
    }
}
