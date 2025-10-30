package com.francisco.vibe.Data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JamendoFetcher {

    private static final String CLIENT_ID = "e065f7f7"; // 👈 Get one at https://developer.jamendo.com
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static List<Song> search(String tag) throws IOException {
        String url = "https://api.jamendo.com/v3.0/tracks/?client_id=" + CLIENT_ID
                + "&format=json&limit=20&fuzzytags=" + tag + "&include=musicinfo";

        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());

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
            return list;
        }
    }
}
