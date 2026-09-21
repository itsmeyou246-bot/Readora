package com.readora.readora.controller;

import com.readora.readora.dto.NotificationResponse;
import com.readora.readora.model.Book;
import com.readora.readora.model.Purchase;
import com.readora.readora.model.ReadingAnalytics;
import com.readora.readora.model.ReadingHistory;
import com.readora.readora.model.Subscription;
import com.readora.readora.model.User;
import com.readora.readora.repository.PurchaseRepository;
import com.readora.readora.repository.ReadingHistoryRepository;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.NoteService;
import com.readora.readora.service.NotificationService;
import com.readora.readora.service.ReadingAnalyticsService;
import com.readora.readora.service.ReadingHistoryService;
import com.readora.readora.service.RecommendationService;
import com.readora.readora.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final LibraryService libraryService;
    private final ReadingAnalyticsService analyticsService;
    private final ReadingHistoryService historyService;
    private final NoteService noteService;
    private final RecommendationService recommendationService;
    private final SubscriptionService subscriptionService;
    private final PurchaseRepository purchaseRepository;
    private final NotificationService notificationService;
    private final ReadingHistoryRepository historyRepository;

    public DashboardController(
            LibraryService libraryService,
            ReadingAnalyticsService analyticsService,
            ReadingHistoryService historyService,
            NoteService noteService,
            RecommendationService recommendationService,
            SubscriptionService subscriptionService,
            PurchaseRepository purchaseRepository,
            NotificationService notificationService,
            ReadingHistoryRepository historyRepository
    ) {
        this.libraryService = libraryService;
        this.analyticsService = analyticsService;
        this.historyService = historyService;
        this.noteService = noteService;
        this.recommendationService = recommendationService;
        this.subscriptionService = subscriptionService;
        this.purchaseRepository = purchaseRepository;
        this.notificationService = notificationService;
        this.historyRepository = historyRepository;
    }

    /**
     * Returns the authenticated user's dashboard data.
     *
     * The dashboard does not create fake reading activity.
     * Reading statistics are taken from the actual user's
     * ReadingAnalytics and ReadingHistory records.
     */
    @GetMapping
    public ResponseEntity<?> getDashboard(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Authentication is required."
                    ));
        }

        try {

            /*
             * =====================================================
             * AUTHENTICATED USER
             * =====================================================
             */

            User loggedInUser =
                    libraryService.user(authentication.getName());

            if (loggedInUser == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message",
                                "Authenticated user was not found."
                        ));
            }

            /*
             * =====================================================
             * USER
             * =====================================================
             */

            Map<String, Object> userData =
                    buildUserData(loggedInUser);

            /*
             * =====================================================
             * ANALYTICS
             * =====================================================
             */

            ReadingAnalytics analytics =
                    analyticsService.getOrCreateAnalytics(
                            loggedInUser
                    );

            long inProgressCount =
                    historyService.getInProgressCount(
                            loggedInUser
                    );

            int readingMinutes =
                    analytics.getReadingMinutes();

            int streak =
                    analytics.getStreakDays();

            int pagesRead =
                    analytics.getPagesRead();

            int booksCompleted =
                    analytics.getBooksCompleted();

            Map<String, Object> session =
                    buildSessionData(
                            readingMinutes,
                            streak,
                            inProgressCount,
                            pagesRead,
                            booksCompleted
                    );

            /*
             * =====================================================
             * CURRENT BOOK
             * =====================================================
             *
             * IMPORTANT:
             * Only an IN_PROGRESS book belongs in Continue Reading.
             *
             * A completed book must not appear as the active book.
             */

            Map<String, Object> currentBook =
                    findCurrentBook(loggedInUser);

            /*
             * =====================================================
             * RECOMMENDATIONS
             * =====================================================
             *
             * Uses the actual method implemented in
             * RecommendationService:
             *
             * getRecommendationsForUser(User, String)
             */

            List<Map<String, Object>> recommendations =
                    buildRecommendations(loggedInUser);

            /*
             * =====================================================
             * NOTES
             * =====================================================
             *
             * Uses the actual method implemented in NoteService:
             *
             * getUserNotes(User)
             */

            List<Map<String, Object>> notes =
                    buildNotes(loggedInUser);

            /*
             * =====================================================
             * GOAL
             * =====================================================
             */

            Map<String, Object> goal =
                    buildGoal(
                            booksCompleted,
                            inProgressCount
                    );

            /*
             * =====================================================
             * SUBSCRIPTION
             * =====================================================
             */

            Map<String, Object> subscriptionData =
                    buildSubscription(loggedInUser);

            /*
             * =====================================================
             * PURCHASES
             * =====================================================
             */

            List<Map<String, Object>> purchases =
                    buildPurchases(loggedInUser);

            /*
             * =====================================================
             * NOTIFICATIONS
             * =====================================================
             */

            Map<String, Object> notificationData =
                    buildNotifications(loggedInUser);

            /*
             * =====================================================
             * WEEKLY ACTIVITY
             * =====================================================
             */

            List<Map<String, Object>> activity =
                    buildWeeklyActivity(loggedInUser);

            /*
             * =====================================================
             * RESPONSE
             * =====================================================
             */

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put(
                    "user",
                    userData
            );

            response.put(
                    "session",
                    session
            );

            response.put(
                    "currentBook",
                    currentBook
            );

            response.put(
                    "recommendations",
                    recommendations
            );

            response.put(
                    "notes",
                    notes
            );

            response.put(
                    "goal",
                    goal
            );

            response.put(
                    "subscription",
                    subscriptionData
            );

            response.put(
                    "purchases",
                    purchases
            );

            response.put(
                    "notifications",
                    notificationData.get("notifications")
            );

            response.put(
                    "unreadNotificationsCount",
                    notificationData.get("unreadCount")
            );

            response.put(
                    "activity",
                    activity
            );

            /*
             * Kept for compatibility with existing dashboard JS.
             */
            response.put(
                    "papers",
                    Collections.emptyList()
            );

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            ex.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Unable to load reader dashboard."
                    ));
        }
    }

    /*
     * =========================================================
     * USER DATA
     * =========================================================
     */

    private Map<String, Object> buildUserData(User user) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                user.getId()
        );

        data.put(
                "name",
                safe(
                        user.getName(),
                        "Reader"
                )
        );

        data.put(
                "email",
                safe(
                        user.getEmail(),
                        ""
                )
        );

        data.put(
                "role",
                user.getRole() != null
                        ? user.getRole().name()
                        : "USER"
        );

        data.put(
                "mood",
                safe(
                        user.getCurrentMood(),
                        "Calm"
                )
        );

        return data;
    }

    /*
     * =========================================================
     * SESSION DATA
     * =========================================================
     */

    private Map<String, Object> buildSessionData(
            int readingMinutes,
            int streak,
            long inProgressCount,
            int pagesRead,
            int booksCompleted
    ) {

        Map<String, Object> session =
                new LinkedHashMap<>();

        session.put(
                "minutes",
                readingMinutes
        );

        session.put(
                "streak",
                streak
        );

        session.put(
                "inProgress",
                inProgressCount
        );

        session.put(
                "pagesRead",
                pagesRead
        );

        session.put(
                "booksCompleted",
                booksCompleted
        );

        return session;
    }

    /*
     * =========================================================
     * CURRENT / CONTINUE READING BOOK
     * =========================================================
     */

    private Map<String, Object> findCurrentBook(User user) {

        List<ReadingHistory> histories =
                historyRepository
                        .findByUserOrderByLastReadAtDesc(user);

        Optional<ReadingHistory> inProgressHistory =
                histories.stream()
                        .filter(history ->
                                "IN_PROGRESS".equalsIgnoreCase(
                                        history.getStatus()
                                )
                        )
                        .findFirst();

        if (inProgressHistory.isEmpty()) {
            return null;
        }

        ReadingHistory history =
                inProgressHistory.get();

        Book book =
                history.getBook();

        if (book == null) {
            return null;
        }

        return createCurrentBook(
                user,
                book,
                history
        );
    }

    /*
     * =========================================================
     * CURRENT BOOK OBJECT
     * =========================================================
     */

    private Map<String, Object> createCurrentBook(
            User user,
            Book book,
            ReadingHistory history
    ) {

        int totalPages =
                book.getPageCount() != null &&
                        book.getPageCount() > 0
                        ? book.getPageCount()
                        : 1;

        int currentPage =
                history.getLastPage() != null &&
                        history.getLastPage() > 0
                        ? history.getLastPage()
                        : 1;

        currentPage =
                Math.min(
                        currentPage,
                        totalPages
                );

        int progress =
                Math.min(
                        100,
                        Math.round(
                                (float) currentPage
                                        / totalPages
                                        * 100
                        )
                );

        long noteCount =
                noteService.getNotesCountForBook(
                        user,
                        book
                );

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "id",
                book.getId()
        );

        result.put(
                "title",
                safe(
                        book.getTitle(),
                        "Untitled"
                )
        );

        result.put(
                "author",
                safe(
                        book.getAuthor(),
                        "Unknown Author"
                )
        );

        result.put(
                "category",
                safe(
                        book.getCategory(),
                        "GENERAL"
                )
        );

        result.put(
                "progress",
                progress
        );

        result.put(
                "currentPage",
                currentPage
        );

        result.put(
                "totalPages",
                totalPages
        );

        result.put(
                "status",
                safe(
                        history.getStatus(),
                        "IN_PROGRESS"
                )
        );

        result.put(
                "cover",
                normalizeImage(
                        book.getImage()
                )
        );

        result.put(
                "notes",
                noteCount
        );

        result.put(
                "premium",
                isPremiumBook(book)
        );

        return result;
    }

    /*
     * =========================================================
     * RECOMMENDATIONS
     * =========================================================
     */

    private List<Map<String, Object>> buildRecommendations(
            User user
    ) {

        List<Book> recommendedBooks =
                recommendationService
                        .getRecommendationsForUser(
                                user,
                                user.getCurrentMood()
                        );

        if (recommendedBooks == null ||
                recommendedBooks.isEmpty()) {

            return Collections.emptyList();
        }

        List<Map<String, Object>> recommendations =
                new ArrayList<>();

        for (Book book : recommendedBooks) {

            if (book == null) {
                continue;
            }

            boolean premium =
                    isPremiumBook(book);

            String description =
                    safe(
                            book.getDescription(),
                            "Recommended for your reading profile."
                    );

            if (description.length() > 120) {
                description =
                        description.substring(0, 120)
                                + "...";
            }

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "id",
                    book.getId()
            );

            item.put(
                    "title",
                    safe(
                            book.getTitle(),
                            "Untitled"
                    )
            );

            item.put(
                    "author",
                    safe(
                            book.getAuthor(),
                            "Unknown Author"
                    )
            );

            item.put(
                    "category",
                    safe(
                            book.getCategory(),
                            "GENERAL"
                    )
            );

            item.put(
                    "match",
                    "Recommended"
            );

            item.put(
                    "description",
                    description
            );

            item.put(
                    "theme",
                    recommendationTheme(book)
            );

            item.put(
                    "premium",
                    premium
            );

            item.put(
                    "price",
                    book.getPrice() != null
                            ? book.getPrice()
                            : 0
            );

            item.put(
                    "priceLabel",
                    premium
                            ? "NPR " + book.getPrice()
                            : "Free to read"
            );

            item.put(
                    "action",
                    premium
                            ? "Unlock Book →"
                            : "Read Now →"
            );

            recommendations.add(item);
        }

        return recommendations;
    }

    /*
     * =========================================================
     * NOTES
     * =========================================================
     */

    private List<Map<String, Object>> buildNotes(
            User user
    ) {

        List<Map<String, Object>> userNotes =
                noteService.getUserNotes(user);

        if (userNotes == null ||
                userNotes.isEmpty()) {

            return Collections.emptyList();
        }

        List<Map<String, Object>> notes =
                new ArrayList<>();

        userNotes.stream()
                .limit(5)
                .forEach(note -> {

                    if (note == null) {
                        return;
                    }

                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put(
                            "bookTitle",
                            safe(
                                    note.get("bookTitle"),
                                    "Reading Note"
                            )
                    );

                    item.put(
                            "page",
                            safe(
                                    note.get("page"),
                                    ""
                            )
                    );

                    item.put(
                            "dateCreated",
                            safe(
                                    note.get("dateCreated"),
                                    ""
                            )
                    );

                    item.put(
                            "noteText",
                            safe(
                                    note.get("noteText"),
                                    ""
                            )
                    );

                    item.put(
                            "bookId",
                            note.get("bookId")
                    );

                    item.put(
                            "pageNumber",
                            note.get("pageNumber")
                    );

                    notes.add(item);
                });

        return notes;
    }

    /*
     * =========================================================
     * READING GOAL
     * =========================================================
     */

    private Map<String, Object> buildGoal(
            int booksCompleted,
            long inProgressCount
    ) {

        final int targetBooks = 50;

        int goalPercent =
                Math.min(
                        100,
                        Math.round(
                                (float) booksCompleted
                                        / targetBooks
                                        * 100
                        )
                );

        Map<String, Object> goal =
                new LinkedHashMap<>();

        goal.put(
                "target",
                targetBooks
        );

        goal.put(
                "completed",
                booksCompleted
        );

        goal.put(
                "percent",
                goalPercent
        );

        goal.put(
                "remaining",
                Math.max(
                        0,
                        targetBooks - booksCompleted
                )
        );

        goal.put(
                "inProgress",
                inProgressCount
        );

        return goal;
    }

    /*
     * =========================================================
     * SUBSCRIPTION
     * =========================================================
     */

    private Map<String, Object> buildSubscription(
            User user
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        try {

            Optional<Subscription> subscription =
                    subscriptionService.getSubscription(
                            user.getId()
                    );

            if (subscription.isPresent()) {

                Subscription sub =
                        subscription.get();

                if ("ACTIVE".equalsIgnoreCase(
                        sub.getStatus()
                )) {

                    data.put(
                            "active",
                            true
                    );

                    data.put(
                            "plan",
                            safe(
                                    sub.getPlan(),
                                    "PREMIUM"
                            )
                    );

                    data.put(
                            "endDate",
                            sub.getEndDate() != null
                                    ? sub.getEndDate().toString()
                                    : ""
                    );

                    data.put(
                            "paymentMethod",
                            safe(
                                    sub.getPaymentMethod(),
                                    "eSewa"
                            )
                    );

                    return data;
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        data.put(
                "active",
                false
        );

        data.put(
                "plan",
                "Free Plan"
        );

        data.put(
                "endDate",
                ""
        );

        data.put(
                "paymentMethod",
                ""
        );

        return data;
    }

    /*
     * =========================================================
     * PURCHASES
     * =========================================================
     */

    private List<Map<String, Object>> buildPurchases(
            User user
    ) {

        try {

            List<Purchase> userPurchases =
                    purchaseRepository
                            .findByUserOrderByPurchasedAtDesc(
                                    user
                            );

            if (userPurchases == null ||
                    userPurchases.isEmpty()) {

                return Collections.emptyList();
            }

            List<Map<String, Object>> purchases =
                    new ArrayList<>();

            userPurchases.stream()
                    .limit(5)
                    .forEach(purchase -> {

                        if (purchase == null ||
                                purchase.getBook() == null) {
                            return;
                        }

                        Book book =
                                purchase.getBook();

                        Map<String, Object> item =
                                new LinkedHashMap<>();

                        item.put(
                                "id",
                                purchase.getId()
                        );

                        item.put(
                                "bookId",
                                book.getId()
                        );

                        item.put(
                                "bookTitle",
                                safe(
                                        book.getTitle(),
                                        "Book"
                                )
                        );

                        item.put(
                                "author",
                                safe(
                                        book.getAuthor(),
                                        "Unknown Author"
                                )
                        );

                        item.put(
                                "price",
                                book.getPrice() != null
                                        ? book.getPrice()
                                        : 0
                        );

                        item.put(
                                "purchasedAt",
                                purchase.getPurchasedAt() != null
                                        ? purchase.getPurchasedAt()
                                          .toString()
                                          .substring(0, 10)
                                        : ""
                        );

                        /*
                         * READORA uses eSewa for paid book
                         * purchases.
                         */
                        item.put(
                                "gateway",
                                "eSewa"
                        );

                        purchases.add(item);
                    });

            return purchases;

        } catch (Exception ex) {

            ex.printStackTrace();

            return Collections.emptyList();
        }
    }

    /*
     * =========================================================
     * NOTIFICATIONS
     * =========================================================
     */

    private Map<String, Object> buildNotifications(
            User user
    ) {

        List<NotificationResponse> notifications =
                Collections.emptyList();

        long unreadCount = 0;

        try {

            notifications =
                    notificationService
                            .getUserNotifications(user);

            unreadCount =
                    notificationService
                            .getUnreadCount(user);

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        if (notifications == null) {
            notifications =
                    Collections.emptyList();
        }

        List<NotificationResponse> limitedNotifications =
                notifications.stream()
                        .limit(5)
                        .toList();

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "notifications",
                limitedNotifications
        );

        data.put(
                "unreadCount",
                unreadCount
        );

        return data;
    }

    /*
     * =========================================================
     * WEEKLY ACTIVITY
     * =========================================================
     *
     * Counts actual ReadingHistory activity for the last
     * seven calendar days.
     */

    private List<Map<String, Object>> buildWeeklyActivity(
            User user
    ) {

        List<Map<String, Object>> weeklyActivity =
                new ArrayList<>();

        try {

            List<ReadingHistory> historyList =
                    historyRepository
                            .findByUserOrderByLastReadAtDesc(
                                    user
                            );

            ZoneId zone =
                    ZoneId.systemDefault();

            ZonedDateTime todayStart =
                    ZonedDateTime.now(zone)
                            .truncatedTo(
                                    ChronoUnit.DAYS
                            );

            long maxCount = 1;

            long[] counts =
                    new long[7];

            /*
             * Oldest day -> today.
             */
            for (int i = 6; i >= 0; i--) {

                ZonedDateTime dayStart =
                        todayStart.minusDays(i);

                ZonedDateTime dayEnd =
                        dayStart.plusDays(1);

                long count =
                        historyList.stream()
                                .filter(history ->
                                        history.getLastReadAt() != null
                                                && !history
                                                .getLastReadAt()
                                                .isBefore(
                                                        dayStart.toInstant()
                                                )
                                                && history
                                                .getLastReadAt()
                                                .isBefore(
                                                        dayEnd.toInstant()
                                                )
                                )
                                .count();

                counts[6 - i] =
                        count;

                if (count > maxCount) {
                    maxCount = count;
                }
            }

            /*
             * Build seven dashboard bars.
             */
            for (int i = 6; i >= 0; i--) {

                ZonedDateTime dayStart =
                        todayStart.minusDays(i);

                long count =
                        counts[6 - i];

                long heightPercent =
                        Math.round(
                                (count * 100.0)
                                        / maxCount
                        );

                Map<String, Object> day =
                        new LinkedHashMap<>();

                day.put(
                        "day",
                        dayStart
                                .getDayOfWeek()
                                .getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.ENGLISH
                                )
                );

                day.put(
                        "count",
                        count
                );

                day.put(
                        "heightPercent",
                        heightPercent
                );

                weeklyActivity.add(day);
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return weeklyActivity;
    }

    /*
     * =========================================================
     * PREMIUM BOOK CHECK
     * =========================================================
     */

    private boolean isPremiumBook(Book book) {

        if (book == null) {
            return false;
        }

        return book.isPremium()
                && book.getPrice() != null
                && book.getPrice() > 0;
    }

    /*
     * =========================================================
     * RECOMMENDATION THEME
     * =========================================================
     */

    private String recommendationTheme(Book book) {

        if (book == null ||
                book.getCategory() == null) {

            return "light";
        }

        String category =
                book.getCategory();

        if (category.equalsIgnoreCase(
                "Architecture"
        )) {
            return "dark";
        }

        if (category.equalsIgnoreCase(
                "Read Nepal"
        )) {
            return "beige";
        }

        if (category.equalsIgnoreCase(
                "Nepali"
        )) {
            return "beige";
        }

        return "light";
    }

    /*
     * =========================================================
     * SAFE STRING
     * =========================================================
     */

    private String safe(
            String value,
            String fallback
    ) {

        if (value == null ||
                value.isBlank()) {

            return fallback;
        }

        return value;
    }

    private String safe(
            Object value,
            String fallback
    ) {

        if (value == null) {
            return fallback;
        }

        String text =
                String.valueOf(value);

        return text.isBlank()
                ? fallback
                : text;
    }

    /*
     * =========================================================
     * IMAGE NORMALIZATION
     * =========================================================
     */

    private String normalizeImage(
            String image
    ) {

        if (image == null ||
                image.isBlank()) {

            return "/img/Book.png";
        }

        String value =
                image
                        .replace("\\", "/")
                        .trim();

        if (value.startsWith("/static/")) {

            value =
                    value.substring(
                            "/static".length()
                    );
        }

        if (value.startsWith("/images/")) {

            value =
                    "/img/" +
                            value.substring(
                                    "/images/".length()
                            );
        }

        if (!value.startsWith("/")) {
            value =
                    "/" + value;
        }

        return value;
    }
}