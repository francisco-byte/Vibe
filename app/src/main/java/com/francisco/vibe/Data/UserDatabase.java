package com.francisco.vibe.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "vibe_users.db";
    private static final int DB_VERSION = 5;

    /**
     * Construtor da base de dados da aplicação.
     * Define o nome e a versão da base de dados SQLite.
     */
    public UserDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Método executado na criação inicial da base de dados.
     * Responsável pela criação de todas as tabelas necessárias
     * ao funcionamento da aplicação.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT)"
        );

        db.execSQL(
                "CREATE TABLE history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user TEXT," +
                        "title TEXT," +
                        "artist TEXT," +
                        "imageUrl TEXT," +
                        "streamUrl TEXT," +
                        "trackId TEXT," +
                        "playedAt INTEGER)"
        );

        db.execSQL(
                "CREATE TABLE favorites (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user TEXT," +
                        "trackId TEXT," +
                        "title TEXT," +
                        "artist TEXT," +
                        "imageUrl TEXT," +
                        "streamUrl TEXT)"
        );

        db.execSQL(
                "CREATE TABLE playlists (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user TEXT," +
                        "name TEXT)"
        );

        db.execSQL(
                "CREATE TABLE playlist_songs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "playlistId INTEGER," +
                        "title TEXT," +
                        "artist TEXT," +
                        "imageUrl TEXT," +
                        "streamUrl TEXT," +
                        "trackId TEXT)"
        );
    }

    /**
     * Método executado quando a versão da base de dados é atualizada.
     * Garante a criação das tabelas em falta para manter a compatibilidade
     * com versões anteriores da aplicação.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS history (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user TEXT," +
                            "title TEXT," +
                            "artist TEXT," +
                            "imageUrl TEXT," +
                            "streamUrl TEXT," +
                            "trackId TEXT," +
                            "playedAt INTEGER)"
            );

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS favorites (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user TEXT," +
                            "trackId TEXT," +
                            "title TEXT," +
                            "artist TEXT," +
                            "imageUrl TEXT," +
                            "streamUrl TEXT)"
            );

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS playlists (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user TEXT," +
                            "name TEXT)"
            );

            db.execSQL(
                    "CREATE TABLE playlist_songs (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "playlistId INTEGER," +
                            "title TEXT," +
                            "artist TEXT," +
                            "imageUrl TEXT," +
                            "streamUrl TEXT," +
                            "trackId TEXT)"
            );
        }
    }

    /**
     * Regista um novo utilizador na base de dados.
     * Caso o nome de utilizador já exista, o registo falha.
     */
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

    /**
     * Verifica as credenciais do utilizador,
     * permitindo validar o processo de autenticação.
     */
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
