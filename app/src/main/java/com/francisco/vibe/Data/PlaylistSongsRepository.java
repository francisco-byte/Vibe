package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongsRepository {

    private final UserDatabase db;

    public PlaylistSongsRepository(Context context) {
        db = new UserDatabase(context);
    }

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

    public boolean addSong(int playlistId, Song song) {
        SQLiteDatabase wdb = db.getWritableDatabase();

        // Check if song already exists in this playlist by trackId
        Cursor c = wdb.rawQuery(
                "SELECT 1 FROM playlist_songs WHERE playlistId=? AND trackId=?",
                new String[]{String.valueOf(playlistId), song.getTrackId()}
        );

        boolean exists = c.moveToFirst();
        c.close();

        if (exists) {
            return false; // song already exists
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

        return true; // successfully added
    }

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
