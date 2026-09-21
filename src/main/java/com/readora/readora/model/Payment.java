package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_user", columnList = "user_id"),
        @Index(name = "idx_payment_gateway_ref", columnList = "gateway_reference")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // SUBSCRIPTION, BOOK_PURCHASE, or CART_PURCHASE
    @Column(nullable = false, length = 20)
    private String paymentType;

    // Set when paymentType = SUBSCRIPTION (FREE/PREMIUM/INSTITUTIONAL)
    @Column(length = 30)
    private String plan;

    // Set when paymentType = BOOK_PURCHASE (single book)
    private Long bookId;

    // NEW: set when paymentType = CART_PURCHASE - comma-separated book IDs,
    // since a cart can contain more than one book and Payment previously only
    // had room for a single bookId.
    @Column(name = "cart_book_ids", length = 500)
    private String cartBookIds;

    @Column(nullable = false)
    private Double amount;

    // KHALTI or ESEWA
    @Column(nullable = false, length = 20)
    private String gateway;

    // PENDING, SUCCESS, FAILED
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    // Khalti pidx, or eSewa transaction_uuid
    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant completedAt;

    protected Payment() {
    }

    public Payment(User user, String paymentType, String plan, Long bookId,
                   Double amount, String gateway, String gatewayReference) {
        this.user = user;
        this.paymentType = paymentType;
        this.plan = plan;
        this.bookId = bookId;
        this.amount = amount;
        this.gateway = gateway;
        this.gatewayReference = gatewayReference;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getPaymentType() { return paymentType; }
    public String getPlan() { return plan; }
    public Long getBookId() { return bookId; }
    public String getCartBookIds() { return cartBookIds; }
    public void setCartBookIds(String cartBookIds) { this.cartBookIds = cartBookIds; }
    public Double getAmount() { return amount; }
    public String getGateway() { return gateway; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGatewayReference() { return gatewayReference; }
    public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}