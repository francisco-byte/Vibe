package com.francisco.vibe.Data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JamendoFetcher {

    private static final String CLIENT_ID = "e065f7f7";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final Gson gson = new Gson();

    // Cache simples por tag
    private static final Map<String, List<Song>> cache = new HashMap<>();

    public static List<Song> search(String tag) throws IOException {

        // Cache hit
        if (cache.containsKey(tag)) {
            return cache.get(tag);
        }

        String encodedTag = URLEncoder.encode(tag, StandardCharsets.UTF_8.name());

        String url = "https://api.jamendo.com/v3.0/tracks/"
                + "?client_id=" + CLIENT_ID
                + "&format=json"
                + "&limit=20"
                + "&fuzzytags=" + encodedTag
                + "&include=musicinfo";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("HTTP " + response.code());
            }

            JsonObject root = gson.fromJson(response.body().charStream(), JsonObject.class);
            JsonArray results = root.getAsJsonArray("results");

            List<Song> list = new ArrayList<>();

            for (int i = 0; i < results.size(); i++) {
                JsonObject track = results.get(i).getAsJsonObject();

                String title = track.get("name").getAsString();
                String artist = track.get("artist_name").getAsString();
                String image = track.get("album_image").getAsString();
                String audio = track.get("audio").getAsString();

                list.add(new Song(title, artist, image, audio));
            }

            // Guarda em cache
            cache.put(tag, list);
            return list;
        }
    }



    // Opcional: limpar cache (ex: pull-to-refresh)
    public static void clearCache() {
        cache.clear();
    }
}
