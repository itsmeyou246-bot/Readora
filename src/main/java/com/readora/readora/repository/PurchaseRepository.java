package com.readora.readora.repository;

import com.readora.readora.model.Purchase;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    boolean existsByUserAndBookId(User user, Long bookId);
    List<Purchase> findByUserOrderByPurchasedAtDesc(User user);

    @Query("SELECT COALESCE(SUM(p.book.price), 0.0) FROM Purchase p WHERE p.book.authorUser.id = :authorId")
    Double sumRoyaltiesByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.book.id = :bookId")
    Long countPurchasesByBookId(@Param("bookId") Long bookId);

    @Query("SELECT COALESCE(SUM(p.book.price), 0.0) FROM Purchase p WHERE p.book.id = :bookId")
    Double sumRoyaltiesByBookId(@Param("bookId") Long bookId);

    @Query("SELECT p FROM Purchase p WHERE p.book.authorUser.id = :authorId ORDER BY p.purchasedAt DESC")
    List<Purchase> findByAuthorIdOrderByPurchasedAtDesc(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.book.authorUser.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COALESCE(SUM(p.book.price), 0.0) FROM Purchase p WHERE p.book.authorUser.id = :authorId AND p.purchasedAt >= :start AND p.purchasedAt < :end")
    Double sumSalesByAuthorIdBetween(@Param("authorId") Long authorId, @Param("start") java.time.Instant start, @Param("end") java.time.Instant end);
}
