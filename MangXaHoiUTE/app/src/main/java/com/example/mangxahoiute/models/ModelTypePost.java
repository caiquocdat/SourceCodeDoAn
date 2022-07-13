package com.example.mangxahoiute.models;

public class ModelTypePost {
    String tPost,image;

    public ModelTypePost() {
    }

    public ModelTypePost(String tPost, String image) {
        this.tPost = tPost;
        this.image = image;
    }

    public String gettPost() {
        return tPost;
    }

    public void settPost(String tPost) {
        this.tPost = tPost;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
