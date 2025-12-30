package com.francisco.vibe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.francisco.vibe.Data.JamendoFetcher;
import com.francisco.vibe.Data.SessionManager;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.Data.SongHistoryRepository;
import com.francisco.vibe.Data.AuthActivity;
import com.francisco.vibe.UI.LibraryActivity;
import com.francisco.vibe.UI.PlayerActivity;
import com.francisco.vibe.UI.ProfileActivity;
import com.francisco.vibe.UI.SongAdapter;
import com.francisco.vibe.databinding.MainActivityBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private MainActivityBinding binding;

    private SongHistoryRepository historyRepo;
    private String currentUser;

    private SongAdapter popularAdapter;
    private SongAdapter historyAdapter;
    private SongAdapter searchAdapter;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String currentFilter = "All"; // default search filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);

        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🔐 SESSION CHECK
        currentUser = SessionManager.getUsername(this);
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        // 👤 PROFILE
        binding.btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        // 🧭 BOTTOM NAV
        setupBottomNav();

        // DB REPO
        historyRepo = new SongHistoryRepository(this);

        // SYSTEM INSETS (bottom nav safe)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });

        setupRecyclerViews();
        setupSearchBarWithFilter();
        loadAllSections();
    }

    // -------------------------
    // BOTTOM NAVIGATION
    // -------------------------
    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) return true;
            if (item.getItemId() == R.id.nav_library) {
                startActivity(new Intent(this, LibraryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // -------------------------
    // RECYCLERVIEWS
    // -------------------------
    private void setupRecyclerViews() {
        // POPULAR
        popularAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvPopular.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPopular.setAdapter(popularAdapter);

        // LISTEN AGAIN
        historyAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvListenAgain.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvListenAgain.setAdapter(historyAdapter);

        // SEARCH RESULTS
        searchAdapter = new SongAdapter(new ArrayList<>(), this);
        RecyclerView rvSearch = binding.rvSearchResults;
        rvSearch.setLayoutManager(new LinearLayoutManager(this));
        rvSearch.setAdapter(searchAdapter);
    }

    // -------------------------
    // SEARCH BAR + FILTER
    // -------------------------
    private void setupSearchBarWithFilter() {
        EditText searchInput = binding.searchInput;

        // Optional: Add a spinner in your XML as `spinnerFilter`
        Spinner spinner = binding.searchFilter;
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.search_filter_options, // e.g., All, Mood, Artist
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(filterAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString();
                String query = searchInput.getText().toString().trim();
                if (!query.isEmpty()) performSearch(query);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                } else {
                    searchAdapter.setSongs(new ArrayList<>());
                    binding.rvSearchResults.setVisibility(View.GONE);
                    binding.scroll.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private String getSelectedFilter() {
        return binding.searchFilter.getSelectedItem().toString();
    }
    private void performSearch(String query) {
        binding.rvSearchResults.setVisibility(android.view.View.VISIBLE);
        binding.scroll.setVisibility(android.view.View.GONE);

        String filter = getSelectedFilter();

        executor.execute(() -> {
            try {
                List<Song> results;
                switch (filter) {
                    case "Artist":
                        results = JamendoFetcher.searchByArtist(query);
                        break;
                    case "Mood":
                        results = JamendoFetcher.search(query); // mood search
                        break;
                    default:
                        results = JamendoFetcher.search(query); // normal track search
                        break;
                }

                mainHandler.post(() -> searchAdapter.setSongs(results));
            } catch (IOException e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    // -------------------------
    // LOAD DATA
    // -------------------------
    private void loadAllSections() {
        // POPULAR SONGS
        executor.execute(() -> {
            try {
                List<Song> popular = JamendoFetcher.search("popular");
                mainHandler.post(() -> popularAdapter.setSongs(popular));
            } catch (IOException e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "Failed to load popular", Toast.LENGTH_SHORT).show()
                );
            }
        });

        // HISTORY
        executor.execute(() -> {
            List<Song> history = historyRepo.getLast(currentUser, 6);
            mainHandler.post(() -> historyAdapter.setSongs(history));
        });
    }

    // -------------------------
    // CLICK SONG
    // -------------------------
    @Override
    public void onSongClicked(Song song) {
        executor.execute(() -> historyRepo.save(currentUser, song));

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("trackId", song.getTrackId());
        intent.putExtra("title", song.getTitle());
        intent.putExtra("artist", song.getArtist());
        intent.putExtra("imageUrl", song.getImageUrl());
        intent.putExtra("streamUrl", song.getStreamUrl());

        startActivity(intent);
    }

    // -------------------------
    // LIFECYCLE
    // -------------------------
    @Override
    protected void onResume() {
        super.onResume();
        executor.execute(() -> {
            List<Song> history = historyRepo.getLast(currentUser, 6);
            mainHandler.post(() -> historyAdapter.setSongs(history));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
