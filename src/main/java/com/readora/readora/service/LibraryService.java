package com.readora.readora.service;

import com.readora.readora.model.*;
import com.readora.readora.repository.*;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class LibraryService {

    private final UserRepository users;
    private final BookRepository books;
    private final PurchaseRepository purchases;
    private final ReadingProgressRepository progress;
    private final ReadingHistoryRepository historyRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReadingAnalyticsService analyticsService;

    public LibraryService(
            UserRepository users,
            BookRepository books,
            PurchaseRepository purchases,
            ReadingProgressRepository progress,
            ReadingHistoryRepository historyRepository,
            FavoriteRepository favoriteRepository,
            ReadingAnalyticsService analyticsService
    ) {
        this.users = users;
        this.books = books;
        this.purchases = purchases;
        this.progress = progress;
        this.historyRepository = historyRepository;
        this.favoriteRepository = favoriteRepository;
        this.analyticsService = analyticsService;
    }

    /**
     * Find the currently authenticated user.
     */
    public User user(String email) {

        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Logged-in user was not found."
                        )
                );
    }

    /**
     * Used by purchase endpoints when a book payload
     * contains the book ID.
     */
    public Book bookForPurchase(Map<String, Object> data) {

        if (data == null || data.get("id") == null) {
            throw new IllegalArgumentException(
                    "Book ID is required."
            );
        }

        Long id = Long.valueOf(
                String.valueOf(data.get("id"))
        );

        return books.findById(id)
                .orElseGet(() -> {

                    Book book = new Book(
                            id,
                            String.valueOf(
                                    data.getOrDefault(
                                            "title",
                                            "Untitled Book"
                                    )
                            ),
                            String.valueOf(
                                    data.getOrDefault(
                                            "author",
                                            "Unknown Author"
                                    )
                            ),
                            fileName(
                                    String.valueOf(
                                            data.getOrDefault(
                                                    "title",
                                                    "Book"
                                            )
                                    )
                            )
                    );

                    if (data.get("description") != null) {
                        book.setDescription(
                                String.valueOf(
                                        data.get("description")
                                )
                        );
                    }

                    if (data.get("category") != null) {
                        book.setCategory(
                                String.valueOf(
                                        data.get("category")
                                )
                        );
                    }

                    if (data.get("language") != null) {
                        book.setLanguage(
                                String.valueOf(
                                        data.get("language")
                                )
                        );
                    }

                    if (data.get("price") != null) {
                        try {
                            book.setPrice(
                                    Double.valueOf(
                                            String.valueOf(
                                                    data.get("price")
                                            )
                                    )
                            );
                        } catch (Exception ignored) {
                        }
                    }

                    if (data.get("image") != null) {
                        book.setImage(
                                String.valueOf(
                                        data.get("image")
                                )
                        );
                    }

                    return books.save(book);
                });
    }

    /**
     * Check whether a reader owns a premium book.
     */
    public boolean owns(User user, Long bookId) {

        if (user == null || bookId == null) {
            return false;
        }

        return purchases.existsByUserAndBookId(
                user,
                bookId
        );
    }

    /**
     * Simple purchased-library response.
     *
     * Kept for backwards compatibility with existing
     * frontend/backend calls.
     */
    public List<Map<String, Object>> library(User user) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Purchase purchase :
                purchases.findByUserOrderByPurchasedAtDesc(user)) {

            Book book = purchase.getBook();

            int currentPage =
                    progress
                            .findByUserIdAndBookId(
                                    user.getId(),
                                    book.getId()
                            )
                            .map(ReadingProgress::getCurrentPage)
                            .orElse(0);

            int totalPages =
                    getTotalPages(book);

            int percentage =
                    calculateProgress(
                            currentPage,
                            totalPages
                    );

            Map<String, Object> item =
                    toBookMap(
                            book,
                            currentPage,
                            percentage
                    );

            item.put(
                    "purchasedAt",
                    purchase.getPurchasedAt() != null
                            ? purchase.getPurchasedAt().toString()
                            : null
            );

            item.put(
                    "owned",
                    true
            );

            result.add(item);
        }

        return result;
    }

    /**
     * Complete reader library.
     *
     * reading    = books with actual reading history
     * saved      = favorites
     * completed  = completed reading history
     * purchased  = purchased premium books
     *
     * FREE BOOKS:
     * They appear in reading/completed after the user
     * actually starts reading them.
     *
     * PREMIUM BOOKS:
     * They appear in purchased after successful purchase.
     */
    public Map<String, Object> categorizedLibrary(User user) {

        List<ReadingHistory> histories =
                historyRepository
                        .findByUserOrderByLastReadAtDesc(user);

        List<Favorite> favorites =
                favoriteRepository
                        .findByUserOrderByCreatedAtDesc(user);

        List<Purchase> userPurchases =
                purchases
                        .findByUserOrderByPurchasedAtDesc(user);

        List<Map<String, Object>> readingList =
                new ArrayList<>();

        List<Map<String, Object>> completedList =
                new ArrayList<>();

        List<Map<String, Object>> purchasedList =
                new ArrayList<>();

        List<Map<String, Object>> savedList =
                new ArrayList<>();

        /*
         * =====================================================
         * PURCHASED
         * =====================================================
         */
        for (Purchase purchase : userPurchases) {

            if (purchase == null ||
                    purchase.getBook() == null) {
                continue;
            }

            Book book = purchase.getBook();

            int currentPage =
                    progress
                            .findByUserIdAndBookId(
                                    user.getId(),
                                    book.getId()
                            )
                            .map(ReadingProgress::getCurrentPage)
                            .orElse(0);

            int totalPages =
                    getTotalPages(book);

            int percentage =
                    calculateProgress(
                            currentPage,
                            totalPages
                    );

            Map<String, Object> item =
                    toBookMap(
                            book,
                            currentPage,
                            percentage
                    );

            item.put(
                    "owned",
                    true
            );

            item.put(
                    "purchasedAt",
                    purchase.getPurchasedAt() != null
                            ? purchase.getPurchasedAt().toString()
                            : null
            );

            purchasedList.add(item);
        }

        /*
         * =====================================================
         * READING HISTORY
         * =====================================================
         */
        Set<Long> processedHistoryBooks =
                new HashSet<>();

        for (ReadingHistory history : histories) {

            if (history == null ||
                    history.getBook() == null) {
                continue;
            }

            Book book =
                    history.getBook();

            if (book.getId() == null) {
                continue;
            }

            /*
             * Avoid duplicate entries if the repository
             * ever returns duplicate history rows.
             */
            if (!processedHistoryBooks.add(
                    book.getId()
            )) {
                continue;
            }

            int currentPage =
                    history.getLastPage() != null
                            ? history.getLastPage()
                            : 1;

            int totalPages =
                    getTotalPages(book);

            int percentage =
                    calculateProgress(
                            currentPage,
                            totalPages
                    );

            Map<String, Object> item =
                    toBookMap(
                            book,
                            currentPage,
                            percentage
                    );

            boolean completed =
                    "COMPLETED".equalsIgnoreCase(
                            history.getStatus()
                    )
                            ||
                            percentage >= 100;

            item.put(
                    "completed",
                    completed
            );

            item.put(
                    "status",
                    history.getStatus()
            );

            item.put(
                    "lastReadAt",
                    history.getLastReadAt() != null
                            ? history.getLastReadAt().toString()
                            : null
            );

            item.put(
                    "link",
                    "/book-reader?bookId=" +
                            book.getId()
            );

            if (completed) {

                if (!containsBook(
                        completedList,
                        book.getId()
                )) {
                    completedList.add(item);
                }

            } else {

                if (!containsBook(
                        readingList,
                        book.getId()
                )) {
                    readingList.add(item);
                }
            }
        }

        /*
         * =====================================================
         * SAVED / FAVORITES
         * =====================================================
         */
        Set<Long> processedFavorites =
                new HashSet<>();

        for (Favorite favorite : favorites) {

            if (favorite == null ||
                    favorite.getBook() == null) {
                continue;
            }

            Book book =
                    favorite.getBook();

            if (book.getId() == null ||
                    !processedFavorites.add(
                            book.getId()
                    )) {
                continue;
            }

            int currentPage =
                    progress
                            .findByUserIdAndBookId(
                                    user.getId(),
                                    book.getId()
                            )
                            .map(ReadingProgress::getCurrentPage)
                            .orElse(0);

            int totalPages =
                    getTotalPages(book);

            int percentage =
                    calculateProgress(
                            currentPage,
                            totalPages
                    );

            Map<String, Object> item =
                    toBookMap(
                            book,
                            currentPage,
                            percentage
                    );

            item.put(
                    "saved",
                    true
            );

            item.put(
                    "link",
                    "/book-details?id=" +
                            book.getId()
            );

            savedList.add(item);
        }

        /*
         * IMPORTANT:
         *
         * Do NOT put purchased books into "reading"
         * automatically.
         *
         * A purchased book is only "currently reading"
         * after the user actually opens/reads it.
         */

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "reading",
                readingList
        );

        result.put(
                "saved",
                savedList
        );

        result.put(
                "completed",
                completedList
        );

        result.put(
                "purchased",
                purchasedList
        );

        return result;
    }

    /**
     * Convert Book to a frontend-safe map.
     */
    private Map<String, Object> toBookMap(
            Book book,
            int currentPage,
            int progressPercent
    ) {

        Map<String, Object> map =
                new LinkedHashMap<>();

        map.put(
                "id",
                book.getId()
        );

        map.put(
                "title",
                safe(
                        book.getTitle(),
                        "Untitled Book"
                )
        );

        map.put(
                "author",
                safe(
                        book.getAuthor(),
                        "Unknown Author"
                )
        );

        map.put(
                "image",
                normalizeImage(
                        book.getImage()
                )
        );

        map.put(
                "category",
                safe(
                        book.getCategory(),
                        "General"
                )
        );

        map.put(
                "language",
                safe(
                        book.getLanguage(),
                        ""
                )
        );

        map.put(
                "description",
                safe(
                        book.getDescription(),
                        ""
                )
        );

        map.put(
                "premium",
                book.isPremium()
        );

        map.put(
                "price",
                book.getPrice() != null
                        ? book.getPrice()
                        : 0
        );

        map.put(
                "pageCount",
                getTotalPages(book)
        );

        map.put(
                "currentPage",
                Math.max(
                        0,
                        currentPage
                )
        );

        map.put(
                "progress",
                Math.max(
                        0,
                        Math.min(
                                100,
                                progressPercent
                        )
                )
        );

        map.put(
                "filePath",
                book.getFilePath()
        );

        map.put(
                "link",
                "/book-details?id=" +
                        book.getId()
        );

        return map;
    }

    /**
     * Prefer the real Book.pageCount.
     *
     * The active reader itself uses BookPage count,
     * but this value is still the project's public
     * book page-count field.
     */
    private int getTotalPages(Book book) {

        if (book == null) {
            return 0;
        }

        Integer count =
                book.getPageCount();

        if (count == null || count <= 0) {
            return 0;
        }

        return count;
    }

    /**
     * A newly purchased book should show 0%,
     * not 1% simply because reader page numbering
     * starts at page 1.
     */
    private int calculateProgress(
            int currentPage,
            int totalPages
    ) {

        if (currentPage <= 0) {
            return 0;
        }

        if (totalPages <= 0) {
            return 0;
        }

        return Math.min(
                100,
                Math.round(
                        ((float) currentPage /
                                totalPages) *
                                100
                )
        );
    }

    private boolean containsBook(
            List<Map<String, Object>> list,
            Long bookId
    ) {

        return list.stream()
                .anyMatch(
                        item ->
                                Objects.equals(
                                        item.get("id"),
                                        bookId
                                )
                );
    }

    /**
     * Existing code elsewhere expects page 1
     * as the first reader page.
     */
    public int currentPage(
            User user,
            Book book
    ) {

        return progress
                .findByUserIdAndBookId(
                        user.getId(),
                        book.getId()
                )
                .map(ReadingProgress::getCurrentPage)
                .orElse(1);
    }

    /**
     * Save reader progress.
     */
    public void saveProgress(
            User user,
            Book book,
            int page
    ) {

        int safePage =
                Math.max(
                        1,
                        page
                );

        ReadingProgress value =
                progress
                        .findByUserIdAndBookId(
                                user.getId(),
                                book.getId()
                        )
                        .orElseGet(
                                () ->
                                        new ReadingProgress(
                                                user,
                                                book,
                                                safePage
                                        )
                        );

        value.setCurrentPage(
                safePage
        );

        if (book.getPageCount() != null &&
                book.getPageCount() > 0) {

            value.setTotalPages(
                    book.getPageCount()
            );
        }

        progress.save(value);

        historyRepository
                .findByUserAndBook(
                        user,
                        book
                )
                .ifPresentOrElse(

                        history -> {

                            history.setLastPage(
                                    safePage
                            );

                            history.setLastReadAt(
                                    Instant.now()
                            );

                            if (
                                    book.getPageCount() != null
                                            &&
                                            book.getPageCount() > 0
                                            &&
                                            safePage >=
                                                    book.getPageCount()
                            ) {
                                history.setStatus(
                                        "COMPLETED"
                                );
                            } else {
                                history.setStatus(
                                        "IN_PROGRESS"
                                );
                            }

                            historyRepository.save(
                                    history
                            );
                        },

                        () -> {

                            String status =
                                    book.getPageCount() != null
                                            &&
                                            book.getPageCount() > 0
                                            &&
                                            safePage >=
                                                    book.getPageCount()
                                            ? "COMPLETED"
                                            : "IN_PROGRESS";

                            historyRepository.save(
                                    new ReadingHistory(
                                            user,
                                            book,
                                            safePage,
                                            status
                                    )
                            );
                        }
                );

        analyticsService.recordReadingActivity(
                user,
                1,
                2
        );

        if (
                book.getPageCount() != null
                        &&
                        book.getPageCount() > 0
                        &&
                        safePage >= book.getPageCount()
        ) {
            analyticsService.recordBookCompleted(
                    user
            );
        }
    }

    private String normalizeImage(String image) {

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

        if (value.startsWith("static/")) {
            value =
                    "/" +
                            value.substring(
                                    "static/".length()
                            );
        }

        if (value.startsWith("/")) {
            return value;
        }

        return "/" + value;
    }

    private String safe(
            String value,
            String fallback
    ) {

        return value == null ||
                value.isBlank()
                ? fallback
                : value;
    }

    private String fileName(String title) {

        return title
                .replaceAll(
                        "[^A-Za-z0-9]+",
                        "-"
                )
                .replaceAll(
                        "(^-|-$)",
                        ""
                ) +
                ".pdf";
    }
}