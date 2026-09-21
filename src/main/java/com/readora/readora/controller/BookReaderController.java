package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.model.BookPage;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookPageRepository;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.service.LibraryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class BookReaderController {

    private final BookRepository books;
    private final BookPageRepository pages;
    private final LibraryService libraryService;

    public BookReaderController(
            BookRepository books,
            BookPageRepository pages,
            LibraryService libraryService
    ) {
        this.books = books;
        this.pages = pages;
        this.libraryService = libraryService;
    }

    // =========================================================
    // OPEN READER
    // =========================================================

    @GetMapping({"/book-reader", "/book-reader.html"})
    public String readerPage(
            @RequestParam("bookId") Long bookId,
            Model model
    ) {

        Book book = findBook(bookId);

        model.addAttribute("bookId", book.getId());
        model.addAttribute("bookTitle", book.getTitle());
        model.addAttribute("bookAuthor", book.getAuthor());
        model.addAttribute("premium", isEffectivelyPremium(book));

        return "book-reader";
    }

    // =========================================================
    // CHECK READER ACCESS
    // =========================================================

    @GetMapping("/api/reader/{bookId}/access")
    @ResponseBody
    public ResponseEntity<?> access(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        Book book = findBook(bookId);

        // ---------------------------------------------------------
        // IMPORTANT:
        // Only published books can be read publicly.
        // ---------------------------------------------------------

        if (!"published".equalsIgnoreCase(book.getStatus())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    "This book has not been published yet."
                            )
                    );
        }

        boolean premium = isEffectivelyPremium(book);

        // ---------------------------------------------------------
        // PREMIUM BOOK
        // ---------------------------------------------------------

        if (premium) {

            User user = currentUser(authentication);

            if (!libraryService.owns(user, bookId)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                Map.of(
                                        "message",
                                        "This book is not unlocked for your account.",
                                        "premium",
                                        true,
                                        "unlocked",
                                        false
                                )
                        );
            }

            long pageCount = pages.countByBookId(bookId);

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put("bookId", book.getId());
            response.put("title", book.getTitle());
            response.put("author", book.getAuthor());
            response.put("premium", true);
            response.put("unlocked", true);
            response.put(
                    "lastReadPage",
                    libraryService.currentPage(user, book)
            );
            response.put("pageCount", pageCount);
            response.put("contentMode",
                    pageCount > 0 ? "pages" : "pdf");
            response.put(
                    "fileUrl",
                    normalizeFileUrl(book.getFilePath())
            );

            return ResponseEntity.ok(response);
        }

        // ---------------------------------------------------------
        // FREE BOOK
        // ---------------------------------------------------------

        long pageCount =
                pages.countByBookId(bookId);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("bookId", book.getId());
        response.put("title", book.getTitle());
        response.put("author", book.getAuthor());
        response.put("premium", false);
        response.put("unlocked", true);

        int lastReadPage = 1;
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            try {
                User user = libraryService.user(authentication.getName());
                lastReadPage = libraryService.currentPage(user, book);
                if (lastReadPage < 1) {
                    lastReadPage = 1;
                }
            } catch (Exception ignored) {
                lastReadPage = 1;
            }
        }

        response.put("lastReadPage", lastReadPage);
        response.put("pageCount", pageCount);

        /*
         * If BookPage records exist, use the normal
         * READORA page reader.
         *
         * If the author uploaded a PDF but there are
         * no BookPage records, use the uploaded PDF.
         */
        response.put(
                "contentMode",
                pageCount > 0 ? "pages" : "pdf"
        );

        response.put(
                "fileUrl",
                normalizeFileUrl(book.getFilePath())
        );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // LOAD READER PAGE
    // =========================================================

    @GetMapping("/api/reader/{bookId}/pages/{pageNumber}")
    @ResponseBody
    public ResponseEntity<?> page(
            @PathVariable Long bookId,
            @PathVariable int pageNumber,
            Authentication authentication
    ) {

        Book book = findBook(bookId);

        // ---------------------------------------------------------
        // ONLY PUBLISHED BOOKS
        // ---------------------------------------------------------

        if (!"published".equalsIgnoreCase(book.getStatus())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    "This book has not been published yet."
                            )
                    );
        }

        User user = null;

        // ---------------------------------------------------------
        // PREMIUM ACCESS
        // ---------------------------------------------------------

        if (isEffectivelyPremium(book)) {

            // For premium books, require authenticated user who owns the book
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getName() == null
                    || "anonymousUser".equals(authentication.getName())) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                Map.of(
                                        "message",
                                        "Login is required to read this premium book."
                                )
                        );
            }

            try {
                user = libraryService.user(authentication.getName());
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                Map.of(
                                        "message",
                                        "Login is required to read this premium book."
                                )
                        );
            }

            if (!libraryService.owns(user, bookId)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                Map.of(
                                        "message",
                                        "This book is not unlocked for your account."
                                )
                        );
            }

        } else {

            // -----------------------------------------------------
            // FREE BOOK - authentication is optional
            // -----------------------------------------------------

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getName() != null
                    && !"anonymousUser".equals(authentication.getName())) {

                try {

                    user =
                            libraryService.user(
                                    authentication.getName()
                            );

                } catch (Exception ignored) {

                    user = null;
                }
            }
        }

        // ---------------------------------------------------------
        // PAGE NUMBER
        // ---------------------------------------------------------

        if (pageNumber < 1) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Page number must be at least 1."
                            )
                    );
        }

        // ---------------------------------------------------------
        // FIND BOOK PAGE
        // ---------------------------------------------------------

        BookPage bookPage =
                pages.findByBookIdAndPageNumber(
                                bookId,
                                pageNumber
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Page not found."
                                        )
                        );

        long pageCount =
                pages.countByBookId(bookId);

        // ---------------------------------------------------------
        // SAVE READING PROGRESS
        // ---------------------------------------------------------

        if (user != null) {

            libraryService.saveProgress(
                    user,
                    book,
                    pageNumber
            );
        }

        // ---------------------------------------------------------
        // RESPONSE
        // ---------------------------------------------------------

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "pageId",
                bookPage.getPageId()
        );

        response.put(
                "bookId",
                bookId
        );

        response.put(
                "title",
                book.getTitle()
        );

        response.put(
                "author",
                book.getAuthor()
        );

        response.put(
                "pageNumber",
                bookPage.getPageNumber()
        );

        response.put(
                "pageCount",
                pageCount
        );

        response.put(
                "chapterLabel",
                bookPage.getChapterLabel()
        );

        response.put(
                "textContent",
                bookPage.getTextContent()
        );

        response.put(
                "imageUrl",
                bookPage.getImageUrl()
        );

        response.put(
                "imageTitle",
                bookPage.getImageTitle()
        );

        response.put(
                "imageSubtitle",
                bookPage.getImageSubtitle()
        );

        response.put(
                "imageCaption",
                bookPage.getImageCaption()
        );

        response.put(
                "imagePhotoCredit",
                bookPage.getImagePhotoCredit()
        );

        response.put(
                "hasPrevious",
                pageNumber > 1
        );

        response.put(
                "hasNext",
                pageNumber < pageCount
        );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Book findBook(Long bookId) {

        return books.findById(bookId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Book not found."
                                )
                );
    }

    private boolean isEffectivelyPremium(Book book) {

        return book.isPremium()
                && book.getPrice() != null
                && book.getPrice() > 0;
    }

    private User currentUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login is required to read this premium book."
            );
        }

        return libraryService.user(
                authentication.getName()
        );
    }

    private String normalizeFileUrl(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            return null;
        }

        String value =
                filePath.trim().replace("\\", "/");

        // Uploaded files already return:
        // /uploads/manuscripts/filename.pdf
        if (value.startsWith("/uploads/")) {
            return value;
        }

        if (value.startsWith("uploads/")) {
            return "/" + value;
        }

        // Old project files may use:
        // books/file.pdf
        if (value.startsWith("/books/")) {
            return value;
        }

        if (value.startsWith("books/")) {
            return "/" + value;
        }

        // Static resource path
        if (value.startsWith("/")) {
            return value;
        }

        // Bare filename (e.g. "sample.pdf") with no path prefix
        // → serve from the static books directory
        return "/books/" + value;
    }
}