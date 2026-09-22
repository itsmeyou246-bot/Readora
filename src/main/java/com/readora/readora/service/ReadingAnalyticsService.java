package com.readora.readora.service;

import com.readora.readora.model.ReadingAnalytics;
import com.readora.readora.model.User;
import com.readora.readora.repository.ReadingAnalyticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Map;

@Service
public class ReadingAnalyticsService {

    private final ReadingAnalyticsRepository analyticsRepository;

    public ReadingAnalyticsService(ReadingAnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public ReadingAnalytics getOrCreateAnalytics(User user) {
        return analyticsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ReadingAnalytics created = new ReadingAnalytics(user.getId());
                    return analyticsRepository.save(created);
                });
    }

    public ReadingAnalytics recordReadingActivity(User user, int pagesRead, int minutesSpent) {
        ReadingAnalytics analytics = getOrCreateAnalytics(user);

        // Increment counts
        if (pagesRead > 0) {
            analytics.setPagesRead(analytics.getPagesRead() + pagesRead);
        }
        if (minutesSpent > 0) {
            analytics.setReadingMinutes(analytics.getReadingMinutes() + minutesSpent);
        }

        // Streak calculation
        LocalDate today = LocalDate.now();
        LocalDate lastDate = analytics.getLastReadDate();

        if (lastDate == null) {
            analytics.setStreakDays(1);
        } else if (lastDate.equals(today)) {
            // Already read today, do NOT increment streak on page refresh
        } else if (lastDate.equals(today.minusDays(1))) {
            // Consecutive day reading
            analytics.setStreakDays(analytics.getStreakDays() + 1);
        } else {
            // Gap of 2 or more days: reset streak
            analytics.setStreakDays(1);
        }

        if (analytics.getStreakDays() > analytics.getLongestStreak()) {
            analytics.setLongestStreak(analytics.getStreakDays());
        }

        analytics.setLastReadDate(today);
        analytics.setUpdatedAt(Instant.now());

        return analyticsRepository.save(analytics);
    }

    public void recordBookCompleted(User user) {
        ReadingAnalytics analytics = getOrCreateAnalytics(user);
        analytics.setBooksCompleted(analytics.getBooksCompleted() + 1);
        analytics.setUpdatedAt(Instant.now());
        analyticsRepository.save(analytics);
    }

    public Map<String, Object> getAnalyticsSummary(User user) {
        ReadingAnalytics analytics = getOrCreateAnalytics(user);
        return Map.of(
                "booksRead", analytics.getBooksCompleted() + (analytics.getPagesRead() > 10 ? 1 : 0),
                "pagesRead", analytics.getPagesRead(),
                "readingMinutes", analytics.getReadingMinutes(),
                "completedBooks", analytics.getBooksCompleted(),
                "currentStreak", analytics.getStreakDays(),
                "longestStreak", analytics.getLongestStreak(),
                "lastReadDate", analytics.getLastReadDate() != null ? analytics.getLastReadDate().toString() : "None"
        );
    }
}
