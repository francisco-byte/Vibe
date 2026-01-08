package com.francisco.vibe.Data;

public class Song {

    private String trackId;
    private String title;
    private String artist;
    private String imageUrl;
    private String streamUrl;

    /**
     * Construtor da classe Song.
     * Responsável por criar uma instância de uma música com toda a informação
     * necessária para a sua apresentação e reprodução na aplicação.
     */
    public Song(String trackId, String title, String artist, String imageUrl, String streamUrl) {
        this.trackId = trackId;
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.streamUrl = streamUrl;
    }

    /**
     * Devolve o identificador único da música.
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Devolve o título da música.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Devolve o nome do artista da música.
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Devolve o URL da imagem associada à música.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Devolve o URL de reprodução (stream) da música.
     */
    public String getStreamUrl() {
        return streamUrl;
    }
}
