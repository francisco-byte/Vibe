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

    private String currentFilter = "All";

    /**
     * Inicializa o ecrã principal da aplicação.
     * Valida a sessão do utilizador, configura a navegação, prepara os RecyclerViews,
     * inicializa a pesquisa com filtro e carrega os dados iniciais (populares e histórico).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);

        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = SessionManager.getUsername(this);
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        binding.btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        setupBottomNav();

        historyRepo = new SongHistoryRepository(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });

        setupRecyclerViews();
        setupSearchBarWithFilter();
        loadAllSections();
    }

    /**
     * Configura a navegação inferior (Bottom Navigation),
     * permitindo alternar entre o ecrã principal e a biblioteca.
     */
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

    /**
     * Configura os RecyclerViews do ecrã principal:
     * - Músicas populares
     * - Secção "ouvir novamente" (histórico)
     * - Resultados de pesquisa
     */
    private void setupRecyclerViews() {
        popularAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvPopular.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPopular.setAdapter(popularAdapter);

        historyAdapter = new SongAdapter(new ArrayList<>(), this);
        binding.rvListenAgain.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvListenAgain.setAdapter(historyAdapter);

        searchAdapter = new SongAdapter(new ArrayList<>(), this);
        RecyclerView rvSearch = binding.rvSearchResults;
        rvSearch.setLayoutManager(new LinearLayoutManager(this));
        rvSearch.setAdapter(searchAdapter);
    }

    /**
     * Configura a barra de pesquisa e o filtro (Spinner).
     * Sempre que o utilizador altera o texto ou o filtro, é executada uma pesquisa
     * e os resultados são apresentados no RecyclerView dedicado.
     */
    private void setupSearchBarWithFilter() {
        EditText searchInput = binding.searchInput;

        Spinner spinner = binding.searchFilter;
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.search_filter_options,
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

    /**
     * Obtém o filtro atualmente selecionado no Spinner da pesquisa.
     */
    private String getSelectedFilter() {
        return binding.searchFilter.getSelectedItem().toString();
    }

    /**
     * Executa a pesquisa na API Jamendo de acordo com o filtro selecionado.
     * A pesquisa é feita em background e os resultados são apresentados na interface.
     */
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
                        results = JamendoFetcher.search(query);
                        break;
                    default:
                        results = JamendoFetcher.search(query);
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

    /**
     * Carrega os dados principais do ecrã:
     * - Lista de músicas populares (via API)
     * - Histórico do utilizador (via base de dados local)
     */
    private void loadAllSections() {
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

        executor.execute(() -> {
            List<Song> history = historyRepo.getLast(currentUser, 6);
            mainHandler.post(() -> historyAdapter.setSongs(history));
        });
    }

    /**
     * Trata o clique numa música apresentada no ecrã principal.
     * Guarda a música no histórico e abre o PlayerActivity para iniciar a reprodução.
     */
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

    /**
     * Atualiza a secção do histórico quando a Activity volta a ficar ativa,
     * garantindo que a informação apresentada se mantém atualizada.
     */
    @Override
    protected void onResume() {
        super.onResume();
        executor.execute(() -> {
            List<Song> history = historyRepo.getLast(currentUser, 6);
            mainHandler.post(() -> historyAdapter.setSongs(history));
        });
    }

    /**
     * Liberta recursos ao terminar a Activity, encerrando o ExecutorService.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
