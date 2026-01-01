package com.francisco.vibe.UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.francisco.vibe.Data.FavoritesRepository;
import com.francisco.vibe.Data.Playlist;
import com.francisco.vibe.Data.PlaylistAdapter;
import com.francisco.vibe.Data.PlaylistRepository;
import com.francisco.vibe.Data.SessionManager;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.Main;
import com.francisco.vibe.R;
import com.francisco.vibe.databinding.ActivityLibraryBinding;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;

import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity {

    private ActivityLibraryBinding binding;
    private String currentUser;

    private FavoritesRepository favoritesRepo;
    private PlaylistRepository playlistRepo;

    private SongAdapter favoritesAdapter;
    private PlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
        );

        super.onCreate(savedInstanceState);
        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCreatePlaylist.setOnClickListener(v ->
                showCreatePlaylistDialog()
        );

        // 🔐 SESSION
        currentUser = SessionManager.getUsername(this);
        if (currentUser == null) {
            finish();
            return;
        }

        favoritesRepo = new FavoritesRepository(this);
        playlistRepo = new PlaylistRepository(this);

        setupBottomNav();
        setupRecyclerViews();

        loadFavorites();
        loadPlaylists();
    }

    // -------------------------
    // BOTTOM NAV
    // -------------------------
    private void setupBottomNav() {

        binding.bottomNav.setSelectedItemId(R.id.nav_library);

        binding.bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_library) {
                return true;
            }

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, Main.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    // -------------------------
    // RECYCLERS
    // -------------------------
    @OptIn(markerClass = UnstableApi.class)
    private void setupRecyclerViews() {

        favoritesAdapter = new SongAdapter(
                new ArrayList<>(),
                song -> {
                    Intent intent = new Intent(LibraryActivity.this, PlayerActivity.class);

                    // Pass song details via Intent extras
                    intent.putExtra("trackId", song.getTrackId());
                    intent.putExtra("title", song.getTitle());
                    intent.putExtra("artist", song.getArtist());
                    intent.putExtra("imageUrl", song.getImageUrl());
                    intent.putExtra("streamUrl", song.getStreamUrl());


                    startActivity(intent);
                }
        );

        binding.rvFavorites.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.rvFavorites.setAdapter(favoritesAdapter);

        //  PLAYLISTS
        playlistAdapter = new PlaylistAdapter(
                new ArrayList<>(),
                playlist -> {
                    Intent intent = new Intent(this, PlaylistDetailActivity.class);
                    intent.putExtra("playlistId", playlist.getId());
                    intent.putExtra("playlistName", playlist.getName());
                    startActivity(intent);
                }
        );

        binding.rvPlaylists.setLayoutManager(
                new LinearLayoutManager(this)
        );
        binding.rvPlaylists.setAdapter(playlistAdapter);
    }

    // -------------------------
    // Create Playlists
    // -------------------------
    private void showCreatePlaylistDialog() {

        LayoutInflater inflater = LayoutInflater.from(this);
        final var view = inflater.inflate(
                R.layout.dialog_create_playlist,
                null
        );

        EditText etName = view.findViewById(R.id.etPlaylistName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        view.findViewById(R.id.btnCreate).setOnClickListener(v -> {

            String name = etName.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }

            playlistRepo.create(currentUser, name);
            loadPlaylists();
            dialog.dismiss();
        });

        dialog.show();
    }

    // -------------------------
    // LOAD DATA
    // -------------------------
    private void loadFavorites() {
        favoritesAdapter.setSongs(
                favoritesRepo.getAll(currentUser)
        );
    }

    private void loadPlaylists() {
        playlistAdapter.setPlaylists(
                playlistRepo.getAll(currentUser)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites each time the activity resumes
        loadFavorites();
    }

}
