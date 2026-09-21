package com.readora.readora.controller;

import com.readora.readora.dto.PaymentInitResponse;
import com.readora.readora.dto.PaymentInitiateRequest;
import com.readora.readora.model.Payment;
import com.readora.readora.model.User;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final LibraryService libraryService;

    public PaymentController(PaymentService paymentService, LibraryService libraryService) {
        this.paymentService = paymentService;
        this.libraryService = libraryService;
    }

    @PostMapping("/esewa/initiate")
    public ResponseEntity<PaymentInitResponse> initiate(
            @Valid @RequestBody PaymentInitiateRequest request,
            Authentication authentication) {

        User user = currentUser(authentication);
        return ResponseEntity.ok(paymentService.initiate(
                user, request.getPaymentType().toUpperCase(), request.getPlan(),
                request.getBookId(), request.getBookIds()
        ));
    }

    // eSewa redirects the browser here with ?data=<base64>
    @GetMapping("/esewa/success")
    public ResponseEntity<Void> success(@RequestParam("data") String data) {
        try {
            paymentService.handleSuccess(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/library?payment=failed")
                    .build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/library?payment=processed")
                .build();
    }

    @GetMapping("/esewa/failure")
    public ResponseEntity<Void> failure(@RequestParam(value = "transaction_uuid", required = false) String txnUuid) {
        paymentService.handleFailure(txnUuid);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/library?payment=failed")
                .build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(Authentication authentication) {
        User user = currentUser(authentication);
        List<Payment> payments = paymentService.getUserPayments(user);

        List<Map<String, Object>> response = payments.stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "type", p.getPaymentType(),
                        "plan", p.getPlan() != null ? p.getPlan() : "",
                        "bookId", p.getBookId() != null ? p.getBookId() : 0,
                        "amount", p.getAmount(),
                        "gateway", p.getGateway(),
                        "status", p.getStatus(),
                        "createdAt", p.getCreatedAt().toString()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}