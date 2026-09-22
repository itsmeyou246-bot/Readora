package com.readora.readora.controller;

import com.readora.readora.dto.AdminAuthorResponse;
import com.readora.readora.dto.AdminAuthorStats;
import com.readora.readora.dto.AdminPlatformStats;
import com.readora.readora.dto.AdminSubscriptionResponse;
import com.readora.readora.dto.AdminSubscriptionStats;
import com.readora.readora.dto.AdminUserResponse;
import com.readora.readora.dto.BookResponse;
import com.readora.readora.model.Book;
import com.readora.readora.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // =========================
    // ADMIN DASHBOARD STATISTICS
    // =========================

    @GetMapping("/stats")
    public ResponseEntity<AdminPlatformStats> getStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    // =========================
    // USER MANAGEMENT
    // =========================

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> changeRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String role = body.get("role");

        return ResponseEntity.ok(
                adminService.changeUserRole(id, role)
        );
    }

    // =========================
    // BOOK MANAGEMENT
    // =========================

    @GetMapping("/books")
    public ResponseEntity<List<BookResponse>> getBooks() {

        List<Book> books = adminService.getAllBooks();

        List<BookResponse> response = books.stream()
                .map(BookResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(
            @PathVariable Long id) {

        adminService.deleteBook(id);

        return ResponseEntity.ok(
                Map.of("message", "Book deleted by administrator.")
        );
    }

    // =========================
    // SUBSCRIPTION MANAGEMENT
    // =========================

    @GetMapping("/subscriptions")
    public ResponseEntity<List<AdminSubscriptionResponse>> getSubscriptions() {
        return ResponseEntity.ok(adminService.getAllSubscriptions());
    }

    @GetMapping("/subscriptions/stats")
    public ResponseEntity<AdminSubscriptionStats> getSubscriptionStats() {
        return ResponseEntity.ok(adminService.getSubscriptionStats());
    }

    @PutMapping("/subscriptions/{id}/status")
    public ResponseEntity<AdminSubscriptionResponse> updateSubscriptionStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(
                adminService.updateSubscriptionStatus(id, body.get("status"))
        );
    }

    // =========================
    // AUTHOR MANAGEMENT
    // =========================

    @GetMapping("/authors")
    public ResponseEntity<List<AdminAuthorResponse>> getAuthors() {
        return ResponseEntity.ok(adminService.getAllAuthors());
    }

    @GetMapping("/authors/stats")
    public ResponseEntity<AdminAuthorStats> getAuthorStats() {
        return ResponseEntity.ok(adminService.getAuthorStats());
    }

    @GetMapping("/authors/{id}/books")
    public ResponseEntity<List<BookResponse>> getAuthorBooks(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAuthorBooks(id));
    }

    @PutMapping("/authors/{id}/status")
    public ResponseEntity<AdminAuthorResponse> updateAuthorStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(
                adminService.updateAuthorStatus(id, body.get("status"))
        );
    }
}