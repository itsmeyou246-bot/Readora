package com.readora.readora.service;

import com.readora.readora.model.Book;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.repository.FavoriteRepository;
import com.readora.readora.repository.ReadingHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final BookRepository bookRepository;
    private final ReadingHistoryRepository historyRepository;
    private final FavoriteRepository favoriteRepository;

    public RecommendationService(BookRepository bookRepository,
                                 ReadingHistoryRepository historyRepository,
                                 FavoriteRepository favoriteRepository) {
        this.bookRepository = bookRepository;
        this.historyRepository = historyRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public List<Book> getRecommendationsForUser(User user, String moodFilter) {
        // Collect books already read or favorited to avoid duplicate recommendations
        List<Long> readBookIds = historyRepository.findBookIdsReadByUser(user);
        List<Long> favoriteBookIds = favoriteRepository.findFavoriteBookIds(user);

        Set<Long> excludedBookIds = new HashSet<>(readBookIds);
        excludedBookIds.addAll(favoriteBookIds);

        // Determine user's preferred categories
        List<String> historyCategories = historyRepository.findDistinctCategoriesReadByUser(user);
        List<String> favoriteCategories = favoriteRepository.findFavoriteCategories(user);

        Set<String> preferredCategories = new HashSet<>();
        if (historyCategories != null) preferredCategories.addAll(historyCategories);
        if (favoriteCategories != null) preferredCategories.addAll(favoriteCategories);

        // Also consider user's active mood
        String effectiveMood = (moodFilter != null && !moodFilter.isBlank())
                ? moodFilter
                : user.getCurrentMood();

        List<Book> candidates = new ArrayList<>();

        // 1. If user has preferences, fetch books in those categories that haven't been read
        if (!preferredCategories.isEmpty()) {
            List<Book> categoryBooks = bookRepository.findRecommendations(
                    new ArrayList<>(preferredCategories),
                    excludedBookIds.isEmpty() ? List.of(-1L) : new ArrayList<>(excludedBookIds)
            );
            candidates.addAll(categoryBooks);
        }

        // 2. If mood is present, also fetch books matching that mood
        if (effectiveMood != null && !effectiveMood.isBlank()) {
            List<Book> moodBooks = bookRepository.findByMoodIgnoreCase(effectiveMood.trim());
            for (Book b : moodBooks) {
                if (!excludedBookIds.contains(b.getId()) && !candidates.contains(b)) {
                    candidates.add(b);
                }
            }
        }

        // 3. If candidates are sparse (< 3), supplement with highest-rated catalog books
        if (candidates.size() < 3) {
            List<Book> topRated = bookRepository.findTop10ByOrderByAverageRatingDesc();
            for (Book b : topRated) {
                if (!excludedBookIds.contains(b.getId()) && !candidates.contains(b)) {
                    candidates.add(b);
                }
            }
        }

        // 4. Fallback: if still empty, return all books except read ones
        if (candidates.isEmpty()) {
            candidates = bookRepository.findAll().stream()
                    .filter(b -> !excludedBookIds.contains(b.getId()))
                    .collect(Collectors.toList());
        }

        // Limit to 6 personalized recommendations
        return candidates.stream().limit(6).collect(Collectors.toList());
    }

    public List<Book> getRecommendationsByMood(String mood) {
        if (mood == null || mood.isBlank()) {
            mood = "Calm";
        }
        List<Book> books = bookRepository.findByMoodIgnoreCase(mood.trim());
        if (books.isEmpty()) {
            books = bookRepository.findTop10ByOrderByAverageRatingDesc();
        }
        return books.stream().limit(6).collect(Collectors.toList());
    }
}
