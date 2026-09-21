package com.readora.readora.controller;

import com.readora.readora.model.Book;
import com.readora.readora.model.Purchase;
import com.readora.readora.model.User;
import com.readora.readora.repository.PurchaseRepository;
import com.readora.readora.security.JwtService;
import com.readora.readora.service.LibraryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class LibraryController {

    private final JwtService jwt;
    private final LibraryService library;
    private final PurchaseRepository purchases;

    public LibraryController(
            JwtService jwt,
            LibraryService library,
            PurchaseRepository purchases
    ) {
        this.jwt = jwt;
        this.library = library;
        this.purchases = purchases;
    }

    /**
     * =========================================================
     * MY LIBRARY
     * =========================================================
     *
     * GET /api/library
     *
     * Returns:
     *
     * {
     *   reading: [],
     *   saved: [],
     *   completed: [],
     *   purchased: []
     * }
     */
    @GetMapping("/library")
    public ResponseEntity<?> getLibrary(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String auth,

            @RequestParam(
                    value = "categorized",
                    defaultValue = "true"
            )
            boolean categorized,

            Authentication authentication
    ) {

        Optional<User> user =
                resolveUser(
                        auth,
                        authentication
                );

        if (user.isEmpty()) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Please log in to view your library."
                            )
                    );
        }

        try {

            if (categorized) {

                return ResponseEntity.ok(
                        library.categorizedLibrary(
                                user.get()
                        )
                );
            }

            return ResponseEntity.ok(
                    library.library(
                            user.get()
                    )
            );

        } catch (Exception ex) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load your library.",
                                    "error",
                                    ex.getMessage() != null
                                            ? ex.getMessage()
                                            : "Unknown error"
                            )
                    );
        }
    }

    /**
     * =========================================================
     * LEGACY PURCHASE ENDPOINT
     * =========================================================
     *
     * Kept so existing frontend code does not break.
     *
     * IMPORTANT:
     * Actual premium ownership should still be created
     * after successful eSewa verification.
     */
    @PostMapping("/purchases")
    public ResponseEntity<?> purchase(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String auth,

            @RequestBody
            Map<String, Object> request,

            Authentication authentication
    ) {

        Optional<User> authenticated =
                resolveUser(
                        auth,
                        authentication
                );

        if (authenticated.isEmpty()) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Please log in before purchasing."
                            )
                    );
        }

        Object rawBooks =
                request != null
                        ? request.get("books")
                        : null;

        if (!(rawBooks instanceof List<?> bookList)
                ||
                bookList.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "The purchase contains no books."
                            )
                    );
        }

        User user =
                authenticated.get();

        int created =
                0;

        for (Object rawBook :
                bookList) {

            if (!(rawBook instanceof Map<?, ?> rawMap)) {
                continue;
            }

            Map<String, Object> bookData =
                    new HashMap<>();

            rawMap.forEach(
                    (key, value) ->
                            bookData.put(
                                    String.valueOf(key),
                                    value
                            )
            );

            try {

                Book book =
                        library.bookForPurchase(
                                bookData
                        );

                if (!library.owns(
                        user,
                        book.getId()
                )) {

                    purchases.save(
                            new Purchase(
                                    user,
                                    book
                            )
                    );

                    created++;
                }

            } catch (Exception ignored) {
                // One invalid item must not break
                // all remaining purchase records.
            }
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Purchase recorded.",
                        "created",
                        created
                )
        );
    }

    /**
     * Resolve logged-in user from normal Spring Security
     * authentication first, then JWT Authorization header.
     */
    private Optional<User> resolveUser(
            String auth,
            Authentication authentication
    ) {

        /*
         * Spring Security authentication.
         */
        if (
                authentication != null
                        &&
                        authentication.isAuthenticated()
                        &&
                        !"anonymousUser".equals(
                                authentication.getName()
                        )
        ) {

            try {

                return Optional.of(
                        library.user(
                                authentication.getName()
                        )
                );

            } catch (Exception ignored) {
            }
        }

        /*
         * JWT fallback.
         */
        if (
                auth != null &&
                        !auth.isBlank()
        ) {

            try {

                return jwt
                        .extractEmail(auth)
                        .map(
                                library::user
                        );

            } catch (Exception ignored) {
            }
        }

        return Optional.empty();
    }
}