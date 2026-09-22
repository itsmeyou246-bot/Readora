package com.readora.readora.repository;

import com.readora.readora.model.Book;
import com.readora.readora.model.Review;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookOrderByCreatedAtDesc(Book book);

    Optional<Review> findByUserAndBook(User user, Book book);

    Optional<Review> findByIdAndUser(Long id, User user);

    void deleteByUserAndId(User user, Long id);

    long countByBook(Book book);

    long countByBookAuthorUserId(Long authorUserId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingForBook(@Param("bookId") Long bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.authorUser.id = :authorUserId")
    Double getAverageRatingForAuthor(@Param("authorUserId") Long authorUserId);

    List<Review> findByBookAuthorUserIdOrderByCreatedAtDesc(Long authorUserId);
}
