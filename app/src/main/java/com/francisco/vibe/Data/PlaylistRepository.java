package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRepository {

    private final UserDatabase db;

    public PlaylistRepository(Context c) {
        db = new UserDatabase(c);
    }

    public void create(String user, String name) {
        db.getWritableDatabase().execSQL(
                "INSERT INTO playlists (user,name) VALUES (?,?)",
                new Object[]{user, name}
        );
    }

    public List<String> getAll(String user) {
        List<String> list = new ArrayList<>();

        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT name FROM playlists WHERE user=?",
                new String[]{user}
        );

        while (c.moveToNext()) {
            list.add(c.getString(0));
        }

        c.close();
        return list;
    }
}
