package com.readora.readora.service;

import com.readora.readora.dto.AuthorBookRequest;
import com.readora.readora.dto.AuthorDashboardResponse;
import com.readora.readora.dto.AuthorStatsResponse;
import com.readora.readora.dto.BookPageRequest;
import com.readora.readora.model.*;
import com.readora.readora.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthorService {

    private final BookRepository bookRepository;
    private final BookPageRepository pageRepository;
    private final ReviewRepository reviewRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final HighlightRepository highlightRepository;
    private final NotificationRepository notificationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReadingProgressRepository readingProgressRepository;

    private static final double AUTHOR_ROYALTY_RATE = 0.15;

    public AuthorService(
            BookRepository bookRepository,
            BookPageRepository pageRepository,
            ReviewRepository reviewRepository,
            PurchaseRepository purchaseRepository,
            ReadingHistoryRepository readingHistoryRepository,
            HighlightRepository highlightRepository,
            NotificationRepository notificationRepository,
            BookmarkRepository bookmarkRepository,
            ReadingProgressRepository readingProgressRepository) {

        this.bookRepository = bookRepository;
        this.pageRepository = pageRepository;
        this.reviewRepository = reviewRepository;
        this.purchaseRepository = purchaseRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.highlightRepository = highlightRepository;
        this.notificationRepository = notificationRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.readingProgressRepository = readingProgressRepository;
    }

    // =========================================================
    // AUTHOR BOOKS
    // =========================================================

    public List<Book> getAuthorBooks(User author) {
        return bookRepository.findByAuthorUserId(author.getId());
    }

    // =========================================================
    // CREATE BOOK
    // =========================================================

    public Book createBook(User author, AuthorBookRequest request) {

        if (author == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Author session is required."
            );
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book data is required."
            );
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book title is required."
            );
        }

        Long maxId = bookRepository.findMaxId();
        Long newId = maxId == null ? 1L : maxId + 1L;

        Book book = new Book();

        book.setId(newId);

        book.setTitle(request.getTitle().trim());

        book.setAuthor(
                request.getAuthor() != null &&
                        !request.getAuthor().isBlank()
                        ? request.getAuthor().trim()
                        : author.getName()
        );

        book.setDescription(
                request.getDescription() != null
                        ? request.getDescription().trim()
                        : ""
        );

        book.setCategory(
                request.getCategory() != null &&
                        !request.getCategory().isBlank()
                        ? request.getCategory().trim()
                        : "Fiction"
        );

        book.setLanguage(
                request.getLanguage() != null &&
                        !request.getLanguage().isBlank()
                        ? request.getLanguage().trim()
                        : "English"
        );

        double price = request.getPrice() != null
                ? Math.max(0.0, request.getPrice())
                : 0.0;

        book.setPrice(price);

        book.setImage(
                request.getImage() != null &&
                        !request.getImage().isBlank()
                        ? request.getImage().trim()
                        : "img/Book.png"
        );

        book.setFilePath(
                request.getFilePath() != null &&
                        !request.getFilePath().isBlank()
                        ? request.getFilePath().trim()
                        : "sample.pdf"
        );

        boolean premium = request.getPremium() != null
                ? request.getPremium()
                : price > 0;

        book.setPremium(premium);

        book.setPageCount(
                request.getPageCount() != null &&
                        request.getPageCount() > 0
                        ? request.getPageCount()
                        : 1
        );

        book.setMood(
                request.getMood() != null &&
                        !request.getMood().isBlank()
                        ? request.getMood().trim()
                        : "Calm"
        );

        book.setType(
                request.getType() != null &&
                        !request.getType().isBlank()
                        ? request.getType().trim()
                        : "book"
        );

        /*
         * IMPORTANT
         *
         * Keep status values internally consistent:
         *
         * draft
         * review
         * revision
         * approved
         * published
         */
        book.setStatus(normalizeStatus(request.getStatus()));

        /*
         * IMPORTANT:
         * This connects the created book to the logged-in author.
         *
         * Without this, the author may create the book successfully
         * but My Publications / Author Dashboard may not find it.
         */
        book.setAuthorUser(author);

        book.setCreatedAt(Instant.now());
        book.setUpdatedAt(Instant.now());

        Book savedBook = bookRepository.save(book);

        /*
         * If the book is immediately published, make sure the
         * Reader has readable BookPage data.
         */
        if ("published".equalsIgnoreCase(savedBook.getStatus())) {
            ensureReadableContent(savedBook);
        }

        return savedBook;
    }

    // =========================================================
    // UPDATE BOOK
    // =========================================================

    public Book updateBook(
            User author,
            Long bookId,
            AuthorBookRequest request) {

        if (author == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Author session is required."
            );
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book data is required."
            );
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Book not found."
                        )
                );

        // -----------------------------------------------------
        // STRICT AUTHOR OWNERSHIP CHECK
        // -----------------------------------------------------

        if (book.getAuthorUser() == null ||
                !book.getAuthorUser().getId().equals(author.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this book."
            );
        }

        // -----------------------------------------------------
        // BASIC FIELDS
        // -----------------------------------------------------

        if (request.getTitle() != null &&
                !request.getTitle().isBlank()) {

            book.setTitle(request.getTitle().trim());
        }

        if (request.getAuthor() != null &&
                !request.getAuthor().isBlank()) {

            book.setAuthor(request.getAuthor().trim());
        }

        if (request.getDescription() != null) {
            book.setDescription(request.getDescription().trim());
        }

        if (request.getCategory() != null &&
                !request.getCategory().isBlank()) {

            book.setCategory(request.getCategory().trim());
        }

        if (request.getLanguage() != null &&
                !request.getLanguage().isBlank()) {

            book.setLanguage(request.getLanguage().trim());
        }

        if (request.getPrice() != null) {
            book.setPrice(Math.max(0.0, request.getPrice()));
        }

        if (request.getImage() != null &&
                !request.getImage().isBlank()) {

            book.setImage(request.getImage().trim());
        }

        if (request.getFilePath() != null &&
                !request.getFilePath().isBlank()) {

            book.setFilePath(request.getFilePath().trim());
        }

        if (request.getPremium() != null) {
            book.setPremium(request.getPremium());
        }

        if (request.getPageCount() != null &&
                request.getPageCount() > 0) {

            book.setPageCount(request.getPageCount());
        }

        if (request.getMood() != null &&
                !request.getMood().isBlank()) {

            book.setMood(request.getMood().trim());
        }

        if (request.getType() != null &&
                !request.getType().isBlank()) {

            book.setType(request.getType().trim());
        }

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        if (request.getStatus() != null &&
                !request.getStatus().isBlank()) {

            book.setStatus(normalizeStatus(request.getStatus()));
        }

        /*
         * Always preserve author ownership.
         */
        if (book.getAuthorUser() == null) {
            book.setAuthorUser(author);
        }

        book.setUpdatedAt(Instant.now());

        Book savedBook = bookRepository.save(book);

        /*
         * If author has now published the book, make sure
         * BookPage records exist.
         */
        if ("published".equalsIgnoreCase(savedBook.getStatus())) {
            ensureReadableContent(savedBook);
        }

        return savedBook;
    }

    // =========================================================
    // STATUS NORMALIZATION
    // =========================================================

    private String normalizeStatus(String status) {

        if (status == null || status.isBlank()) {
            return "draft";
        }

        String s = status.trim().toLowerCase(Locale.ENGLISH);

        if (s.equals("published") ||
                s.equals("publish") ||
                s.equals("live")) {

            return "published";
        }

        if (s.equals("review") ||
                s.equals("in_review") ||
                s.equals("in-review") ||
                s.equals("submitted") ||
                s.equals("pending")) {

            return "review";
        }

        if (s.equals("revision") ||
                s.equals("revisions")) {

            return "revision";
        }

        if (s.equals("approved") ||
                s.equals("approval")) {

            return "approved";
        }

        if (s.equals("draft") ||
                s.equals("unpublished")) {

            return "draft";
        }

        return s;
    }

    // =========================================================
    // ENSURE BOOK IS READABLE
    // =========================================================

    /**
     * Makes sure a published author book has at least one
     * BookPage record.
     *
     * The active ReaderController / BookReaderController reads
     * content from BookPageRepository.
     *
     * Therefore:
     *
     * Author publishes book
     *        ↓
     * Book saved
     *        ↓
     * BookPage created
     *        ↓
     * Reader can open book
     *
     * This method does NOT overwrite existing pages.
     */
    private void ensureReadableContent(Book book) {

        if (book == null || book.getId() == null) {
            return;
        }

        long existingPages =
                pageRepository.countByBookId(book.getId());

        /*
         * Existing pages are already good.
         */
        if (existingPages > 0) {

            /*
             * Keep the actual readable page count synchronized.
             */
            if (book.getPageCount() == null ||
                    book.getPageCount() != (int) existingPages) {

                book.setPageCount((int) existingPages);
                book.setUpdatedAt(Instant.now());
                bookRepository.save(book);
            }

            return;
        }

        /*
         * No BookPage exists.
         *
         * Create a first readable page from the book metadata.
         *
         * This prevents:
         *
         * "This book does not have any readable pages yet."
         */
        String title = book.getTitle() != null
                ? book.getTitle()
                : "READORA Book";

        String description = book.getDescription() != null &&
                !book.getDescription().isBlank()
                ? book.getDescription().trim()
                : "Welcome to this READORA publication.";

        BookPage firstPage = new BookPage(
                book,
                1,
                description,
                "Chapter 1",
                null,
                null,
                null,
                null,
                null
        );

        pageRepository.save(firstPage);

        /*
         * Reader now has at least one actual BookPage.
         */
        book.setPageCount(1);
        book.setUpdatedAt(Instant.now());

        bookRepository.save(book);
    }

    // =========================================================
    // DELETE BOOK
    // =========================================================

    public void deleteBook(User author, Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Book not found."
                        )
                );

        if (book.getAuthorUser() == null ||
                !book.getAuthorUser().getId().equals(author.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this book."
            );
        }

        bookRepository.delete(book);
    }

    // =========================================================
    // ADD BOOK PAGE
    // =========================================================

    public BookPage addBookPage(
            User author,
            Long bookId,
            BookPageRequest request) {

        if (author == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Author session is required."
            );
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page data is required."
            );
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Book not found."
                        )
                );

        if (book.getAuthorUser() == null ||
                !book.getAuthorUser().getId().equals(author.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this book."
            );
        }

        int pageNum;

        if (request.getPageNumber() != null &&
                request.getPageNumber() > 0) {

            pageNum = request.getPageNumber();

        } else {

            pageNum =
                    (int) pageRepository.countByBookId(bookId) + 1;
        }

        /*
         * Prevent invalid page numbers.
         */
        if (pageNum < 1) {
            pageNum = 1;
        }

        BookPage page = new BookPage(
                book,
                pageNum,
                request.getTextContent(),
                request.getChapterLabel(),
                request.getImageUrl(),
                request.getImageTitle(),
                request.getImageSubtitle(),
                request.getImageCaption(),
                request.getImagePhotoCredit()
        );

        BookPage saved = pageRepository.save(page);

        /*
         * Synchronize book page count with actual BookPage records.
         */
        long totalPages =
                pageRepository.countByBookId(bookId);

        book.setPageCount((int) totalPages);
        book.setUpdatedAt(Instant.now());

        bookRepository.save(book);

        return saved;
    }

    // =========================================================
    // AUTHOR STATS
    // =========================================================

    public AuthorStatsResponse getAuthorStats(User author) {

        List<Book> books =
                bookRepository.findByAuthorUserId(author.getId());

        int totalBooks = books.size();

        int totalPages = books.stream()
                .mapToInt(b ->
                        b.getPageCount() != null
                                ? b.getPageCount()
                                : 0)
                .sum();

        int totalReviews =
                (int) reviewRepository
                        .countByBookAuthorUserId(author.getId());

        Double avg =
                reviewRepository
                        .getAverageRatingForAuthor(author.getId());

        double averageRating =
                avg != null
                        ? Math.round(avg * 10.0) / 10.0
                        : 0.0;

        return new AuthorStatsResponse(
                totalBooks,
                totalPages,
                totalReviews,
                averageRating
        );
    }

    // =========================================================
    // AUTHOR DASHBOARD
    // =========================================================

    public AuthorDashboardResponse getAuthorDashboardData(
            User author) {

        AuthorDashboardResponse resp =
                new AuthorDashboardResponse();

        resp.setAuthorName(
                author.getName() != null &&
                        !author.getName().isBlank()
                        ? author.getName()
                        : "Author"
        );

        resp.setAuthorEmail(author.getEmail());

        List<Book> books =
                bookRepository.findByAuthorUserId(author.getId());

        int totalWorks = books.size();

        int publishedWorks =
                (int) books.stream()
                        .filter(b ->
                                "published".equalsIgnoreCase(
                                        b.getStatus()))
                        .count();

        int pipelineWorks =
                totalWorks - publishedWorks;

        resp.setTotalWorks(totalWorks);
        resp.setPublishedWorks(publishedWorks);
        resp.setPipelineWorks(pipelineWorks);

        long totalReads =
                readingHistoryRepository
                        .countReadsByAuthorId(author.getId());

        resp.setTotalReads(totalReads);

        Double royaltiesSum =
                purchaseRepository
                        .sumRoyaltiesByAuthorId(author.getId());

        resp.setTotalRoyalties(
                royaltiesSum != null
                        ? royaltiesSum
                        : 0.0
        );

        int totalReviews =
                (int) reviewRepository
                        .countByBookAuthorUserId(author.getId());

        Double avgRating =
                reviewRepository
                        .getAverageRatingForAuthor(author.getId());

        resp.setTotalReviews(totalReviews);

        resp.setAverageRating(
                avgRating != null
                        ? Math.round(avgRating * 10.0) / 10.0
                        : 0.0
        );

        // -----------------------------------------------------
        // TOP PERFORMING TITLE
        // -----------------------------------------------------

        if (!books.isEmpty()) {

            Book topBookEntity = null;
            long topReads = -1;
            double topRoyalties = 0.0;

            for (Book b : books) {

                long reads =
                        readingHistoryRepository
                                .countReadsByBookId(b.getId());

                double royalties =
                        purchaseRepository
                                .sumRoyaltiesByBookId(b.getId());

                if (topBookEntity == null ||
                        reads > topReads) {

                    topBookEntity = b;
                    topReads = reads;
                    topRoyalties = royalties;
                }
            }

            if (topBookEntity != null) {

                AuthorDashboardResponse.TopBookDto topDto =
                        new AuthorDashboardResponse.TopBookDto();

                topDto.setId(topBookEntity.getId());
                topDto.setTitle(topBookEntity.getTitle());

                topDto.setCategory(
                        topBookEntity.getCategory() != null
                                ? topBookEntity.getCategory()
                                : "Literature"
                );

                topDto.setImage(topBookEntity.getImage());

                topDto.setRating(
                        topBookEntity.getAverageRating() != null
                                ? topBookEntity.getAverageRating()
                                : 0.0
                );

                topDto.setReviewCount(
                        topBookEntity.getRatingCount() != null
                                ? topBookEntity.getRatingCount()
                                : 0
                );

                topDto.setCatalogReads(topReads);
                topDto.setAccruedRoyalties(topRoyalties);

                String edition =
                        topBookEntity.isPremium()
                                ? "Premium Edition"
                                : "Open Access";

                int pages =
                        topBookEntity.getPageCount() != null
                                ? topBookEntity.getPageCount()
                                : 0;

                topDto.setMeta(
                        "Published · " +
                                pages +
                                " pages · " +
                                edition
                );

                resp.setTopBook(topDto);
            }
        }

        // -----------------------------------------------------
        // WEEKLY READERSHIP
        // -----------------------------------------------------

        List<AuthorDashboardResponse.WeeklyReadDto> weeklyReads =
                new ArrayList<>();

        LocalDate today = LocalDate.now();

        DateTimeFormatter dayFormatter =
                DateTimeFormatter.ofPattern(
                        "EEE",
                        Locale.ENGLISH
                );

        for (int i = 6; i >= 0; i--) {

            LocalDate day =
                    today.minusDays(i);

            Instant startOfDay =
                    day.atStartOfDay(
                                    ZoneOffset.UTC)
                            .toInstant();

            Instant endOfDay =
                    day.plusDays(1)
                            .atStartOfDay(
                                    ZoneOffset.UTC)
                            .toInstant();

            long readsOnDay =
                    readingHistoryRepository
                            .countReadsByAuthorIdBetween(
                                    author.getId(),
                                    startOfDay,
                                    endOfDay
                            );

            weeklyReads.add(
                    new AuthorDashboardResponse.WeeklyReadDto(
                            day.format(dayFormatter),
                            readsOnDay
                    )
            );
        }

        resp.setWeeklyReads(weeklyReads);

        // -----------------------------------------------------
        // RECENT REVIEWS
        // -----------------------------------------------------

        List<Review> reviews =
                reviewRepository
                        .findByBookAuthorUserIdOrderByCreatedAtDesc(
                                author.getId()
                        );

        List<AuthorDashboardResponse.AuthorReviewDto>
                reviewDtos = new ArrayList<>();

        int reviewLimit =
                Math.min(reviews.size(), 5);

        for (int i = 0; i < reviewLimit; i++) {

            Review r = reviews.get(i);

            AuthorDashboardResponse.AuthorReviewDto rDto =
                    new AuthorDashboardResponse.AuthorReviewDto();

            rDto.setId(r.getId());

            String reviewerName =
                    r.getUser() != null &&
                            r.getUser().getName() != null
                            ? r.getUser().getName()
                            : "Reader";

            rDto.setName(reviewerName);
            rDto.setInitials(getInitials(reviewerName));
            rDto.setLoc("Reader in READORA");

            rDto.setStars(
                    r.getRating() != null
                            ? r.getRating()
                            : 5
            );

            rDto.setQuote(
                    r.getComment() != null
                            ? r.getComment()
                            : ""
            );

            rDto.setBook(
                    r.getBook() != null
                            ? "On " + r.getBook().getTitle()
                            : ""
            );

            rDto.setTime(
                    formatTimeAgo(r.getCreatedAt())
            );

            reviewDtos.add(rDto);
        }

        resp.setRecentReviews(reviewDtos);

        // -----------------------------------------------------
        // RECENT HIGHLIGHTS
        // -----------------------------------------------------

        List<Highlight> highlights =
                highlightRepository
                        .findByBookAuthorUserIdOrderByCreatedAtDesc(
                                author.getId()
                        );

        List<AuthorDashboardResponse.AuthorHighlightDto>
                highlightDtos = new ArrayList<>();

        int highlightLimit =
                Math.min(highlights.size(), 5);

        for (int i = 0; i < highlightLimit; i++) {

            Highlight h = highlights.get(i);

            AuthorDashboardResponse.AuthorHighlightDto hDto =
                    new AuthorDashboardResponse.AuthorHighlightDto();

            hDto.setId(h.getId());

            String bookTitle =
                    h.getBook() != null
                            ? h.getBook().getTitle()
                            : "Book";

            int pageNum =
                    h.getPageNumber() != null
                            ? h.getPageNumber()
                            : 1;

            hDto.setBook(
                    bookTitle +
                            " · Page " +
                            pageNum
            );

            long bookHighlightCount =
                    h.getBook() != null
                            ? highlightRepository.countByBookId(
                            h.getBook().getId())
                            : 1L;

            hDto.setCount(
                    bookHighlightCount +
                            (
                                    bookHighlightCount == 1
                                            ? " HIGHLIGHT"
                                            : " HIGHLIGHTS"
                            )
            );

            hDto.setQuote(
                    h.getText() != null
                            ? h.getText()
                            : ""
            );

            String readerName =
                    h.getUser() != null &&
                            h.getUser().getName() != null
                            ? h.getUser().getName()
                            : "Reader";

            hDto.setRef(
                    "Highlighted by " +
                            readerName
            );

            highlightDtos.add(hDto);
        }

        resp.setRecentHighlights(highlightDtos);

        // -----------------------------------------------------
        // AUTHOR NOTIFICATIONS
        // -----------------------------------------------------

        List<Notification> notifs =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(author);

        List<AuthorDashboardResponse.AuthorNoticeDto>
                noticeDtos = new ArrayList<>();

        int notifLimit =
                Math.min(notifs.size(), 5);

        for (int i = 0; i < notifLimit; i++) {

            Notification n = notifs.get(i);

            noticeDtos.add(
                    new AuthorDashboardResponse.AuthorNoticeDto(
                            n.getId(),
                            n.getMessage() != null
                                    ? n.getMessage()
                                    : n.getTitle(),
                            formatTimeAgo(n.getCreatedAt())
                    )
            );
        }

        resp.setNotices(noticeDtos);

        return resp;
    }

    // =========================================================
    // SUBMISSIONS & PIPELINE
    // =========================================================

    public Map<String, Object> getPipelineData(User author) {

        List<Book> books =
                bookRepository.findByAuthorUserId(author.getId());

        int draft = 0;
        int review = 0;
        int revision = 0;
        int approved = 0;
        int published = 0;

        for (Book b : books) {

            String st =
                    normalizeStatus(b.getStatus());

            if ("draft".equals(st)) {
                draft++;
            } else if ("review".equals(st)) {
                review++;
            } else if ("revision".equals(st)) {
                revision++;
            } else if ("approved".equals(st)) {
                approved++;
            } else if ("published".equals(st)) {
                published++;
            }
        }

        List<Map<String, Object>> stages =
                new ArrayList<>();

        stages.add(
                stage(
                        1,
                        "Draft",
                        "Author authoring",
                        draft,
                        "#948d78",
                        draft > 0 && review == 0
                )
        );

        stages.add(
                stage(
                        2,
                        "Submitted",
                        "Intake & validation",
                        review,
                        "#3b6fd6",
                        false
                )
        );

        stages.add(
                stage(
                        3,
                        "Editorial Review",
                        "Curator appraisal",
                        review,
                        "#c99a4b",
                        review > 0
                )
        );

        stages.add(
                stage(
                        4,
                        "Revision",
                        "Author edits",
                        revision,
                        "#c9704b",
                        revision > 0
                )
        );

        stages.add(
                stage(
                        5,
                        "Approved",
                        "Typesetting ready",
                        approved,
                        "#2f4a3a",
                        approved > 0
                )
        );

        stages.add(
                stage(
                        6,
                        "Published",
                        "Live on READORA",
                        published,
                        "#2f4a3a",
                        false
                )
        );

        List<Map<String, Object>> manuscripts =
                new ArrayList<>();

        List<Map<String, Object>> completed =
                new ArrayList<>();

        DateTimeFormatter dateFmt =
                DateTimeFormatter.ofPattern(
                        "MMM dd, yyyy",
                        Locale.ENGLISH
                );

        for (Book b : books) {

            String st =
                    normalizeStatus(b.getStatus());

            if ("published".equals(st)) {

                Map<String, Object> row =
                        new LinkedHashMap<>();

                row.put("id", b.getId());
                row.put("title", b.getTitle());

                String submitted =
                        b.getCreatedAt() != null
                                ? dateFmt.format(
                                b.getCreatedAt()
                                .atZone(
                                        ZoneOffset.UTC))
                                : "—";

                String publishedDate =
                        b.getUpdatedAt() != null
                                ? dateFmt.format(
                                b.getUpdatedAt()
                                .atZone(
                                        ZoneOffset.UTC))
                                : submitted;

                row.put("submitted", submitted);
                row.put(
                        "approvedBy",
                        "READORA Editorial Board"
                );
                row.put("published", publishedDate);

                double rating =
                        b.getAverageRating() != null
                                ? b.getAverageRating()
                                : 0.0;

                row.put(
                        "rating",
                        rating > 0
                                ? String.format(
                                Locale.ENGLISH,
                                "%.1f / 5.0",
                                rating
                        )
                                : "Published"
                );

                completed.add(row);

            } else {

                manuscripts.add(
                        toManuscriptCard(
                                b,
                                st,
                                dateFmt
                        )
                );
            }
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("stages", stages);
        result.put("manuscripts", manuscripts);
        result.put("completed", completed);

        return result;
    }

    private Map<String, Object> stage(
            int num,
            String name,
            String desc,
            int active,
            String color,
            boolean isActive) {

        Map<String, Object> s =
                new LinkedHashMap<>();

        s.put("num", num);
        s.put("name", name);
        s.put("desc", desc);
        s.put("active", active);
        s.put("dotColor", color);
        s.put("isActive", isActive);

        return s;
    }

    private Map<String, Object> toManuscriptCard(
            Book b,
            String st,
            DateTimeFormatter dateFmt) {

        Map<String, Object> m =
                new LinkedHashMap<>();

        boolean isDraft =
                "draft".equals(st);

        m.put("id", b.getId());
        m.put(
                "type",
                isDraft
                        ? "draft"
                        : "review"
        );

        m.put("title", b.getTitle());

        m.put(
                "status",
                isDraft
                        ? "Draft"
                        : (
                        "revision".equals(st)
                        ? "Revision"
                        : "In Review"
                )
        );

        String pages =
                b.getPageCount() != null
                        ? b.getPageCount() + " pages"
                        : "—";

        String price =
                b.getPrice() != null &&
                        b.getPrice() > 0
                        ? "Rs. " +
                        Math.round(b.getPrice())
                        : "Open Access";

        m.put(
                "meta",
                (
                        b.getCategory() != null
                                ? b.getCategory()
                                : "Literature"
                ) +
                        " · " +
                        (
                                b.getLanguage() != null
                                        ? b.getLanguage()
                                        : "English"
                        ) +
                        " · " +
                        pages +
                        " · Proposed Price: " +
                        price
        );

        if (isDraft) {

            m.put(
                    "submissionDate",
                    "Not yet submitted"
            );

            m.put(
                    "editor",
                    "Unassigned (Pre-submission)"
            );

            m.put(
                    "lastUpdate",
                    b.getUpdatedAt() != null
                            ? formatTimeAgo(
                            b.getUpdatedAt())
                            : "—"
            );

            m.put(
                    "action",
                    "Continue editing draft"
            );

            m.put(
                    "actionType",
                    "action-box info"
            );

            m.put("progress", 40);

            m.put(
                    "remaining",
                    "Complete metadata & submit for review"
            );

            m.put(
                    "buttons",
                    List.of(
                            "Continue Editing",
                            "View Details"
                    )
            );

        } else {

            String when =
                    b.getCreatedAt() != null
                            ? dateFmt.format(
                            b.getCreatedAt()
                            .atZone(
                                    ZoneOffset.UTC))
                              +
                              " (" +
                            formatTimeAgo(
                                    b.getCreatedAt())
                              +
                              ")"
                            : "—";

            m.put(
                    "submissionDate",
                    when
            );

            m.put(
                    "editor",
                    "Senior Literary Curator"
            );

            m.put(
                    "lastUpdate",
                    b.getUpdatedAt() != null
                            ? formatTimeAgo(
                            b.getUpdatedAt())
                            : "—"
            );

            m.put(
                    "action",
                    "Awaiting editorial feedback"
            );

            m.put(
                    "actionType",
                    "action-box"
            );

            m.put(
                    "buttons",
                    List.of(
                            "View Manuscript",
                            "View Details"
                    )
            );
        }

        return m;
    }

    // =========================================================
    // ROYALTIES & SALES
    // =========================================================

    public Map<String, Object> getRoyaltiesData(User author) {

        List<Purchase> purchases =
                purchaseRepository
                        .findByAuthorIdOrderByPurchasedAtDesc(
                                author.getId()
                        );

        double totalSales = 0.0;
        double totalRoyalties = 0.0;
        double pendingRoyalties = 0.0;

        Instant weekAgo =
                Instant.now().minus(Duration.ofDays(7));

        YearMonth thisMonth =
                YearMonth.now();

        Instant monthStart =
                thisMonth.atDay(1)
                        .atStartOfDay(
                                ZoneOffset.UTC)
                        .toInstant();

        Instant monthEnd =
                thisMonth.plusMonths(1)
                        .atDay(1)
                        .atStartOfDay(
                                ZoneOffset.UTC)
                        .toInstant();

        Double monthSalesRaw =
                purchaseRepository
                        .sumSalesByAuthorIdBetween(
                                author.getId(),
                                monthStart,
                                monthEnd
                        );

        double monthSales =
                monthSalesRaw != null
                        ? monthSalesRaw
                        : 0.0;

        double monthRoyalties =
                monthSales * AUTHOR_ROYALTY_RATE;

        DateTimeFormatter dateFmt =
                DateTimeFormatter.ofPattern(
                        "MMMM dd, yyyy",
                        Locale.ENGLISH
                );

        List<Map<String, Object>> sales =
                new ArrayList<>();

        for (Purchase p : purchases) {

            double amount =
                    p.getBook() != null &&
                            p.getBook().getPrice() != null
                            ? p.getBook().getPrice()
                            : 0.0;

            double royalty =
                    amount * AUTHOR_ROYALTY_RATE;

            totalSales += amount;
            totalRoyalties += royalty;

            boolean pending =
                    p.getPurchasedAt() != null &&
                            p.getPurchasedAt().isAfter(weekAgo);

            if (pending) {
                pendingRoyalties += royalty;
            }

            Map<String, Object> row =
                    new LinkedHashMap<>();

            row.put(
                    "date",
                    p.getPurchasedAt() != null
                            ? dateFmt.format(
                            p.getPurchasedAt()
                            .atZone(
                                    ZoneOffset.UTC))
                            : "—"
            );

            row.put(
                    "pub",
                    p.getBook() != null
                            ? p.getBook().getTitle()
                            : "Publication"
            );

            row.put("units", "1 unit");

            row.put(
                    "amount",
                    "Rs. " +
                            formatMoney(amount)
            );

            row.put(
                    "royalty",
                    "Rs. " +
                            formatMoney(royalty)
            );

            row.put(
                    "status",
                    pending
                            ? "pending"
                            : "paid"
            );

            row.put(
                    "invoice",
                    "#INV-" + p.getId()
            );

            sales.add(row);
        }

        List<Map<String, Object>> metrics =
                new ArrayList<>();

        metrics.add(
                metric(
                        "🪙",
                        "Total Earnings",
                        "Rs. " +
                                formatMoney(totalRoyalties),
                        purchaseRepository
                                .countByAuthorId(author.getId()) +
                                " total sales",
                        "positive"
                )
        );

        metrics.add(
                metric(
                        "📅",
                        "This Month",
                        "Rs. " +
                                formatMoney(monthRoyalties),
                        thisMonth.getMonth()
                                .toString()
                                .substring(0, 1) +
                                thisMonth.getMonth()
                                        .toString()
                                        .substring(1)
                                        .toLowerCase() +
                                " " +
                                thisMonth.getYear() +
                                " accrued",
                        ""
                )
        );

        metrics.add(
                metric(
                        "↺",
                        "Pending Payout",
                        "Rs. " +
                                formatMoney(
                                        pendingRoyalties),
                        "Recent sales awaiting cycle",
                        pendingRoyalties > 0
                                ? "warn"
                                : ""
                )
        );

        metrics.add(
                metric(
                        "🛍",
                        "Total Units Sold",
                        String.valueOf(
                                purchases.size()),
                        "Across your catalog",
                        ""
                )
        );

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("metrics", metrics);
        result.put("sales", sales);

        result.put(
                "availableBalance",
                "Rs. " +
                        formatMoney(pendingRoyalties)
        );

        result.put(
                "totalRoyalties",
                totalRoyalties
        );

        result.put(
                "pendingRoyalties",
                pendingRoyalties
        );

        result.put(
                "authorName",
                author.getName() != null
                        ? author.getName()
                        : "Author"
        );

        result.put(
                "lastPayout",
                sales.isEmpty()
                        ? "—"
                        : sales.get(
                        sales.size() - 1)
                          .get("royalty")
        );

        return result;
    }

    private Map<String, Object> metric(
            String icon,
            String label,
            String value,
            String note,
            String noteType) {

        Map<String, Object> m =
                new LinkedHashMap<>();

        m.put("icon", icon);
        m.put("label", label);
        m.put("value", value);
        m.put("note", note);
        m.put("noteType", noteType);

        return m;
    }

    private String formatMoney(double value) {

        if (value == Math.floor(value)) {

            return String.format(
                    Locale.ENGLISH,
                    "%,.0f",
                    value
            );
        }

        return String.format(
                Locale.ENGLISH,
                "%,.2f",
                value
        );
    }

    // =========================================================
    // READER ANALYTICS
    // =========================================================

    public Map<String, Object> getReaderAnalytics(User author) {

        List<Book> books =
                bookRepository.findByAuthorUserId(author.getId());

        long totalReads =
                readingHistoryRepository
                        .countReadsByAuthorId(author.getId());

        long uniqueReaders =
                readingHistoryRepository
                        .countUniqueReadersByAuthorId(
                                author.getId()
                        );

        long bookmarks =
                bookmarkRepository
                        .countByBookAuthorUserId(
                                author.getId()
                        );

        long highlights =
                highlightRepository
                        .countByBookAuthorUserId(
                                author.getId()
                        );

        Double avgRating =
                reviewRepository
                        .getAverageRatingForAuthor(
                                author.getId()
                        );

        int reviewCount =
                (int) reviewRepository
                        .countByBookAuthorUserId(
                                author.getId()
                        );

        List<Long> bookIds =
                new ArrayList<>();

        for (Book b : books) {
            bookIds.add(b.getId());
        }

        double avgProgress = 0.0;

        int opened = 0;
        int mid = 0;
        int finished = 0;
        int withMarginalia = 0;

        if (!bookIds.isEmpty()) {

            List<ReadingProgress> progressList =
                    readingProgressRepository
                            .findByBookIdIn(bookIds);

            if (!progressList.isEmpty()) {

                int sum = 0;

                for (ReadingProgress p :
                        progressList) {

                    int pct =
                            p.getPercentage();

                    sum += pct;
                    opened++;

                    if (pct >= 50) {
                        mid++;
                    }

                    if (pct >= 100) {
                        finished++;
                    }
                }

                avgProgress =
                        (double) sum /
                                progressList.size();

            } else {

                List<ReadingHistory> histories =
                        readingHistoryRepository
                                .findByAuthorId(
                                        author.getId()
                                );

                if (!histories.isEmpty()) {

                    int sum = 0;

                    for (ReadingHistory h :
                            histories) {

                        int pages =
                                h.getBook() != null &&
                                        h.getBook().getPageCount() != null
                                        ? Math.max(
                                        1,
                                        h.getBook()
                                        .getPageCount())
                                        : 1;

                        int last =
                                h.getLastPage() != null
                                        ? h.getLastPage()
                                        : 1;

                        int pct =
                                Math.min(
                                        100,
                                        Math.round(
                                                100f *
                                                        last /
                                                        pages
                                        )
                                );

                        sum += pct;
                        opened++;

                        if (pct >= 50) {
                            mid++;
                        }

                        if (pct >= 100 ||
                                "COMPLETED"
                                        .equalsIgnoreCase(
                                                h.getStatus())) {

                            finished++;
                        }
                    }

                    avgProgress =
                            (double) sum /
                                    histories.size();
                }
            }

            if (highlights > 0 &&
                    totalReads > 0) {

                withMarginalia =
                        (int) Math.min(
                                100,
                                Math.round(
                                        100.0 *
                                                highlights /
                                                Math.max(
                                                        1,
                                                        totalReads
                                                )
                                )
                        );
            }
        }

        int openedPct =
                totalReads > 0
                        ? (int) Math.min(
                        100,
                        Math.round(
                                100.0 *
                                opened /
                                        Math.max(
                                                1,
                                                totalReads
                                        )
                        )
                )
                        : 0;

        if (opened > 0 &&
                totalReads == 0) {

            openedPct = 100;
        }

        if (totalReads > 0 &&
                opened == 0) {

            openedPct = 100;
            opened = (int) totalReads;
        }

        int midPct =
                opened > 0
                        ? (int) Math.round(
                        100.0 *
                        mid /
                        opened
                )
                        : 0;

        int finishedPct =
                opened > 0
                        ? (int) Math.round(
                        100.0 *
                        finished /
                        opened
                )
                        : 0;

        // -----------------------------------------------------
        // PERFORMANCE
        // -----------------------------------------------------

        List<Map<String, Object>> performance =
                new ArrayList<>();

        Book topBook = null;
        long topReads = -1;

        for (Book b : books) {

            long reads =
                    readingHistoryRepository
                            .countReadsByBookId(
                                    b.getId()
                            );

            long bm =
                    bookmarkRepository
                            .countByBookId(
                                    b.getId()
                            );

            long hl =
                    highlightRepository
                            .countByBookId(
                                    b.getId()
                            );

            if (reads > topReads) {

                topReads = reads;
                topBook = b;
            }

            int bookProgress = 0;

            List<ReadingProgress> bp =
                    readingProgressRepository
                            .findByBookIdIn(
                                    List.of(b.getId())
                            );

            if (!bp.isEmpty()) {

                int sum = 0;

                for (ReadingProgress p : bp) {
                    sum += p.getPercentage();
                }

                bookProgress =
                        sum / bp.size();
            }

            Map<String, Object> row =
                    new LinkedHashMap<>();

            row.put("id", b.getId());
            row.put("title", b.getTitle());

            row.put(
                    "pages",
                    (
                            b.getPageCount() != null
                                    ? b.getPageCount() +
                                      " pages"
                                    : "—"
                    ) +
                            " · " +
                            (
                                    b.getLanguage() != null
                                            ? b.getLanguage()
                                            : "English"
                            )
            );

            row.put(
                    "cat",
                    b.getCategory() != null
                            ? b.getCategory()
                            : "Literature"
            );

            row.put(
                    "reads",
                    String.format(
                            Locale.ENGLISH,
                            "%,d",
                            reads
                    )
            );

            row.put(
                    "progress",
                    bookProgress
            );

            row.put(
                    "bookmarks",
                    String.format(
                            Locale.ENGLISH,
                            "%,d",
                            bm
                    )
            );

            row.put(
                    "highlights",
                    String.format(
                            Locale.ENGLISH,
                            "%,d",
                            hl
                    )
            );

            row.put(
                    "rating",
                    b.getAverageRating() != null
                            ? String.format(
                            Locale.ENGLISH,
                            "%.1f",
                            b.getAverageRating()
                    )
                            : "—"
            );

            row.put(
                    "reviewCount",
                    b.getRatingCount() != null
                            ? String.valueOf(
                            b.getRatingCount())
                            : "0"
            );

            row.put(
                    "image",
                    b.getImage()
            );

            performance.add(row);
        }

        // -----------------------------------------------------
        // SORT PERFORMANCE BY READS
        // -----------------------------------------------------

        for (int i = 0;
             i < performance.size();
             i++) {

            for (int j = i + 1;
                 j < performance.size();
                 j++) {

                long ri =
                        parseCount(
                                String.valueOf(
                                        performance
                                                .get(i)
                                                .get("reads")
                                )
                        );

                long rj =
                        parseCount(
                                String.valueOf(
                                        performance
                                                .get(j)
                                                .get("reads")
                                )
                        );

                if (rj > ri) {

                    Map<String, Object> tmp =
                            performance.get(i);

                    performance.set(
                            i,
                            performance.get(j)
                    );

                    performance.set(
                            j,
                            tmp
                    );
                }
            }
        }

        // -----------------------------------------------------
        // TOP BOOK
        // -----------------------------------------------------

        Map<String, Object> top =
                new LinkedHashMap<>();

        if (topBook != null) {

            top.put("id", topBook.getId());
            top.put("title", topBook.getTitle());
            top.put("author", topBook.getAuthor());

            top.put(
                    "category",
                    topBook.getCategory() != null
                            ? topBook.getCategory()
                            : "Literature"
            );

            top.put("image", topBook.getImage());

            top.put(
                    "pages",
                    topBook.getPageCount() != null
                            ? topBook.getPageCount()
                            : 0
            );

            top.put(
                    "language",
                    topBook.getLanguage() != null
                            ? topBook.getLanguage()
                            : "English"
            );

            top.put(
                    "price",
                    topBook.getPrice() != null
                            ? topBook.getPrice()
                            : 0.0
            );

            top.put(
                    "rating",
                    topBook.getAverageRating() != null
                            ? topBook.getAverageRating()
                            : 0.0
            );

            top.put(
                    "reviewCount",
                    topBook.getRatingCount() != null
                            ? topBook.getRatingCount()
                            : 0
            );

            top.put("reads", topReads);

            top.put(
                    "highlights",
                    highlightRepository
                            .countByBookId(
                                    topBook.getId()
                            )
            );

            Double bookSales =
                    purchaseRepository
                            .sumRoyaltiesByBookId(
                                    topBook.getId()
                            );

            top.put(
                    "royalties",
                    (
                            bookSales != null
                                    ? bookSales
                                    : 0.0
                    ) *
                            AUTHOR_ROYALTY_RATE
            );

            int topProgress = 0;

            List<ReadingProgress> tp =
                    readingProgressRepository
                            .findByBookIdIn(
                                    List.of(
                                            topBook.getId()
                                    )
                            );

            if (!tp.isEmpty()) {

                int sum = 0;

                for (ReadingProgress p : tp) {
                    sum += p.getPercentage();
                }

                topProgress =
                        sum / tp.size();
            }

            top.put(
                    "avgCompletion",
                    topProgress
            );

            double catalogShare =
                    totalReads > 0
                            ? (
                            100.0 *
                            topReads /
                            totalReads
                    )
                            : 0.0;

            top.put(
                    "catalogShare",
                    Math.round(catalogShare)
            );
        }

        // -----------------------------------------------------
        // MILESTONES
        // -----------------------------------------------------

        List<Map<String, Object>> milestones =
                new ArrayList<>();

        milestones.add(
                milestone(
                        "Opened & Read Chapter 1 (Intake)",
                        openedPct > 0
                                ? openedPct
                                : (
                                totalReads > 0
                                ? 100
                                : 0
                        ),
                        false
                )
        );

        milestones.add(
                milestone(
                        "Reached Midpoint (50% Completion)",
                        midPct,
                        false
                )
        );

        milestones.add(
                milestone(
                        "Finished Full Book (100% Completion)",
                        finishedPct,
                        false
                )
        );

        milestones.add(
                milestone(
                        "Saved Marginalia / Quotations",
                        withMarginalia,
                        true
                )
        );

        // -----------------------------------------------------
        // REGIONS / DEVICES
        // -----------------------------------------------------

        List<Map<String, Object>> regions =
                new ArrayList<>();

        regions.add(
                demoRow(
                        "Regional data",
                        "Not tracked"
                )
        );

        List<Map<String, Object>> modality =
                new ArrayList<>();

        modality.add(
                demoRow(
                        "Device data",
                        "Not tracked"
                )
        );

        // -----------------------------------------------------
        // STATS
        // -----------------------------------------------------

        Map<String, Object> stats =
                new LinkedHashMap<>();

        stats.put(
                "totalReads",
                totalReads
        );

        stats.put(
                "uniqueReaders",
                uniqueReaders
        );

        stats.put(
                "avgProgress",
                Math.round(
                        avgProgress * 10.0
                ) / 10.0
        );

        stats.put(
                "averageRating",
                avgRating != null
                        ? Math.round(
                        avgRating * 10.0
                ) / 10.0
                        : 0.0
        );

        stats.put(
                "reviewCount",
                reviewCount
        );

        stats.put(
                "bookmarks",
                bookmarks
        );

        stats.put(
                "highlights",
                highlights
        );

        // -----------------------------------------------------
        // FINAL RESULT
        // -----------------------------------------------------

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "stats",
                stats
        );

        result.put(
                "topBook",
                top.isEmpty()
                        ? null
                        : top
        );

        result.put(
                "performance",
                performance
        );

        result.put(
                "milestones",
                milestones
        );

        result.put(
                "regions",
                regions
        );

        result.put(
                "modality",
                modality
        );

        return result;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Map<String, Object> milestone(
            String label,
            int value,
            boolean gold) {

        Map<String, Object> m =
                new LinkedHashMap<>();

        m.put("label", label);
        m.put("value", value);
        m.put("gold", gold);

        return m;
    }

    private Map<String, Object> demoRow(
            String label,
            String value) {

        Map<String, Object> m =
                new HashMap<>();

        m.put("label", label);
        m.put("value", value);

        return m;
    }

    private String formatTimeAgo(
            Instant instant) {

        if (instant == null) {
            return "Recently";
        }

        Duration duration =
                Duration.between(
                        instant,
                        Instant.now()
                );

        long seconds =
                duration.getSeconds();

        if (seconds < 60) {
            return "Just now";
        }

        long minutes =
                seconds / 60;

        if (minutes < 60) {
            return minutes + " min ago";
        }

        long hours =
                minutes / 60;

        if (hours < 24) {

            return hours +
                    " hour" +
                    (hours > 1 ? "s" : "") +
                    " ago";
        }

        long days =
                hours / 24;

        if (days == 1) {
            return "Yesterday";
        }

        if (days < 7) {
            return days + " days ago";
        }

        long weeks =
                days / 7;

        return weeks +
                " week" +
                (weeks > 1 ? "s" : "") +
                " ago";
    }

    private String getInitials(String name) {

        if (name == null ||
                name.isBlank()) {

            return "RD";
        }

        String[] parts =
                name.trim().split("\\s+");

        if (parts.length == 1) {

            return parts[0]
                    .substring(
                            0,
                            Math.min(
                                    2,
                                    parts[0].length()
                            )
                    )
                    .toUpperCase();
        }

        return (
                "" +
                        parts[0].charAt(0) +
                        parts[parts.length - 1].charAt(0)
        ).toUpperCase();
    }

    private long parseCount(String formatted) {

        try {

            return Long.parseLong(
                    formatted
                            .replace(",", "")
                            .trim()
            );

        } catch (Exception e) {

            return 0;
        }
    }
}