package com.samgottfried.redicture.models;

public class Post {

    private final String title;
    private final String imageHash;
    private final String extension;

    public Post(String title, String imageHash, String extension) {
        this.title = title;
        this.imageHash = imageHash;
        this.extension = extension;
    }

    public String getTitle(){
        return title;
    }

    public String getImageHash() {
        return imageHash;
    }

    public String getThumbnail() {
        return "http://i.imgur.com/" + imageHash + "b" + extension;
    }
}
