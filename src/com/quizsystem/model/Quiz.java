package com.quizsystem.model;

/** A quiz: a titled set of questions played under a time limit. */
public class Quiz {

    private int id;
    private String title;
    private String description;
    private String category;
    private int timeLimitMinutes;
    private int passPercentage;
    private boolean active;
    private String createdBy;

    public Quiz() {
    }

    public Quiz(int id, String title, String description, String category,
                int timeLimitMinutes, int passPercentage, boolean active, String createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.timeLimitMinutes = timeLimitMinutes;
        this.passPercentage = passPercentage;
        this.active = active;
        this.createdBy = createdBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public int getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(int passPercentage) {
        this.passPercentage = passPercentage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
