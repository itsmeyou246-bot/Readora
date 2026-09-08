package com.readora.readora.dto;

public class SubscriptionRequest {

    private String email;
    private String plan;

    public SubscriptionRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
}