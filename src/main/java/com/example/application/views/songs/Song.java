package com.example.application.views.songs;

import java.io.File;
import java.util.Random;

public class Song {

    public String id;
    public String title;
    public String author;
    public String category;
    public String description;
    public File songFile;


    public Song(String title, String category, String description) {
    }

    public Song(String id,String title, String author, String category, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.description = description;
    }

    public Song(String title, String author, String category, String description, File songFile) {
        Random generator = new Random();
        this.id = 100000000 + generator.nextInt(900000000) + "";
        this.title = title;
        this.author = author;
        this.category = category;
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
        return category;
    }

    public void setCategory(String categories) {
        this.category = categories;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getSongFile() {
        return songFile;
    }

    public void setSongFile(File songFile) {
        this.songFile = songFile;
    }

    @Override
    public String toString() {
        return  id + "\n" +
                title + "\n" +
                author + "\n" +
                category + "\n" +
                description + "\n" +
                "----------------------\n" ;
    }

}
