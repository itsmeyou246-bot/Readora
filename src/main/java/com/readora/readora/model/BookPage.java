package com.readora.readora.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_pages",
        uniqueConstraints = @UniqueConstraint(name = "uk_book_page_number", columnNames = {"book_id", "page_number"}))
public class BookPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String textContent;

    @Column(nullable = false)
    private String chapterLabel;

    private String imageUrl;
    private String imageTitle;
    private String imageSubtitle;
    private String imageCaption;
    private String imagePhotoCredit;

    protected BookPage() {
    }

    public BookPage(Book book, int pageNumber, String textContent, String chapterLabel,
                    String imageUrl, String imageTitle, String imageSubtitle,
                    String imageCaption, String imagePhotoCredit) {
        this.book = book;
        this.pageNumber = pageNumber;
        this.textContent = textContent;
        this.chapterLabel = chapterLabel;
        this.imageUrl = imageUrl;
        this.imageTitle = imageTitle;
        this.imageSubtitle = imageSubtitle;
        this.imageCaption = imageCaption;
        this.imagePhotoCredit = imagePhotoCredit;
    }

    public Long getPageId() { return pageId; }
    public Book getBook() { return book; }
    public int getPageNumber() { return pageNumber; }
    public String getTextContent() { return textContent; }
    public String getChapterLabel() { return chapterLabel; }
    public String getImageUrl() { return imageUrl; }
    public String getImageTitle() { return imageTitle; }
    public String getImageSubtitle() { return imageSubtitle; }
    public String getImageCaption() { return imageCaption; }
    public String getImagePhotoCredit() { return imagePhotoCredit; }
}
