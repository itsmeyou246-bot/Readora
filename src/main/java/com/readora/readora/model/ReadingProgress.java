package com.readora.readora.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "reading_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reading_progress_user_book",
                        columnNames = {
                                "user_id",
                                "book_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_progress_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_progress_book",
                        columnList = "book_id"
                )
        }
)
public class ReadingProgress {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "book_id",
            nullable = false
    )
    private Long bookId;

    @Column(
            nullable = false
    )
    private Integer currentPage = 1;

    @Column(
            nullable = false
    )
    private Integer totalPages = 1;

    public ReadingProgress() {
    }

    public ReadingProgress(
            User user,
            Book book,
            int currentPage) {

        this.userId = user.getId();
        this.bookId = book.getId();
        this.currentPage = Math.max(1, currentPage);
        this.totalPages =
                book.getPageCount() == null
                        ? 1
                        : Math.max(
                        1,
                        book.getPageCount()
                );
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(
            Integer currentPage) {

        this.currentPage =
                Math.max(1, currentPage);
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(
            Integer totalPages) {

        this.totalPages =
                Math.max(1, totalPages);
    }

    public int getPercentage() {
        if (totalPages == null || totalPages <= 0) {
            return 0;
        }
        int cur = currentPage == null ? 0 : currentPage;
        return Math.min(100, Math.max(0, Math.round((float) cur / totalPages * 100)));
    }
}