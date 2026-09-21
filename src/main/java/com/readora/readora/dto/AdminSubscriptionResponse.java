package com.readora.readora.dto;

import com.readora.readora.model.Subscription;
import com.readora.readora.model.User;

import java.time.LocalDate;

public class AdminSubscriptionResponse {

    private Long id;
    private String subscriptionCode;
    private Long userId;
    private String userName;
    private String userEmail;
    private String plan;
    private Double amount;
    private String paymentMethod;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    public AdminSubscriptionResponse() {
    }

    public static AdminSubscriptionResponse fromEntity(Subscription sub, User user) {
        AdminSubscriptionResponse resp = new AdminSubscriptionResponse();
        resp.setId(sub.getId());
        resp.setSubscriptionCode(String.format("SUB-%06d", sub.getId()));
        resp.setUserId(sub.getUserId());
        resp.setUserName(user != null ? user.getName() : "Unknown User");
        resp.setUserEmail(user != null ? user.getEmail() : "—");
        resp.setPlan(sub.getPlan());
        resp.setAmount(sub.getAmount());
        resp.setPaymentMethod(sub.getPaymentMethod());
        resp.setStatus(sub.getStatus());
        resp.setStartDate(sub.getStartDate());
        resp.setEndDate(sub.getEndDate());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubscriptionCode() { return subscriptionCode; }
    public void setSubscriptionCode(String subscriptionCode) { this.subscriptionCode = subscriptionCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}