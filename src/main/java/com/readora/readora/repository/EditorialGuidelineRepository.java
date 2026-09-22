package com.readora.readora.repository;

import com.readora.readora.model.EditorialGuideline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EditorialGuidelineRepository extends JpaRepository<EditorialGuideline, Long> {

    List<EditorialGuideline> findByActiveTrueOrderBySortOrderAsc();

    List<EditorialGuideline> findByCategoryIgnoreCaseAndActiveTrueOrderBySortOrderAsc(String category);

    long countByCategoryIgnoreCase(String category);
}
