package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "vibe_users.db";
    private static final int DB_VERSION = 2;

    public UserDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // USERS TABLE
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT)"
        );

        // HISTORY TABLE
        db.execSQL(
                "CREATE TABLE history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user TEXT," +
                        "title TEXT," +
                        "artist TEXT," +
                        "imageUrl TEXT," +
                        "streamUrl TEXT," +
                        "playedAt INTEGER)"
        );

        // FAVORITES TABLE
        db.execSQL(
                "CREATE TABLE favorites (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user TEXT," +
                        "title TEXT," +
                        "artist TEXT," +
                        "imageUrl TEXT," +
                        "streamUrl TEXT)"
        );

        // PLAYLISTS TABLE
        db.execSQL(
                "CREATE TABLE playlists (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user TEXT," +
                        "name TEXT)"
        );

        // PLAYLIST SONGS TABLE
        db.execSQL(
                "CREATE TABLE playlist_songs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "playlistId INTEGER," +
                        "title TEXT," +
                        "artist TEXT," +
                        "imageUrl TEXT," +
                        "streamUrl TEXT)"
        );
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) { // new version
            // Create missing history table
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS history (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user TEXT," +
                            "title TEXT," +
                            "artist TEXT," +
                            "imageUrl TEXT," +
                            "streamUrl TEXT," +
                            "playedAt INTEGER)"
            );

            // Create favorites table if needed
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS favorites (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user TEXT," +
                            "title TEXT," +
                            "artist TEXT," +
                            "imageUrl TEXT," +
                            "streamUrl TEXT)"
            );

            // Playlists table
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS playlists (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user TEXT," +
                            "name TEXT)"
            );

            // Playlist songs table
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "playlistId INTEGER," +
                            "title TEXT," +
                            "artist TEXT," +
                            "imageUrl TEXT," +
                            "streamUrl TEXT)"
            );
        }
    }


    // REGISTER
    public boolean register(String username, String password) {
        try {
            getWritableDatabase().execSQL(
                    "INSERT INTO users (username, password) VALUES (?,?)",
                    new Object[]{username, password}
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // LOGIN
    public boolean login(String username, String password) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );
        boolean success = c.moveToFirst();
        c.close();
        return success;
    }
}
