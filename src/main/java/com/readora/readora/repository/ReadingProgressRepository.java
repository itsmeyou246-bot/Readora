package com.readora.readora.repository;

import com.readora.readora.model.ReadingProgress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReadingProgressRepository
        extends JpaRepository<ReadingProgress, Long> {

    Optional<ReadingProgress> findByUserIdAndBookId(
            Long userId,
            Long bookId
    );

    @Query("SELECT p FROM ReadingProgress p WHERE p.bookId IN :bookIds")
    List<ReadingProgress> findByBookIdIn(@Param("bookIds") List<Long> bookIds);
}