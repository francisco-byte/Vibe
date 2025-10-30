package com.francisco.vibe.Data;

public class Song {
    private String title;
    private String artist;
    private String imageUrl;
    private String streamUrl;

    public Song(String title, String artist, String imageUrl, String streamUrl) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.streamUrl = streamUrl;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
    public String getStreamUrl() { return streamUrl; }
}
