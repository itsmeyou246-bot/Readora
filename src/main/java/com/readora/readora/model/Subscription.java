package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_user", columnList = "user_id"),
                @Index(name = "idx_subscription_status", columnList = "status")
        }
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 30)
    private String plan;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // NEW: billing amount for this subscription (per period, in NPR)
    @Column(nullable = false)
    private Double amount = 0.0;

    // NEW: gateway used to pay (eSewa, Khalti, Complimentary, etc.)
    @Column(length = 40)
    private String paymentMethod = "Not Specified";

    public Subscription() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}