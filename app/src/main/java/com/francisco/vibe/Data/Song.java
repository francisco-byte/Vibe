package com.francisco.vibe.Data;

public class Song {
        private final String title;
        private final String artist;
        private final String imageUrl;
        private final String streamUrl;

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
