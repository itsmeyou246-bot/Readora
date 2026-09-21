package com.readora.readora.controller;

import com.readora.readora.dto.AdminAnalyticsResponse;
import com.readora.readora.model.User;
import com.readora.readora.service.AdminInsightsService;
import com.readora.readora.service.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiExtraController {

    private final AdminInsightsService adminInsightsService;
    private final LibraryService libraryService;

    public AdminApiExtraController(
            AdminInsightsService adminInsightsService,
            LibraryService libraryService) {

        this.adminInsightsService = adminInsightsService;
        this.libraryService = libraryService;
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    "Authentication is required."
                            )
                    );
        }

        User admin =
                libraryService.user(
                        authentication.getName()
                );

        adminInsightsService.deleteUserAccount(
                id,
                admin.getId()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User account removed."
                )
        );
    }

    // =========================================================
    // ADMIN ANALYTICS
    // =========================================================

    @GetMapping("/api/admin/analytics/overview")
    public ResponseEntity<AdminAnalyticsResponse>
    getAnalyticsOverview() {

        return ResponseEntity.ok(
                adminInsightsService.getOverview()
        );
    }
}