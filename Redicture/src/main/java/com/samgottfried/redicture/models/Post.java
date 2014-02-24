package com.samgottfried.redicture.models;

public class Post {

    public String title;
    public String imageHash;

    public Post(String title, String imageHash) {
        this.title = title;
        this.imageHash = imageHash;
    }

    @Override
    public String toString () {
        return this.title;
    }
}
