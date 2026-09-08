package com.readora.readora.repository;

import com.readora.readora.model.Purchase;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    boolean existsByUserAndBookId(User user, Long bookId);
    List<Purchase> findByUserOrderByPurchasedAtDesc(User user);
}
