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
import com.francisco.vibe.UI.PlayerActivity;
import com.francisco.vibe.UI.SongAdapter;
import com.francisco.vibe.databinding.MainActivityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Main extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private MainActivityBinding binding;

    // Adapters for each section
    private SongAdapter topTracksAdapter;
    private SongAdapter discoverAdapter;
    private SongAdapter mixesAdapter;
    private SongAdapter artistsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply system insets (status/nav bars)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerViews();
        loadAllSections();
    }

    private void setupRecyclerViews() {
        // Horizontal: Top Artists
        artistsAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvTopArtists.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvTopArtists.setAdapter(artistsAdapter);

        // Vertical: Top Tracks
        topTracksAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvTopTracks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTopTracks.setAdapter(topTracksAdapter);

        // Horizontal: Your Mixes
        mixesAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvMixes.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvMixes.setAdapter(mixesAdapter);

        // Grid: Discover Something New
        discoverAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvDiscover.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvDiscover.setAdapter(discoverAdapter);
    }

    private void loadAllSections() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Fetch different "genres" or moods for each section
                List<Song> topTracks = JamendoFetcher.search("metal");
                List<Song> mixes = JamendoFetcher.search("pop");
                List<Song> artists = JamendoFetcher.search("rock");
                List<Song> discover = JamendoFetcher.search("indie");

                new Handler(Looper.getMainLooper()).post(() -> {
                    topTracksAdapter.setSongs(topTracks);
                    mixesAdapter.setSongs(mixes);
                    artistsAdapter.setSongs(artists);
                    discoverAdapter.setSongs(discover);
                    Toast.makeText(this,
                            "Loaded " + (topTracks.size() + mixes.size() + artists.size() + discover.size()) + " songs",
                            Toast.LENGTH_SHORT).show();
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
