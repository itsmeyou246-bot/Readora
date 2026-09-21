package com.readora.readora.service;

import com.readora.readora.model.Subscription;
import com.readora.readora.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;

    // Monthly price per plan, in NPR. Single source of truth for billing.
    private static final Map<String, Double> PLAN_PRICING = Map.of(
            "FREE", 0.0,
            "PREMIUM", 450.0,
            "INSTITUTIONAL", 2400.0
    );

    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    public Subscription createSubscription(Long userId, String plan) {
        String cleanPlan = normalizePlan(plan);

        Subscription subscription = repository.findByUserId(userId)
                .orElse(new Subscription());

        LocalDate today = LocalDate.now();

        subscription.setUserId(userId);
        subscription.setPlan(cleanPlan);
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(today);
        subscription.setEndDate(today.plusMonths(1));
        subscription.setAmount(PLAN_PRICING.getOrDefault(cleanPlan, 0.0));
        subscription.setPaymentMethod(
                cleanPlan.equals("FREE") ? "Complimentary" : "Pending Gateway Selection"
        );

        return repository.save(subscription);
    }

    public Optional<Subscription> getSubscription(Long userId) {
        return repository.findByUserId(userId);
    }

    private String normalizePlan(String plan) {
        if (plan == null || plan.isBlank()) {
            throw new IllegalArgumentException("Subscription plan is required.");
        }

        String value = plan.trim().toUpperCase();

        return switch (value) {
            case "FREE" -> "FREE";
            case "PREMIUM" -> "PREMIUM";
            case "INSTITUTIONAL" -> "INSTITUTIONAL";
            default -> throw new IllegalArgumentException("Invalid subscription plan.");
        };
    }
}