package com.readora.readora.dto;

import jakarta.validation.constraints.NotBlank;

public class SubscriptionRequest {

    /*
     * Kept because the old frontend may send email.
     *
     * The backend does NOT trust this email
     * to identify the logged-in user.
     */
    private String email;

    @NotBlank(
            message = "Subscription plan is required."
    )
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