package com.readora.readora.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(nullable = false, length = 20)
    private String gateway;

    // Khalti's transaction_id, or eSewa's transaction_code / ref_id
    @Column(length = 100)
    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String responseMessage;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Transaction() {
    }

    public Transaction(Payment payment, String gateway, String referenceId,
                       String responseMessage, String status) {
        this.payment = payment;
        this.gateway = gateway;
        this.referenceId = referenceId;
        this.responseMessage = responseMessage;
        this.status = status;
    }

    public Long getId() { return id; }
    public Payment getPayment() { return payment; }
    public String getGateway() { return gateway; }
    public String getReferenceId() { return referenceId; }
    public String getResponseMessage() { return responseMessage; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}