package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRepository {

    private final UserDatabase db;

    public PlaylistRepository(Context context) {
        db = new UserDatabase(context);
    }

    public List<Playlist> getAll(String user) {

        List<Playlist> list = new ArrayList<>();
        SQLiteDatabase rdb = db.getReadableDatabase();

        Cursor c = rdb.rawQuery(
                "SELECT id, name FROM playlists WHERE user=?",
                new String[]{user}
        );

        while (c.moveToNext()) {
            list.add(
                    new Playlist(
                            c.getInt(0),
                            c.getString(1)
                    )
            );
        }

        c.close();
        return list;
    }

    public void create(String user, String name) {
        db.getWritableDatabase().execSQL(
                "INSERT INTO playlists (user,name) VALUES (?,?)",
                new Object[]{user, name}
        );
    }
}
