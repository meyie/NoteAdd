package com.mcksfg.noteadd;

public class Note {
    private String image;
    private String timestamp;
    private String uid;
    private String username;
    private String description;
    private String lessonId;

    public Note() { }

    public Note(String image, String timestamp, String uid, String username, String description, String lessonId) {
        this.image = image;
        this.timestamp = timestamp;
        this.uid = uid;
        this.username = username;
        this.description = description;
        this.lessonId = lessonId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }
}
