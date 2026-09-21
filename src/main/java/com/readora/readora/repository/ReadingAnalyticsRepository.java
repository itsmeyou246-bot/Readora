package com.readora.readora.repository;

import com.readora.readora.model.ReadingAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingAnalyticsRepository extends JpaRepository<ReadingAnalytics, Long> {

    Optional<ReadingAnalytics> findByUserId(Long userId);
}