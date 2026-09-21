package com.readora.readora.controller;

import com.readora.readora.dto.SubscriptionRequest;
import com.readora.readora.model.Subscription;
import com.readora.readora.model.User;

import com.readora.readora.service.LibraryService;
import com.readora.readora.service.SubscriptionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService service;

    private final LibraryService libraryService;


    public SubscriptionController(
            SubscriptionService service,
            LibraryService libraryService) {

        this.service = service;

        this.libraryService =
                libraryService;
    }


    // =====================================================
    // CREATE / UPDATE SUBSCRIPTION
    // =====================================================

    @PostMapping
    public ResponseEntity<?> subscribe(

            @Valid @RequestBody SubscriptionRequest request,

            Authentication authentication) {


        User user =
                currentUser(authentication);


        try {

            Subscription subscription =
                    service.createSubscription(
                            user.getId(),
                            request.getPlan()
                    );


            return ResponseEntity.ok(
                    subscription
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // GET MY SUBSCRIPTION
    // =====================================================

    @GetMapping
    public ResponseEntity<?> getMySubscription(
            Authentication authentication) {

        User user =
                currentUser(authentication);


        return service
                .getSubscription(
                        user.getId()
                )
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }


    // =====================================================
    // OLD URL - KEPT FOR COMPATIBILITY
    // =====================================================

    @GetMapping("/{userId}")
    public ResponseEntity<?> getSubscription(

            @PathVariable Long userId,

            Authentication authentication) {


        User user =
                currentUser(authentication);


        if (!user.getId().equals(userId)) {

            return ResponseEntity
                    .status(
                            HttpStatus.FORBIDDEN
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "You can only view your own subscription."
                            )
                    );
        }


        return service
                .getSubscription(userId)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }


    private User currentUser(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required."
            );
        }


        return libraryService.user(
                authentication.getName()
        );
    }
}