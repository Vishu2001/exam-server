package com.exam.examserver.dto;

public class CategoryDTO {

    private Long cid;
    private String title;
    private String description;

    public CategoryDTO() {
    }

    public CategoryDTO(Long cid, String title, String description) {
        this.cid = cid;
        this.title = title;
        this.description = description;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
