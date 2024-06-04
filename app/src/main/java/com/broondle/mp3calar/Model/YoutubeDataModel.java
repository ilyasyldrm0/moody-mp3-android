package com.broondle.mp3calar.Model;

public class YoutubeDataModel {
    String title;
    String description;
    String publishedAt;
    String thumbnail;
    String videoID;
    String views;
    String author;
    String lengthSeconds;

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

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLengthSeconds() {
        return lengthSeconds;
    }

    public void setLengthSeconds(String lengthSeconds) {
        this.lengthSeconds = lengthSeconds;
    }

    public YoutubeDataModel(String title, String description, String publishedAt, String thumbnail, String videoID, String views, String author, String lengthSeconds) {
        this.title = title;
        this.description = description;
        this.publishedAt = publishedAt;
        this.thumbnail = thumbnail;
        this.videoID = videoID;
        this.views = views;
        this.author = author;
        this.lengthSeconds = lengthSeconds;
    }

    public YoutubeDataModel() {
    }
}
