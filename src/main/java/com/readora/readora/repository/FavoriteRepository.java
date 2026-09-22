package com.readora.readora.repository;

import com.readora.readora.model.Book;
import com.readora.readora.model.Favorite;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserOrderByCreatedAtDesc(User user);

    Optional<Favorite> findByUserAndBook(User user, Book book);

    boolean existsByUserAndBook(User user, Book book);

    void deleteByUserAndBook(User user, Book book);

    @Query("SELECT f.book.category FROM Favorite f WHERE f.user = :user")
    List<String> findFavoriteCategories(@Param("user") User user);

    @Query("SELECT f.book.id FROM Favorite f WHERE f.user = :user")
    List<Long> findFavoriteBookIds(@Param("user") User user);
}
