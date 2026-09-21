package com.readora.readora.service;

import com.readora.readora.model.Book;
import com.readora.readora.model.ReadingProgress;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.repository.ReadingProgressRepository;
import com.readora.readora.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReadingProgressService {

    private final ReadingProgressRepository repository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingHistoryService historyService;
    private final ReadingAnalyticsService analyticsService;

    public ReadingProgressService(ReadingProgressRepository repository,
                                  UserRepository userRepository,
                                  BookRepository bookRepository,
                                  ReadingHistoryService historyService,
                                  ReadingAnalyticsService analyticsService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.historyService = historyService;
        this.analyticsService = analyticsService;
    }

    public ReadingProgress saveProgress(Long userId,
                                        Long bookId,
                                        Integer currentPage,
                                        Integer totalPages) {

        ReadingProgress progress = repository
                .findByUserIdAndBookId(userId, bookId)
                .orElse(new ReadingProgress());

        progress.setUserId(userId);
        progress.setBookId(bookId);
        progress.setCurrentPage(currentPage);
        progress.setTotalPages(totalPages);

        ReadingProgress saved = repository.save(progress);

        // Sync with ReadingHistory and ReadingAnalytics
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Book> bookOpt = bookRepository.findById(bookId);

        if (userOpt.isPresent() && bookOpt.isPresent()) {
            User user = userOpt.get();
            Book book = bookOpt.get();
            historyService.recordReading(user, book, currentPage);
            analyticsService.recordReadingActivity(user, 1, 2);
        }

        return saved;
    }

    public Optional<ReadingProgress> getProgress(Long userId, Long bookId) {
        return repository.findByUserIdAndBookId(userId, bookId);
    }
}