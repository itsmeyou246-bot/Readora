package com.readora.readora.dto;

import com.readora.readora.model.Notification;
import java.time.Instant;

public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String type;
    private String link;
    private boolean isRead;
    private Instant createdAt;

    public NotificationResponse() {
    }

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(notification.getId());
        resp.setTitle(notification.getTitle());
        resp.setMessage(notification.getMessage());
        resp.setType(notification.getType());
        resp.setLink(notification.getLink());
        resp.setRead(notification.isRead());
        resp.setCreatedAt(notification.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
