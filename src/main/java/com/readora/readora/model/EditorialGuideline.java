package com.readora.readora.model;

import jakarta.persistence.*;

@Entity
@Table(name = "editorial_guidelines")
public class EditorialGuideline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean checkedItem = false;

    public EditorialGuideline() {
    }

    public EditorialGuideline(String category, String title, String content, int sortOrder, boolean checkedItem) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder;
        this.checkedItem = checkedItem;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCheckedItem() {
        return checkedItem;
    }

    public void setCheckedItem(boolean checkedItem) {
        this.checkedItem = checkedItem;
    }
}
