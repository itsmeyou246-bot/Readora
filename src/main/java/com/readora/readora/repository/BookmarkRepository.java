package com.readora.readora.repository;

import com.readora.readora.model.Bookmark;
import com.readora.readora.model.Book;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);

    List<Bookmark> findByUserAndBookOrderByPageNumberAsc(User user, Book book);

    Optional<Bookmark> findByUserAndBookAndPageNumber(User user, Book book, Integer pageNumber);

    boolean existsByUserAndBookAndPageNumber(User user, Book book, Integer pageNumber);

    void deleteByUserAndBookAndPageNumber(User user, Book book, Integer pageNumber);

    void deleteByUserAndId(User user, Long id);

    long countByUser(User user);

    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.book.authorUser.id = :authorId")
    long countByBookAuthorUserId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.book.id = :bookId")
    long countByBookId(@Param("bookId") Long bookId);
}
