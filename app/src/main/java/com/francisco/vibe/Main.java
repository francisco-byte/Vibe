package com.francisco.vibe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.Data.JamendoFetcher;
import com.francisco.vibe.UI.PlayerActivity;
import com.francisco.vibe.UI.SongAdapter;
import com.francisco.vibe.databinding.MainActivityBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Main extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private MainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SongAdapter adapter = new SongAdapter(new ArrayList<>(), this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Song> songs = JamendoFetcher.search("metal");
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.setSongs(songs);
                    Toast.makeText(this, "Loaded " + songs.size() + " songs", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Failed to load songs", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onSongClicked(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("title", song.getTitle());
        intent.putExtra("artist", song.getArtist());
        intent.putExtra("imageUrl", song.getImageUrl());
        intent.putExtra("streamUrl", song.getStreamUrl());
        startActivity(intent);
    }
}
