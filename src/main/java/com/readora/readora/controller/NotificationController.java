package com.readora.readora.controller;

import com.readora.readora.dto.NotificationResponse;
import com.readora.readora.model.User;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final LibraryService libraryService;

    public NotificationController(NotificationService notificationService,
                                  LibraryService libraryService) {
        this.notificationService = notificationService;
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(notificationService.getUserNotifications(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = currentUser(authentication);
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id,
                                                          Authentication authentication) {
        User user = currentUser(authentication);
        notificationService.markAsRead(user, id);
        return ResponseEntity.ok(Map.of("message", "Marked as read."));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        User user = currentUser(authentication);
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read."));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
