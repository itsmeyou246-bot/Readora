package com.readora.readora.service;

import com.readora.readora.dto.AdminAnalyticsResponse;
import com.readora.readora.dto.AdminAnalyticsResponse.*;
import com.readora.readora.model.*;
import com.readora.readora.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminInsightsService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kathmandu");

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PurchaseRepository purchaseRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ReviewRepository reviewRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NoteRepository noteRepository;
    private final HighlightRepository highlightRepository;
    private final NotificationRepository notificationRepository;
    private final FavoriteRepository favoriteRepository;

    public AdminInsightsService(
            UserRepository userRepository,
            BookRepository bookRepository,
            PurchaseRepository purchaseRepository,
            SubscriptionRepository subscriptionRepository,
            ReadingHistoryRepository readingHistoryRepository,
            ReadingProgressRepository readingProgressRepository,
            ReviewRepository reviewRepository,
            BookmarkRepository bookmarkRepository,
            NoteRepository noteRepository,
            HighlightRepository highlightRepository,
            NotificationRepository notificationRepository,
            FavoriteRepository favoriteRepository) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.purchaseRepository = purchaseRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.reviewRepository = reviewRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.noteRepository = noteRepository;
        this.highlightRepository = highlightRepository;
        this.notificationRepository = notificationRepository;
        this.favoriteRepository = favoriteRepository;
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @Transactional
    public void deleteUserAccount(Long userId, Long requestingAdminId) {

        if (userId.equals(requestingAdminId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You cannot delete the account you are currently logged in with."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found."
                        )
                );

        if (!bookRepository.findByAuthorUserId(userId).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This author still owns published works. Reassign or remove their catalog first."
            );
        }

        purchaseRepository.deleteAll(
                purchaseRepository.findByUserOrderByPurchasedAtDesc(user)
        );

        subscriptionRepository.findByUserId(userId)
                .ifPresent(subscriptionRepository::delete);

        readingHistoryRepository.deleteAll(
                readingHistoryRepository.findByUserOrderByLastReadAtDesc(user)
        );

        readingProgressRepository.deleteAll(
                readingProgressRepository.findAll()
                        .stream()
                        .filter(p ->
                                p.getUserId() != null &&
                                        p.getUserId().equals(userId))
                        .toList()
        );

        reviewRepository.deleteAll(
                reviewRepository.findAll()
                        .stream()
                        .filter(r ->
                                r.getUser() != null &&
                                        r.getUser().getId() != null &&
                                        r.getUser().getId().equals(userId))
                        .toList()
        );

        bookmarkRepository.deleteAll(
                bookmarkRepository.findByUserOrderByCreatedAtDesc(user)
        );

        noteRepository.deleteAll(
                noteRepository.findByUserOrderByUpdatedAtDesc(user)
        );

        highlightRepository.deleteAll(
                highlightRepository.findAll()
                        .stream()
                        .filter(h ->
                                h.getUser() != null &&
                                        h.getUser().getId() != null &&
                                        h.getUser().getId().equals(userId))
                        .toList()
        );

        notificationRepository.deleteAll(
                notificationRepository.findByUserOrderByCreatedAtDesc(user)
        );

        favoriteRepository.deleteAll(
                favoriteRepository.findByUserOrderByCreatedAtDesc(user)
        );

        userRepository.delete(user);
    }

    // =========================================================
    // ADMIN ANALYTICS
    // =========================================================

    /*
     * IMPORTANT FIX:
     *
     * ReadingHistory.user and ReadingHistory.book are LAZY.
     * Analytics reads those relationships, so this method MUST
     * execute inside a transaction.
     */
    @Transactional(readOnly = true)
    public AdminAnalyticsResponse getOverview() {

        AdminAnalyticsResponse response =
                new AdminAnalyticsResponse();

        response.setTotalUsers(
                userRepository.count()
        );

        response.setTotalBooks(
                bookRepository.count()
        );

        response.setTotalPurchases(
                purchaseRepository.count()
        );

        response.setTotalSubscriptions(
                subscriptionRepository.count()
        );

        List<ReadingHistory> allHistory =
                readingHistoryRepository.findAll();

        List<Purchase> allPurchases =
                purchaseRepository.findAll();

        List<Subscription> allSubscriptions =
                subscriptionRepository.findAll();

        List<Book> allBooks =
                bookRepository.findAll();

        response.setReadingActivity(
                buildReadingActivity(allHistory)
        );

        response.setContentDistribution(
                buildContentDistribution(allBooks)
        );

        response.setPopularContent(
                buildPopularContent(
                        allBooks,
                        allHistory
                )
        );

        response.setRecentActivity(
                buildRecentActivity(
                        allPurchases,
                        allHistory,
                        allSubscriptions
                )
        );

        return response;
    }

    // =========================================================
    // READING ACTIVITY
    // =========================================================

    private List<DailyActivityDto> buildReadingActivity(
            List<ReadingHistory> allHistory) {

        LocalDate today =
                LocalDate.now(ZONE);

        LocalDate start =
                today.minusDays(6);

        Instant fromInstant =
                start
                        .atStartOfDay(ZONE)
                        .toInstant();

        List<ReadingHistory> recent =
                allHistory == null
                        ? List.of()
                        : allHistory.stream()
                          .filter(h ->
                                  h != null &&
                                  h.getLastReadAt() != null &&
                                  !h.getLastReadAt()
                                   .isBefore(fromInstant))
                          .toList();

        Map<LocalDate, List<ReadingHistory>> byDay =
                recent.stream()
                        .filter(h ->
                                h.getLastReadAt() != null)
                        .collect(
                                Collectors.groupingBy(
                                        h ->
                                                h.getLastReadAt()
                                                        .atZone(ZONE)
                                                        .toLocalDate()
                                )
                        );

        List<DailyActivityDto> days =
                new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            LocalDate day =
                    start.plusDays(i);

            List<ReadingHistory> events =
                    byDay.getOrDefault(
                            day,
                            List.of()
                    );

            long pagesRead =
                    events.stream()
                            .mapToLong(h ->
                                    h.getLastPage() == null
                                            ? 0
                                            : h.getLastPage())
                            .sum();

            long activeReaders =
                    events.stream()
                            .filter(h ->
                                    h.getUser() != null &&
                                            h.getUser().getId() != null)
                            .map(h ->
                                    h.getUser().getId())
                            .distinct()
                            .count();

            String label =
                    day.getMonth()
                            .getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                            )
                            + " "
                            + day.getDayOfMonth();

            days.add(
                    new DailyActivityDto(
                            label,
                            pagesRead,
                            activeReaders
                    )
            );
        }

        return days;
    }

    // =========================================================
    // CONTENT DISTRIBUTION
    // =========================================================

    private List<ContentShareDto> buildContentDistribution(
            List<Book> books) {

        if (books == null || books.isEmpty()) {
            return List.of();
        }

        long total =
                books.size();

        Map<String, Long> counts =
                books.stream()
                        .filter(Objects::nonNull)
                        .collect(
                                Collectors.groupingBy(
                                        b ->
                                                b.getCategory() == null ||
                                                        b.getCategory().isBlank()
                                                        ? "Uncategorized"
                                                        : b.getCategory(),
                                        Collectors.counting()
                                )
                        );

        return counts.entrySet()
                .stream()
                .sorted(
                        (a, b) ->
                                Long.compare(
                                        b.getValue(),
                                        a.getValue()
                                )
                )
                .limit(6)
                .map(e ->
                        new ContentShareDto(
                                e.getKey(),
                                e.getValue(),
                                Math.round(
                                        e.getValue()
                                                * 1000.0
                                                / total
                                ) / 10.0
                        )
                )
                .collect(Collectors.toList());
    }

    // =========================================================
    // POPULAR CONTENT
    // =========================================================

    private List<PopularBookDto> buildPopularContent(
            List<Book> books,
            List<ReadingHistory> allHistory) {

        if (books == null || books.isEmpty()) {
            return List.of();
        }

        List<ReadingHistory> historyList =
                allHistory == null
                        ? List.of()
                        : allHistory;

        List<Book> topBooks =
                books.stream()
                        .filter(b ->
                                b != null &&
                                        "published"
                                                .equalsIgnoreCase(
                                                        b.getStatus()))
                        .sorted((a, b) -> {

                            int byCount =
                                    Integer.compare(
                                            b.getRatingCount() == null
                                                    ? 0
                                                    : b.getRatingCount(),
                                            a.getRatingCount() == null
                                                    ? 0
                                                    : a.getRatingCount()
                                    );

                            if (byCount != 0) {
                                return byCount;
                            }

                            return Double.compare(
                                    b.getAverageRating() == null
                                            ? 0.0
                                            : b.getAverageRating(),
                                    a.getAverageRating() == null
                                            ? 0.0
                                            : a.getAverageRating()
                            );
                        })
                        .limit(4)
                        .toList();

        List<PopularBookDto> result =
                new ArrayList<>();

        for (Book book : topBooks) {

            List<ReadingHistory> historyForBook =
                    historyList.stream()
                            .filter(h ->
                                    h != null &&
                                            h.getBook() != null &&
                                            book.getId() != null &&
                                            book.getId().equals(
                                                    h.getBook().getId()))
                            .toList();

            long readers =
                    historyForBook.stream()
                            .filter(h ->
                                    h.getUser() != null &&
                                            h.getUser().getId() != null)
                            .map(h ->
                                    h.getUser().getId())
                            .distinct()
                            .count();

            long completed =
                    historyForBook.stream()
                            .filter(h ->
                                    "COMPLETED"
                                            .equalsIgnoreCase(
                                                    h.getStatus()))
                            .count();

            double completionRate =
                    historyForBook.isEmpty()
                            ? 0.0
                            : Math.round(
                            completed
                            * 1000.0
                            / historyForBook.size()
                    ) / 10.0;

            result.add(
                    new PopularBookDto(
                            book.getTitle(),
                            book.getAuthor(),
                            book.getCategory(),
                            readers,
                            book.getAverageRating() == null
                                    ? 0.0
                                    : book.getAverageRating(),
                            completionRate
                    )
            );
        }

        return result;
    }

    // =========================================================
    // RECENT ACTIVITY
    // =========================================================

    private List<ActivityEventDto> buildRecentActivity(
            List<Purchase> purchases,
            List<ReadingHistory> history,
            List<Subscription> subscriptions) {

        List<ActivityEventDto> events =
                new ArrayList<>();

        Instant now =
                Instant.now();

        // ---------------- PURCHASES ----------------

        if (purchases != null) {

            purchases.stream()
                    .filter(p ->
                            p != null &&
                                    p.getPurchasedAt() != null &&
                                    p.getBook() != null &&
                                    p.getUser() != null)
                    .sorted(
                            (a, b) ->
                                    b.getPurchasedAt()
                                            .compareTo(
                                                    a.getPurchasedAt())
                    )
                    .limit(6)
                    .forEach(p -> {

                        double price =
                                p.getBook().getPrice() == null
                                        ? 0.0
                                        : p.getBook().getPrice();

                        String userName =
                                p.getUser().getName() != null
                                        ? p.getUser().getName()
                                        : "User";

                        String bookTitle =
                                p.getBook().getTitle() != null
                                        ? p.getBook().getTitle()
                                        : "Book";

                        events.add(
                                new ActivityEventDto(
                                        "shopping_bag",
                                        "Purchase completed: "
                                                + bookTitle,
                                        userName
                                                + " • Rs. "
                                                + String.format(
                                                "%.2f",
                                                price),
                                        p.getPurchasedAt(),
                                        relativeTime(
                                                p.getPurchasedAt(),
                                                now)
                                )
                        );
                    });
        }

        // ---------------- READING ----------------

        if (history != null) {

            history.stream()
                    .filter(h ->
                            h != null &&
                                    h.getLastReadAt() != null &&
                                    h.getBook() != null &&
                                    h.getUser() != null)
                    .sorted(
                            (a, b) ->
                                    b.getLastReadAt()
                                            .compareTo(
                                                    a.getLastReadAt())
                    )
                    .limit(6)
                    .forEach(h -> {

                        boolean done =
                                "COMPLETED"
                                        .equalsIgnoreCase(
                                                h.getStatus());

                        String userName =
                                h.getUser().getName() != null
                                        ? h.getUser().getName()
                                        : "User";

                        String bookTitle =
                                h.getBook().getTitle() != null
                                        ? h.getBook().getTitle()
                                        : "Book";

                        events.add(
                                new ActivityEventDto(
                                        done
                                                ? "task_alt"
                                                : "auto_stories",
                                        done
                                                ? "Book completed: "
                                                  + bookTitle
                                                : "Reading session in "
                                                  + bookTitle,
                                        userName
                                                + " • page "
                                                + (h.getLastPage() != null
                                                ? h.getLastPage()
                                                : 1),
                                        h.getLastReadAt(),
                                        relativeTime(
                                                h.getLastReadAt(),
                                                now)
                                )
                        );
                    });
        }

        // ---------------- SUBSCRIPTIONS ----------------

        if (subscriptions != null) {

            subscriptions.stream()
                    .filter(s ->
                            s != null &&
                                    s.getStartDate() != null &&
                                    s.getUserId() != null)
                    .sorted(
                            (a, b) ->
                                    b.getStartDate()
                                            .compareTo(
                                                    a.getStartDate())
                    )
                    .limit(4)
                    .forEach(s ->

                            userRepository
                                    .findById(
                                            s.getUserId())
                                    .ifPresent(user -> {

                                        Instant startedAt =
                                                s.getStartDate()
                                                        .atStartOfDay(
                                                                ZONE)
                                                        .toInstant();

                                        String userName =
                                                user.getName() != null
                                                        ? user.getName()
                                                        : "User";

                                        events.add(
                                                new ActivityEventDto(
                                                        "card_membership",
                                                        "New subscription: "
                                                                + (s.getPlan() != null
                                                                ? s.getPlan()
                                                                : "Plan"),
                                                        userName
                                                                + " • Status "
                                                                + (s.getStatus() != null
                                                                ? s.getStatus()
                                                                : "ACTIVE"),
                                                        startedAt,
                                                        relativeTime(
                                                                startedAt,
                                                                now)
                                                )
                                        );
                                    })
                    );
        }

        events.sort((a, b) -> {

            if (a.getTimestamp() == null &&
                    b.getTimestamp() == null) {
                return 0;
            }

            if (a.getTimestamp() == null) {
                return 1;
            }

            if (b.getTimestamp() == null) {
                return -1;
            }

            return b.getTimestamp()
                    .compareTo(
                            a.getTimestamp());
        });

        return events.stream()
                .limit(6)
                .collect(Collectors.toList());
    }

    // =========================================================
    // TIME
    // =========================================================

    private String relativeTime(
            Instant from,
            Instant now) {

        long minutes =
                Duration.between(
                        from,
                        now
                ).toMinutes();

        if (minutes < 1) {
            return "just now";
        }

        if (minutes < 60) {
            return minutes + "m ago";
        }

        long hours =
                minutes / 60;

        if (hours < 24) {
            return hours + "h ago";
        }

        long days =
                hours / 24;

        return days + "d ago";
    }
}