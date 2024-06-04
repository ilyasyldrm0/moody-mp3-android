package com.broondle.mp3calar.Model;


import androidx.annotation.NonNull;

import java.io.Serializable;

public class AudioModel implements Serializable {
    String path;
    String name;
    String album;
    String artist;
    String videoID;
    Long date_added;
    String thumb_path;
    String lenghtSeconds;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public Long getDate_added() {
        return date_added;
    }

    public void setDate_added(Long date_added) {
        this.date_added = date_added;
    }

    public String getThumb_path() {
        return thumb_path;
    }

    public void setThumb_path(String thumb_path) {
        this.thumb_path = thumb_path;
    }

    public String getLenghtSeconds() {
        return lenghtSeconds;
    }

    public void setLenghtSeconds(String lenghtSeconds) {
        this.lenghtSeconds = lenghtSeconds;
    }

    public AudioModel(String path, String name, String album, String artist, String videoID, Long date_added, String thumb_url, String lenghtSeconds) {
        this.path = path;
        this.name = name;
        this.album = album;
        this.artist = artist;
        this.videoID = videoID;
        this.date_added = date_added;
        this.thumb_path = thumb_url;
        this.lenghtSeconds = lenghtSeconds;
    }

    public AudioModel() {
    }
}
