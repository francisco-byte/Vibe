package com.francisco.vibe.Data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SongHistoryManager {

    private static final String PREFS_NAME = "song_history";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY = 20;

    private static final Gson gson = new Gson();

    public static void save(Context context, Song song) {
        List<Song> history = getAll(context);

        // remove duplicates (same stream URL)
        Iterator<Song> iterator = history.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getStreamUrl().equals(song.getStreamUrl())) {
                iterator.remove();
            }
        }

        // add to top
        history.add(0, song);

        // limit size
        if (history.size() > MAX_HISTORY) {
            history = history.subList(0, MAX_HISTORY);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_HISTORY, gson.toJson(history))
                .apply();
    }

    public static List<Song> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, null);

        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<Song>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static List<Song> getLast(Context context, int limit) {
        List<Song> all = getAll(context);
        if (all.size() <= limit) return all;
        return all.subList(0, limit);
    }
}
