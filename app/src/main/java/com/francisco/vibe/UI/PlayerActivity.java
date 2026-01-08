package com.francisco.vibe.UI;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;

import com.bumptech.glide.Glide;
import com.francisco.vibe.Data.FavoritesRepository;
import com.francisco.vibe.Data.PlaylistRepository;
import com.francisco.vibe.Data.PlaylistSongsRepository;
import com.francisco.vibe.Data.SessionManager;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.R;
import com.francisco.vibe.databinding.ActivityPlayerBinding;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ExoPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private FavoritesRepository favoritesRepo;
    private Song currentSong;
    private String currentUser;

    private List<Song> songs = new ArrayList<>();

    private boolean autoPlayed = false;

    private boolean isFavorite = false;

    /**
     * Inicializa a PlayerActivity.
     * Recebe os dados enviados por Intent, prepara a lista de músicas tocáveis,
     * inicializa o ExoPlayer, configura os listeners e prepara a interface.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = SessionManager.getUsername(this);
        favoritesRepo = new FavoritesRepository(this);

        String trackId = getIntent().getStringExtra("trackId");
        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String streamUrl = getIntent().getStringExtra("streamUrl");
        int playlistId = getIntent().getIntExtra("playlistId", -1);
        int startIndex = getIntent().getIntExtra("startIndex", 0);

        List<Song> rawSongs = new ArrayList<>();

        if (playlistId != -1) {
            PlaylistSongsRepository repo = new PlaylistSongsRepository(this);
            rawSongs = repo.getSongs(playlistId);
        } else if (trackId != null) {
            rawSongs.add(new Song(trackId, title, artist, imageUrl, streamUrl));
        }

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        15000,
                        60000,
                        2500,
                        6000
                )
                .build();

        player = new ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();

        player.setShuffleModeEnabled(false);
        player.setRepeatMode(Player.REPEAT_MODE_OFF);

        List<MediaItem> mediaItems = new ArrayList<>();
        List<Song> playableSongs = new ArrayList<>();

        for (Song s : rawSongs) {
            if (s.getStreamUrl() != null && !s.getStreamUrl().isEmpty()) {
                playableSongs.add(s);

                MediaItem item = new MediaItem.Builder()
                        .setUri(s.getStreamUrl())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setTitle(s.getTitle())
                                        .setArtist(s.getArtist())
                                        .setArtworkUri(
                                                (s.getImageUrl() != null && !s.getImageUrl().isEmpty())
                                                        ? Uri.parse(s.getImageUrl())
                                                        : null
                                        )
                                        .build()
                        )
                        .build();

                mediaItems.add(item);
            } else {
                Toast.makeText(this, "Track unavailable: " + s.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        songs = playableSongs;

        if (!mediaItems.isEmpty()) {
            int safeStartIndex = Math.max(0, Math.min(startIndex, mediaItems.size() - 1));
            player.setMediaItems(mediaItems, safeStartIndex, 0);
            player.prepare();
        } else {
            Toast.makeText(this, "No playable tracks.", Toast.LENGTH_SHORT).show();
        }

        player.addListener(new Player.Listener() {

            /**
             * Reage a alterações no estado de reprodução.
             * Inicia automaticamente a reprodução quando o player fica pronto
             * e atualiza o botão de play/pause conforme necessário.
             */
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY && !autoPlayed) {
                    player.play();
                    autoPlayed = true;
                    binding.btnPlay.setImageResource(R.drawable.pause_button);
                } else if (state == Player.STATE_ENDED) {

                    if (player != null && player.getRepeatMode() == Player.REPEAT_MODE_ALL) {
                        player.seekTo(0, 0);
                        player.prepare();
                        player.play();
                        return;
                    }

                    binding.btnPlay.setImageResource(R.drawable.ic_play_filled_24);
                }
            }

            /**
             * Controla o estado de interação dos botões com base no carregamento,
             * evitando ações enquanto o player está a preparar ou a carregar dados.
             */
            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                binding.btnPlay.setEnabled(!isLoading);
                binding.btnNext.setEnabled(!isLoading);
                binding.btnPrev.setEnabled(!isLoading);
            }

            /**
             * Trata erros de reprodução.
             * Tenta recuperar automaticamente avançando para a próxima faixa,
             * ou recomeçando a reprodução quando não existem mais itens.
             */
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Cannot play track", Toast.LENGTH_SHORT).show();

                if (player != null && player.hasNextMediaItem()) {
                    player.seekToNextMediaItem();
                    player.prepare();
                    player.play();
                } else if (player != null) {
                    player.seekTo(0);
                    player.prepare();
                    player.play();
                }
            }

            /**
             * Atualiza a informação apresentada quando ocorre transição de música,
             * sincronizando a música atual com o índice do ExoPlayer.
             */
            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                int idx = player.getCurrentMediaItemIndex();
                if (idx >= 0 && idx < songs.size()) {
                    currentSong = songs.get(idx);
                    updateUIForSong(currentSong);
                }
            }
        });

        if (!songs.isEmpty()) {
            int idx = Math.max(0, Math.min(startIndex, songs.size() - 1));
            currentSong = songs.get(idx);
            updateUIForSong(currentSong);
        }

        setupControls();
        binding.btnAddToPlaylist.setOnClickListener(v -> showAddToPlaylistDialog());

        syncToggleButtonsUI();

        startSeekBarUpdate();
    }

    /**
     * Atualiza os elementos visuais do ecrã com base na música atual,
     * incluindo título, artista, imagem e estado de favorito.
     */
    private void updateUIForSong(Song song) {
        binding.songTitle.setText(song.getTitle());
        binding.songArtist.setText(song.getArtist());

        Glide.with(this)
                .load(song.getImageUrl())
                .into(binding.headerArt);

        isFavorite = favoritesRepo.isFavorite(currentUser, song.getTrackId());
        updateFavoriteIcon();
    }

    /**
     * Configura os controlos do player (botões e seekbar),
     * definindo as ações associadas a cada interação do utilizador.
     */
    private void setupControls() {

        binding.btnBack.setOnClickListener(v -> {
            feedback(v);
            stopPlayerAndCleanup();
            finish();
        });

        binding.btnFavorite.setOnClickListener(v -> {
            feedback(v);
            if (currentSong == null) return;
            favoritesRepo.toggle(currentUser, currentSong);
            isFavorite = favoritesRepo.isFavorite(currentUser, currentSong.getTrackId());
            updateFavoriteIcon();
        });

        binding.btnPlay.setOnClickListener(v -> {
            feedback(v);
            if (player == null) return;

            if (player.isPlaying()) {
                player.pause();
                binding.btnPlay.setImageResource(R.drawable.ic_play_filled_24);
            } else {
                player.play();
                binding.btnPlay.setImageResource(R.drawable.pause_button);
            }
        });

        binding.btnPrev.setOnClickListener(v -> {
            feedback(v);
            if (player != null && player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem();
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            feedback(v);
            if (player != null && player.hasNextMediaItem()) {
                player.seekToNextMediaItem();
            }
        });

        binding.btnShuffle.setOnClickListener(v -> {
            feedback(v);
            if (player == null) return;

            boolean enabled = !player.getShuffleModeEnabled();
            player.setShuffleModeEnabled(enabled);
            syncToggleButtonsUI();
        });

        binding.btnRepeat.setOnClickListener(v -> {
            feedback(v);
            if (player == null) return;

            int mode = player.getRepeatMode();
            int newMode;

            if (mode == Player.REPEAT_MODE_OFF) {
                newMode = Player.REPEAT_MODE_ONE;
                Toast.makeText(this, "Repeat one", Toast.LENGTH_SHORT).show();
            } else if (mode == Player.REPEAT_MODE_ONE) {
                newMode = Player.REPEAT_MODE_ALL;
                Toast.makeText(this, "Repeat all", Toast.LENGTH_SHORT).show();
            } else {
                newMode = Player.REPEAT_MODE_OFF;
                Toast.makeText(this, "Repeat off", Toast.LENGTH_SHORT).show();
            }

            player.setRepeatMode(newMode);
            syncToggleButtonsUI();
        });

        binding.seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) player.seekTo(progress);
            }

            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
    }

    /**
     * Sincroniza a interface gráfica dos botões de alternância (shuffle e repeat)
     * com o estado atual definido no ExoPlayer.
     */
    private void syncToggleButtonsUI() {
        if (player == null) return;
        binding.btnShuffle.setAlpha(player.getShuffleModeEnabled() ? 1f : 0.4f);
        binding.btnRepeat.setAlpha(player.getRepeatMode() != Player.REPEAT_MODE_OFF ? 1f : 0.4f);
    }

    /**
     * Apresenta um diálogo que permite ao utilizador adicionar a música atual
     * a uma das playlists existentes.
     */
    private void showAddToPlaylistDialog() {
        if (currentSong == null) return;

        PlaylistRepository playlistRepo = new PlaylistRepository(this);
        var playlists = playlistRepo.getAll(currentUser);

        if (playlists.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("No Playlists")
                    .setMessage("You don't have any playlists. Create one first!")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] playlistNames = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            playlistNames[i] = playlists.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Add to Playlist")
                .setItems(playlistNames, (dialog, which) -> {
                    int playlistId = playlists.get(which).getId();
                    PlaylistSongsRepository repo = new PlaylistSongsRepository(this);

                    boolean added = repo.addSong(playlistId, currentSong);
                    Toast.makeText(this,
                            added ? "Song added to playlist" : "Song already in playlist",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * Inicia a atualização periódica da seekbar e dos tempos de reprodução,
     * mantendo a interface sincronizada com a posição atual do player.
     */
    private void startSeekBarUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    long position = player.getCurrentPosition();
                    long duration = player.getDuration();

                    if (duration > 0) {
                        binding.seekBar.setMax((int) duration);
                        binding.seekBar.setProgress((int) position);
                        binding.timeStart.setText(formatTime(position));
                        binding.timeEnd.setText(formatTime(duration));
                    }
                }
                handler.postDelayed(this, 500);
            }
        }, 0);
    }

    /**
     * Converte um valor em milissegundos para o formato minutos:segundos,
     * para apresentação na interface.
     */
    private String formatTime(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Atualiza o ícone de favoritos, alterando cor e opacidade
     * consoante a música atual esteja ou não marcada como favorita.
     */
    private void updateFavoriteIcon() {
        int color = isFavorite ? getColor(R.color.md_theme_error) : getColor(android.R.color.white);
        binding.btnFavorite.setColorFilter(color);
        binding.btnFavorite.setAlpha(isFavorite ? 1f : 0.5f);
    }

    /**
     * Aplica feedback háptico e uma pequena animação ao botão pressionado,
     * reforçando a resposta visual à interação do utilizador.
     */
    private void feedback(View v) {
        v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        v.animate()
                .scaleX(0.9f).scaleY(0.9f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }

    /**
     * Interrompe a reprodução, liberta os recursos do ExoPlayer
     * e remove as atualizações pendentes do Handler.
     */
    private void stopPlayerAndCleanup() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Liberta recursos quando a Activity é destruída,
     * evitando memory leaks e mantendo o comportamento estável.
     */
    @Override
    protected void onDestroy() {
        stopPlayerAndCleanup();
        super.onDestroy();
    }
}
