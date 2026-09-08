package com.readora.readora.service;

import com.readora.readora.dto.SubscriptionRequest;
import com.readora.readora.model.Subscription;
import com.readora.readora.model.SubscriptionPlan;
import com.readora.readora.model.User;
import com.readora.readora.repository.SubscriptionRepository;
import com.readora.readora.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(
            UserRepository userRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription saveSubscription(
            SubscriptionRequest request
    ) {

        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("A registered user email is required.");
        }

        if (request.getPlan() == null || request.getPlan().isBlank()) {
            throw new IllegalArgumentException("A subscription plan is required.");
        }

        User user = userRepository
                .findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        SubscriptionPlan plan;
        try {
            plan = SubscriptionPlan.valueOf(request.getPlan().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid subscription plan.");
        }

        double price = 0;

        if (plan == SubscriptionPlan.PREMIUM) {
            price = 499;
        }

        if (plan == SubscriptionPlan.INSTITUTIONAL) {
            price = 0;
        }

        Subscription subscription =
                subscriptionRepository
                        .findByUser(user)
                        .orElse(new Subscription());

        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setPrice(price);

        return subscriptionRepository.save(
                subscription
        );
    }
}