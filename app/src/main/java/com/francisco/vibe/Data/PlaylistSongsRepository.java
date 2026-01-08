package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongsRepository {

    private final UserDatabase db;

    /**
     * Construtor do repositório de músicas das playlists.
     * Inicializa o acesso à base de dados da aplicação.
     */
    public PlaylistSongsRepository(Context context) {
        db = new UserDatabase(context);
    }

    /**
     * Obtém todas as músicas associadas a uma determinada playlist,
     * devolvendo uma lista de objetos Song.
     */
    public List<Song> getSongs(int playlistId) {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase rdb = db.getReadableDatabase();

        Cursor c = rdb.rawQuery(
                "SELECT title, artist, imageUrl, streamUrl, trackId FROM playlist_songs WHERE playlistId=?",
                new String[]{String.valueOf(playlistId)}
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

    /**
     * Adiciona uma música a uma playlist, verificando previamente
     * se a música já existe nessa playlist.
     * Devolve o resultado da operação.
     */
    public boolean addSong(int playlistId, Song song) {
        SQLiteDatabase wdb = db.getWritableDatabase();

        Cursor c = wdb.rawQuery(
                "SELECT 1 FROM playlist_songs WHERE playlistId=? AND trackId=?",
                new String[]{String.valueOf(playlistId), song.getTrackId()}
        );

        boolean exists = c.moveToFirst();
        c.close();

        if (exists) {
            return false;
        }

        wdb.execSQL(
                "INSERT INTO playlist_songs (playlistId,title,artist,imageUrl,streamUrl,trackId) VALUES (?,?,?,?,?,?)",
                new Object[]{
                        playlistId,
                        song.getTitle(),
                        song.getArtist(),
                        song.getImageUrl(),
                        song.getStreamUrl(),
                        song.getTrackId()
                }
        );

        return true;
    }

    /**
     * Remove uma música de uma playlist específica,
     * com base no identificador da playlist e da música.
     */
    public void removeSongFromPlaylist(int playlistId, Song song) {
        db.getWritableDatabase().execSQL(
                "DELETE FROM playlist_songs WHERE playlistId=? AND trackId=?",
                new Object[]{
                        playlistId,
                        song.getTrackId()
                }
        );
    }
}
