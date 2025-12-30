package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SongHistoryRepository {

    private final UserDatabase db;

    public SongHistoryRepository(Context c) {
        db = new UserDatabase(c);
    }

    public void save(String user, Song song) {
        SQLiteDatabase w = db.getWritableDatabase();

        // Delete any previous entry of this song by trackId
        w.execSQL(
                "DELETE FROM history WHERE trackId=? AND user=?",
                new Object[]{song.getTrackId(), user}
        );

        // Insert new history record
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

        // Keep only last 20 played songs
        w.execSQL(
                "DELETE FROM history WHERE id NOT IN (" +
                        "SELECT id FROM history WHERE user=? " +
                        "ORDER BY playedAt DESC LIMIT 20)",
                new Object[]{user}
        );
    }

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
