package com.francisco.vibe.Data;

public class Song {
    private String trackId;
    private String title;
    private String artist;
    private String imageUrl;
    private String streamUrl;

    public Song(String trackId, String title, String artist, String imageUrl, String streamUrl) {
        this.trackId = trackId;
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.streamUrl = streamUrl;
    }

    public String getTrackId() {
        return trackId;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
    public String getStreamUrl() { return streamUrl; }
}
