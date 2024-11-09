package com.example.application.views.dedications;

public class Dedication {

    public String category;
    public String title;
    public String description;

    public Dedication() {
    }

    public Dedication(String title, String category, String description) {
        this.title = title;
        this.category = category;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return  category + "\n" +
                title + "\n" +
                description + "\n" +
                "----------------------\n" ;
    }
}
