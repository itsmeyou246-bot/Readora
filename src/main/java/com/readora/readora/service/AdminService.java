package com.readora.readora.service;

import com.readora.readora.dto.AdminAuthorResponse;
import com.readora.readora.dto.AdminAuthorStats;
import com.readora.readora.dto.AdminPlatformStats;
import com.readora.readora.dto.AdminSubscriptionResponse;
import com.readora.readora.dto.AdminSubscriptionStats;
import com.readora.readora.dto.AdminUserResponse;
import com.readora.readora.dto.BookResponse;
import com.readora.readora.model.Book;
import com.readora.readora.model.Role;
import com.readora.readora.model.Subscription;
import com.readora.readora.model.User;
import com.readora.readora.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReviewRepository reviewRepository;

    public AdminService(UserRepository userRepository,
                        BookRepository bookRepository,
                        SubscriptionRepository subscriptionRepository,
                        PurchaseRepository purchaseRepository,
                        ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.purchaseRepository = purchaseRepository;
        this.reviewRepository = reviewRepository;
    }

    public AdminPlatformStats getPlatformStats() {
        long totalUsers = userRepository.count();
        long totalAuthors = userRepository.countByRole(Role.AUTHOR);
        long totalBooks = bookRepository.count();
        long totalSubscriptions = subscriptionRepository.count();
        long totalPurchases = purchaseRepository.count();
        long totalReviews = reviewRepository.count();

        return new AdminPlatformStats(totalUsers, totalAuthors, totalBooks,
                totalSubscriptions, totalPurchases, totalReviews);
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::fromEntity)
                .toList();
    }

    public AdminUserResponse changeUserRole(Long userId, String newRoleStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        Role newRole;
        try {
            newRole = Role.valueOf(newRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role name.");
        }

        user.setRole(newRole);
        User saved = userRepository.save(user);
        return AdminUserResponse.fromEntity(saved);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));
        bookRepository.delete(book);
    }

    // =========================================================
    // SUBSCRIPTION MANAGEMENT
    // =========================================================

    public List<AdminSubscriptionResponse> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();

        List<Long> userIds = subscriptions.stream()
                .map(Subscription::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return subscriptions.stream()
                .map(sub -> AdminSubscriptionResponse.fromEntity(sub, userMap.get(sub.getUserId())))
                .sorted(Comparator.comparing(AdminSubscriptionResponse::getId).reversed())
                .toList();
    }

    public AdminSubscriptionStats getSubscriptionStats() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();

        long total = subscriptions.size();

        long active = subscriptions.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .count();

        long free = subscriptions.stream()
                .filter(s -> "FREE".equalsIgnoreCase(s.getPlan()))
                .count();

        long premium = subscriptions.stream()
                .filter(s -> "PREMIUM".equalsIgnoreCase(s.getPlan()))
                .count();

        long institutional = subscriptions.stream()
                .filter(s -> "INSTITUTIONAL".equalsIgnoreCase(s.getPlan()))
                .count();

        double mrr = subscriptions.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .mapToDouble(s -> s.getAmount() == null ? 0.0 : s.getAmount())
                .sum();

        return new AdminSubscriptionStats(total, active, free, premium, institutional, mrr);
    }

    public AdminSubscriptionResponse updateSubscriptionStatus(Long subscriptionId, String statusStr) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found."));

        String normalized = normalizeStatus(statusStr);
        subscription.setStatus(normalized);

        Subscription saved = subscriptionRepository.save(subscription);
        User user = userRepository.findById(saved.getUserId()).orElse(null);

        return AdminSubscriptionResponse.fromEntity(saved, user);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required.");
        }

        String value = status.trim().toUpperCase();

        return switch (value) {
            case "ACTIVE", "EXPIRED", "CANCELLED" -> value;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status.");
        };
    }

    // =========================================================
    // AUTHOR MANAGEMENT
    // =========================================================

    public List<AdminAuthorResponse> getAllAuthors() {
        List<User> authors = userRepository.findByRole(Role.AUTHOR);

        return authors.stream()
                .map(author -> {
                    long total = bookRepository.countByAuthorUserId(author.getId());
                    long published = bookRepository.findByAuthorUserId(author.getId()).stream()
                            .filter(b -> "published".equalsIgnoreCase(b.getStatus()))
                            .count();
                    return AdminAuthorResponse.fromEntity(author, total, published);
                })
                .sorted(Comparator.comparing(AdminAuthorResponse::getTotalBooks).reversed())
                .toList();
    }

    public AdminAuthorStats getAuthorStats() {
        List<User> authors = userRepository.findByRole(Role.AUTHOR);

        long total = authors.size();
        long active = authors.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getAuthorStatus()))
                .count();
        long publishedContent = authors.stream()
                .mapToLong(a -> bookRepository.findByAuthorUserId(a.getId()).stream()
                        .filter(b -> "published".equalsIgnoreCase(b.getStatus()))
                        .count())
                .sum();

        return new AdminAuthorStats(total, active, publishedContent);
    }

    public List<BookResponse> getAuthorBooks(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found."));

        if (author.getRole() != Role.AUTHOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user is not an author.");
        }

        return bookRepository.findByAuthorUserId(authorId).stream()
                .map(BookResponse::fromEntity)
                .toList();
    }

    public AdminAuthorResponse updateAuthorStatus(Long authorId, String statusStr) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found."));

        if (author.getRole() != Role.AUTHOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user is not an author.");
        }

        String normalized = normalizeAuthorStatus(statusStr);
        author.setAuthorStatus(normalized);
        User saved = userRepository.save(author);

        long total = bookRepository.countByAuthorUserId(saved.getId());
        long published = bookRepository.findByAuthorUserId(saved.getId()).stream()
                .filter(b -> "published".equalsIgnoreCase(b.getStatus()))
                .count();

        return AdminAuthorResponse.fromEntity(saved, total, published);
    }

    private String normalizeAuthorStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required.");
        }
        String value = status.trim().toUpperCase();
        return switch (value) {
            case "ACTIVE", "UNDER_REVIEW", "SUSPENDED" -> value;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author status.");
        };
    }
}