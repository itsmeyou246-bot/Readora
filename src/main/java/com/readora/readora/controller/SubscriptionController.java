package com.readora.readora.controller;

import com.readora.readora.dto.SubscriptionRequest;
import com.readora.readora.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public String saveSubscription(@RequestBody SubscriptionRequest request) {
        subscriptionService.saveSubscription(request);
        return "Subscription saved successfully";
    }
}