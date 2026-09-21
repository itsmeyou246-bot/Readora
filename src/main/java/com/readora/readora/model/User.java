package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(
                        name = "idx_users_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_users_role",
                        columnList = "role"
                )
        }
)
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @Column(
            nullable = false,
            length = 100
    )
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Role role;

    @Column(length = 50)
    private String currentMood = "Calm";

    private Instant createdAt = Instant.now();

    public User() {
    }

    public User(
            String name,
            String email,
            String password,
            Role role) {

        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.currentMood = "Calm";
        this.createdAt = Instant.now();
    }

    @Column(length = 20)
    private String authorStatus = "ACTIVE"; // ACTIVE | UNDER_REVIEW | SUSPENDED — meaningful for role=AUTHOR

    public String getAuthorStatus() {
        return authorStatus;
    }

    public void setAuthorStatus(String authorStatus) {
        this.authorStatus = authorStatus != null ? authorStatus : "ACTIVE";
    }

    public Long getId() {
        return id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getCurrentMood() {
        return currentMood;
    }

    public void setCurrentMood(String currentMood) {
        this.currentMood = currentMood != null ? currentMood : "Calm";
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}