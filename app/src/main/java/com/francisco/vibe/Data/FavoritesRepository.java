package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class FavoritesRepository {

    private final UserDatabase db;

    /**
     * Construtor do repositório de favoritos.
     * Inicializa o acesso à base de dados da aplicação.
     */
    public FavoritesRepository(Context c) {
        db = new UserDatabase(c);
    }

    /**
     * Verifica se uma determinada música se encontra marcada como favorita
     * para o utilizador atualmente autenticado.
     */
    public boolean isFavorite(String user, String trackId) {
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT id FROM favorites WHERE user=? AND trackId=?",
                new String[]{user, trackId}
        );
        boolean fav = c.moveToFirst();
        c.close();
        return fav;
    }

    /**
     * Alterna o estado de favorito de uma música.
     * Caso a música já esteja nos favoritos, é removida;
     * caso contrário, é adicionada à lista de favoritos.
     */
    public void toggle(String user, Song song) {
        SQLiteDatabase w = db.getWritableDatabase();

        if (isFavorite(user, song.getTrackId())) {
            w.execSQL(
                    "DELETE FROM favorites WHERE user=? AND trackId=?",
                    new Object[]{user, song.getTrackId()}
            );
        } else {
            if (!isFavorite(user, song.getTrackId())) {
                w.execSQL(
                        "INSERT INTO favorites (user, trackId, title, artist, imageUrl, streamUrl) " +
                                "VALUES (?,?,?,?,?,?)",
                        new Object[]{
                                user,
                                song.getTrackId(),
                                song.getTitle(),
                                song.getArtist(),
                                song.getImageUrl(),
                                song.getStreamUrl()
                        }
                );
            }
        }
    }

    /**
     * Obtém todas as músicas favoritas associadas a um determinado utilizador,
     * devolvendo-as sob a forma de uma lista de objetos Song.
     */
    public List<Song> getAll(String user) {
        List<Song> list = new ArrayList<>();

        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM favorites WHERE user=?",
                new String[]{user}
        );

        while (c.moveToNext()) {
            list.add(new Song(
                    c.getString(c.getColumnIndexOrThrow("trackId")),
                    c.getString(c.getColumnIndexOrThrow("title")),
                    c.getString(c.getColumnIndexOrThrow("artist")),
                    c.getString(c.getColumnIndexOrThrow("imageUrl")),
                    c.getString(c.getColumnIndexOrThrow("streamUrl"))
            ));
        }

        c.close();
        return list;
    }
}
