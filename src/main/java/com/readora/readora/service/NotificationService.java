package com.readora.readora.service;

import com.readora.readora.dto.NotificationResponse;
import com.readora.readora.model.Notification;
import com.readora.readora.model.User;
import com.readora.readora.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationBroadcastService broadcastService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationBroadcastService broadcastService) {
        this.notificationRepository = notificationRepository;
        this.broadcastService = broadcastService;
    }

    public List<NotificationResponse> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAsRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(n -> !n.isRead())
                .toList();
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    public Notification createNotification(User user, String title, String message, String type, String link) {
        Notification notification = new Notification(user, title, message, type, link);
        Notification saved = notificationRepository.save(notification);
        broadcastService.pushToUser(user.getId(), saved);
        return saved;
    }
}