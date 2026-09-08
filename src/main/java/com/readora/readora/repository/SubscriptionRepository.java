package com.readora.readora.repository;

import com.readora.readora.model.Subscription;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUser(User user);

}