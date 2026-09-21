package com.readora.readora.repository;

import com.readora.readora.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository
        extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByCategoryIgnoreCase(String category);

    List<Book> findByLanguageIgnoreCase(String language);

    List<Book> findByMoodIgnoreCase(String mood);

    List<Book> findByAuthorUserId(Long authorUserId);

    long countByAuthorUserId(Long authorUserId);

    List<Book> findTop10ByOrderByAverageRatingDesc();

    /*
     * READORA BOOK CATALOG
     *
     * Only real books with published status
     * are returned to the Books page.
     */
    List<Book> findByTypeIgnoreCaseAndStatusIgnoreCase(
            String type,
            String status
    );

    @Query("""
            SELECT b
            FROM Book b
            WHERE b.category IN :categories
            AND b.id NOT IN :excludedIds
            """)
    List<Book> findRecommendations(
            @Param("categories") List<String> categories,
            @Param("excludedIds") List<Long> excludedIds
    );

    @Query("SELECT MAX(b.id) FROM Book b")
    Long findMaxId();
}