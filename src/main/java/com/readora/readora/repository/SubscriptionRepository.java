package com.readora.readora.repository;

import com.readora.readora.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserId(Long userId);

    long countByStatusIgnoreCase(String status);

    long countByPlanIgnoreCase(String plan);
}