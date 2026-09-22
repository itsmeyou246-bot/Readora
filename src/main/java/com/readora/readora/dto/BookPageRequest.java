package com.readora.readora.dto;

public class BookPageRequest {

    private Integer pageNumber;
    private String textContent;
    private String chapterLabel;
    private String imageUrl;
    private String imageTitle;
    private String imageSubtitle;
    private String imageCaption;
    private String imagePhotoCredit;

    public BookPageRequest() {
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getChapterLabel() {
        return chapterLabel;
    }

    public void setChapterLabel(String chapterLabel) {
        this.chapterLabel = chapterLabel;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public String getImageSubtitle() {
        return imageSubtitle;
    }

    public void setImageSubtitle(String imageSubtitle) {
        this.imageSubtitle = imageSubtitle;
    }

    public String getImageCaption() {
        return imageCaption;
    }

    public void setImageCaption(String imageCaption) {
        this.imageCaption = imageCaption;
    }

    public String getImagePhotoCredit() {
        return imagePhotoCredit;
    }

    public void setImagePhotoCredit(String imagePhotoCredit) {
        this.imagePhotoCredit = imagePhotoCredit;
    }
}
