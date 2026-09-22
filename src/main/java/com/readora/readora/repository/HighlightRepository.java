package com.readora.readora.repository;

import com.readora.readora.model.Book;
import com.readora.readora.model.Highlight;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    List<Highlight> findByUserAndBookOrderByPageNumberAsc(User user, Book book);

    Optional<Highlight> findByIdAndUser(Long id, User user);

    void deleteByUserAndId(User user, Long id);

    List<Highlight> findByBookAuthorUserIdOrderByCreatedAtDesc(Long authorUserId);

    @Query("SELECT COUNT(h) FROM Highlight h WHERE h.book.authorUser.id = :authorId")
    long countByBookAuthorUserId(@Param("authorId") Long authorId);

    long countByBookId(Long bookId);
}
