package com.readora.readora.repository;

import com.readora.readora.model.Payment;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByGatewayReference(String gatewayReference);
    List<Payment> findByUserOrderByCreatedAtDesc(User user);
}