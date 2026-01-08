package com.francisco.vibe.Data;

public class Playlist {

    private final int id;
    private final String name;

    /**
     * Construtor da classe Playlist.
     * Responsável por criar uma instância de uma playlist com um identificador
     * e um nome associados.
     */
    public Playlist(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Devolve o identificador único da playlist.
     */
    public int getId() {
        return id;
    }

    /**
     * Devolve o nome da playlist.
     */
    public String getName() {
        return name;
    }
}
