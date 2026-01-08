package com.francisco.vibe.UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.media3.common.util.UnstableApi;
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

    /**
     * Inicializa a PlaylistDetailActivity.
     * Obtém os dados da playlist enviados por Intent, prepara a interface,
     * configura o RecyclerView e carrega as músicas da playlist.
     */
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
                this,
                song -> showRemoveSongDialog(song)
        );

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.setAdapter(adapter);

        loadSongs();
    }

    /**
     * Apresenta uma caixa de diálogo de confirmação para remover uma música da playlist.
     * Caso o utilizador confirme, a música é removida e a lista é atualizada.
     */
    private void showRemoveSongDialog(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Song")
                .setMessage("Are you sure you want to remove \"" + song.getTitle() + "\" from this playlist?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    repo.removeSongFromPlaylist(playlistId, song);
                    loadSongs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Carrega as músicas da playlist a partir do repositório
     * e atualiza o adapter do RecyclerView.
     */
    private void loadSongs() {
        List<Song> songs = repo.getSongs(playlistId);
        adapter.setSongs(songs);
    }

    /**
     * Trata o evento de clique numa música da playlist.
     * Abre o PlayerActivity e inicia a reprodução a partir da música selecionada.
     */
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onSongClicked(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("playlistId", playlistId);
        intent.putExtra("startIndex", adapter.getSongs().indexOf(song));

        startActivity(intent);
    }

}
