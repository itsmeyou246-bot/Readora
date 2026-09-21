package com.readora.readora.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class PaymentInitiateRequest {

    @NotBlank
    private String paymentType; // "SUBSCRIPTION", "BOOK_PURCHASE", or "CART_PURCHASE"

    // Required when paymentType = SUBSCRIPTION
    private String plan;

    // Required when paymentType = BOOK_PURCHASE
    private Long bookId;

    // Required when paymentType = CART_PURCHASE
    private List<Long> bookIds;

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public List<Long> getBookIds() { return bookIds; }
    public void setBookIds(List<Long> bookIds) { this.bookIds = bookIds; }
}