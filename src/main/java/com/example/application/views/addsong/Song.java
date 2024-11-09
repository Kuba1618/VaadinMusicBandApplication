package com.example.application.views.addsong;

import java.io.File;
import java.util.Random;

public class Song {

    public int id;
    public String title;
    public String author;
    public String categories;
    public String description;
    public File songFile;


    public Song() {
    }

    public Song(String title, String author, String categories, String description, File songFile) {
        Random generator = new Random();
        this.id = 100000000 + generator.nextInt(900000000);
        this.title = title;
        this.author = author;
        this.categories = categories;
        this.description = description;
        this.songFile = songFile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return categories;
    }

    public void setCategory(String categories) {
        this.categories = categories;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public File getSongFile() {
        return songFile;
    }

    public void setSongFile(File songFile) {
        this.songFile = songFile;
    }

}
