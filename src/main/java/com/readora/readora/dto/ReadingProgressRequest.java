package com.readora.readora.dto;

public class ReadingProgressRequest {

    private Long bookId;
    private Integer currentPage;
    private Integer totalPages;

    public ReadingProgressRequest() {
    }

    public ReadingProgressRequest(Long bookId, Integer currentPage, Integer totalPages) {
        this.bookId = bookId;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
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

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
