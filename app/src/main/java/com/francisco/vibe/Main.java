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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.francisco.vibe.Data.JamendoFetcher;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.Data.SongHistoryManager;
import com.francisco.vibe.UI.PlayerActivity;
import com.francisco.vibe.UI.SongAdapter;
import com.francisco.vibe.databinding.MainActivityBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private MainActivityBinding binding;

    private SongAdapter topTracksAdapter;
    private SongAdapter discoverAdapter;
    private SongAdapter mixesAdapter;
    private SongAdapter artistsAdapter;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });


        setupRecyclerViews();
        loadAllSections();
    }

    private void setupRecyclerViews() {

        artistsAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvPopular.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvPopular.setAdapter(artistsAdapter);

        discoverAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvListenAgain.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvListenAgain.setAdapter(discoverAdapter);
    }


    private void loadAllSections() {

        executor.execute(() -> {
            try {
                List<Song> popular = JamendoFetcher.search("popular");
                mainHandler.post(() -> artistsAdapter.setSongs(popular));
            } catch (IOException e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "Failed to load popular", Toast.LENGTH_SHORT).show()
                );
            }
        });

        executor.execute(() -> {
            List<Song> listenAgain = SongHistoryManager.getLast(this, 6);
            mainHandler.post(() -> discoverAdapter.setSongs(listenAgain));
        });


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        discoverAdapter.setSongs(
                SongHistoryManager.getLast(this, 6)
        );
    }


    @Override
    public void onSongClicked(Song song) {
        SongHistoryManager.save(this, song);

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("title", song.getTitle());
        intent.putExtra("artist", song.getArtist());
        intent.putExtra("imageUrl", song.getImageUrl());
        intent.putExtra("streamUrl", song.getStreamUrl());
        startActivity(intent);
    }
}
