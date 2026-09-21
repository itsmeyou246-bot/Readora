package com.readora.readora.service;

import com.readora.readora.dto.NotificationResponse;
import com.readora.readora.model.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void pushToUser(Long userId, Notification notification) {
        if (userId == null || notification == null) return;
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                NotificationResponse.fromEntity(notification)
        );
    }
}