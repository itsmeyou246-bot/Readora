package com.readora.readora.dto;

public class LoginResponse {

    private String message;
    private String token;
    private String name;
    private String email;
    private String role;
    private String dashboard;
    private boolean emailSent;

    public LoginResponse(
            String message,
            String token,
            String name,
            String email,
            String role,
            String dashboard,
            boolean emailSent
    ) {
        this.message = message;
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
        this.dashboard = dashboard;
        this.emailSent = emailSent;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getDashboard() {
        return dashboard;
    }

    public boolean isEmailSent() {
        return emailSent;
    }
}