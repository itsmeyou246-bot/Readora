package com.readora.readora.repository;

import com.readora.readora.model.BookPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookPageRepository extends JpaRepository<BookPage, Long> {
    Optional<BookPage> findByBookIdAndPageNumber(Long bookId, int pageNumber);
    long countByBookId(Long bookId);
}
