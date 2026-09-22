package com.readora.readora.service;

import com.readora.readora.model.Book;
import com.readora.readora.model.ReadingHistory;
import com.readora.readora.model.User;
import com.readora.readora.repository.ReadingHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReadingHistoryService {

    private final ReadingHistoryRepository historyRepository;
    private final ReadingAnalyticsService analyticsService;

    public ReadingHistoryService(ReadingHistoryRepository historyRepository,
                                 ReadingAnalyticsService analyticsService) {
        this.historyRepository = historyRepository;
        this.analyticsService = analyticsService;
    }

    public ReadingHistory recordReading(User user, Book book, int page) {
        ReadingHistory history = historyRepository.findByUserAndBook(user, book)
                .orElseGet(() -> new ReadingHistory(user, book, page, "IN_PROGRESS"));

        history.setLastPage(page);
        history.setLastReadAt(Instant.now());

        // Check if finished
        if (book.getPageCount() != null && book.getPageCount() > 0 && page >= book.getPageCount()) {
            if (!"COMPLETED".equals(history.getStatus())) {
                history.setStatus("COMPLETED");
                analyticsService.recordBookCompleted(user);
            }
        } else {
            history.setStatus("IN_PROGRESS");
        }

        return historyRepository.save(history);
    }

    public List<ReadingHistory> getUserHistory(User user) {
        return historyRepository.findByUserOrderByLastReadAtDesc(user);
    }

    public Optional<ReadingHistory> getMostRecentHistory(User user) {
        return historyRepository.findFirstByUserOrderByLastReadAtDesc(user);
    }

    public long getInProgressCount(User user) {
        return historyRepository.countByUserAndStatus(user, "IN_PROGRESS");
    }
}
