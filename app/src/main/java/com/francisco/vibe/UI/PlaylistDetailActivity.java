package com.francisco.vibe.UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.francisco.vibe.Data.PlaylistSongsRepository;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.R;
import com.francisco.vibe.databinding.ActivityPlaylistDetailBinding;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity
        implements SongAdapter.OnSongClickListener {

    private ActivityPlaylistDetailBinding binding;

    private int playlistId;
    private String playlistName;

    private PlaylistSongsRepository repo;
    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
        );

        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        playlistId = getIntent().getIntExtra("playlistId", -1);
        playlistName = getIntent().getStringExtra("playlistName");

        if (playlistId == -1) {
            finish();
            return;
        }

        binding.tvPlaylistName.setText(playlistName);
        binding.btnBack.setOnClickListener(v -> finish());

        repo = new PlaylistSongsRepository(this);

        adapter = new SongAdapter(
                new ArrayList<>(),
                this, // OnSongClickListener
                song -> showRemoveSongDialog(song) // Only here
        );


        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.setAdapter(adapter);

        loadSongs();
    }

    private void showRemoveSongDialog(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Song")
                .setMessage("Are you sure you want to remove \"" + song.getTitle() + "\" from this playlist?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    // Remove from playlist repo
                    repo.removeSongFromPlaylist(playlistId, song);
                    // Refresh RecyclerView
                    loadSongs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void loadSongs() {
        List<Song> songs = repo.getSongs(playlistId);
        adapter.setSongs(songs);
    }

    @Override
    public void onSongClicked(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        // Pass the playlist ID instead of individual song info
        intent.putExtra("playlistId", playlistId);
        // Optionally, pass the position to start from the clicked song
        intent.putExtra("startIndex", adapter.getSongs().indexOf(song));

        startActivity(intent);
    }

}
