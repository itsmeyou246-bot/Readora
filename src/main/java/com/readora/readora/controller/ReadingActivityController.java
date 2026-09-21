package com.readora.readora.controller;

import com.readora.readora.model.ReadingHistory;
import com.readora.readora.model.User;
import com.readora.readora.repository.ReadingHistoryRepository;
import com.readora.readora.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ReadingActivityController {

    private final ReadingHistoryRepository historyRepository;
    private final LibraryService libraryService;

    public ReadingActivityController(ReadingHistoryRepository historyRepository, LibraryService libraryService) {
        this.historyRepository = historyRepository;
        this.libraryService = libraryService;
    }

    @GetMapping("/weekly")
    public List<Map<String, Object>> getWeeklyActivity(Authentication authentication) {
        User user = currentUser(authentication);
        List<ReadingHistory> history = historyRepository.findByUserOrderByLastReadAtDesc(user);

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime todayStart = ZonedDateTime.now(zone).truncatedTo(ChronoUnit.DAYS);

        List<Map<String, Object>> result = new ArrayList<>();
        long maxCount = 1;
        long[] counts = new long[7];

        for (int i = 6; i >= 0; i--) {
            ZonedDateTime dayStart = todayStart.minusDays(i);
            ZonedDateTime dayEnd = dayStart.plusDays(1);
            long count = history.stream()
                    .filter(h -> !h.getLastReadAt().isBefore(dayStart.toInstant())
                            && h.getLastReadAt().isBefore(dayEnd.toInstant()))
                    .count();
            counts[6 - i] = count;
            if (count > maxCount) maxCount = count;
        }

        for (int i = 6; i >= 0; i--) {
            ZonedDateTime dayStart = todayStart.minusDays(i);
            long count = counts[6 - i];
            result.add(Map.of(
                    "day", dayStart.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    "count", count,
                    "heightPercent", Math.round((count * 100.0) / maxCount)
            ));
        }

        return result;
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}