package com.readora.readora.repository;

import com.readora.readora.model.Book;
import com.readora.readora.model.ReadingHistory;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    List<ReadingHistory> findByUserOrderByLastReadAtDesc(User user);

    Optional<ReadingHistory> findByUserAndBook(User user, Book book);

    Optional<ReadingHistory> findFirstByUserOrderByLastReadAtDesc(User user);

    long countByUserAndStatus(User user, String status);

    @Query("SELECT DISTINCT h.book.category FROM ReadingHistory h WHERE h.user = :user")
    List<String> findDistinctCategoriesReadByUser(@Param("user") User user);

    @Query("SELECT h.book.id FROM ReadingHistory h WHERE h.user = :user")
    List<Long> findBookIdsReadByUser(@Param("user") User user);

    @Query("SELECT COUNT(h) FROM ReadingHistory h WHERE h.book.authorUser.id = :authorId")
    long countReadsByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(h) FROM ReadingHistory h WHERE h.book.id = :bookId")
    long countReadsByBookId(@Param("bookId") Long bookId);

    @Query("SELECT COUNT(h) FROM ReadingHistory h WHERE h.book.authorUser.id = :authorId AND h.lastReadAt >= :start AND h.lastReadAt < :end")
    long countReadsByAuthorIdBetween(@Param("authorId") Long authorId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(DISTINCT h.user.id) FROM ReadingHistory h WHERE h.book.authorUser.id = :authorId")
    long countUniqueReadersByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(h) FROM ReadingHistory h WHERE h.book.authorUser.id = :authorId AND UPPER(h.status) = 'COMPLETED'")
    long countCompletedByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT h FROM ReadingHistory h WHERE h.book.authorUser.id = :authorId")
    List<ReadingHistory> findByAuthorId(@Param("authorId") Long authorId);
}
