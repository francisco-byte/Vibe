package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SongHistoryRepository {

    private final UserDatabase db;

    /**
     * Construtor do repositório de histórico de músicas.
     * Inicializa o acesso à base de dados da aplicação.
     */
    public SongHistoryRepository(Context c) {
        db = new UserDatabase(c);
    }

    /**
     * Guarda uma música no histórico de reprodução do utilizador.
     * Caso a música já exista no histórico, a entrada anterior é removida
     * para atualizar a data da última reprodução.
     * O histórico é limitado às últimas 20 músicas reproduzidas.
     */
    public void save(String user, Song song) {
        SQLiteDatabase w = db.getWritableDatabase();

        w.execSQL(
                "DELETE FROM history WHERE trackId=? AND user=?",
                new Object[]{song.getTrackId(), user}
        );

        w.execSQL(
                "INSERT INTO history (user,title,artist,imageUrl,streamUrl,trackId,playedAt) " +
                        "VALUES (?,?,?,?,?,?,?)",
                new Object[]{
                        user,
                        song.getTitle(),
                        song.getArtist(),
                        song.getImageUrl(),
                        song.getStreamUrl(),
                        song.getTrackId(),
                        System.currentTimeMillis()
                }
        );

        w.execSQL(
                "DELETE FROM history WHERE id NOT IN (" +
                        "SELECT id FROM history WHERE user=? " +
                        "ORDER BY playedAt DESC LIMIT 20)",
                new Object[]{user}
        );
    }

    /**
     * Obtém as últimas músicas reproduzidas por um utilizador,
     * ordenadas da mais recente para a mais antiga.
     */
    public List<Song> getLast(String user, int limit) {
        List<Song> list = new ArrayList<>();

        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT * FROM history WHERE user=? " +
                        "ORDER BY playedAt DESC LIMIT ?",
                new String[]{user, String.valueOf(limit)}
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
