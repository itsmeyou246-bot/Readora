package com.readora.readora.dto;

import com.readora.readora.model.User;
import java.time.Instant;

public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Instant createdAt;

    public AdminUserResponse() {
    }

    public static AdminUserResponse fromEntity(User user) {
        AdminUserResponse resp = new AdminUserResponse();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole().name());
        resp.setCreatedAt(user.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
