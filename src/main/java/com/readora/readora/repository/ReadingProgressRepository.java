package com.readora.readora.repository;

import com.readora.readora.model.ReadingProgress;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUserIdAndBookId(Long userId, Long bookId);
}
