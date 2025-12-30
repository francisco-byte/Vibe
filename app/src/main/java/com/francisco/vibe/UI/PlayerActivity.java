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
import androidx.media3.exoplayer.ExoPlayer;

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

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ExoPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private boolean isFavorite = false;

    private FavoritesRepository favoritesRepo;
    private Song currentSong;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = SessionManager.getUsername(this);
        favoritesRepo = new FavoritesRepository(this);

        // ===== GET INTENT DATA =====
        String trackId = getIntent().getStringExtra("trackId");
        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String streamUrl = getIntent().getStringExtra("streamUrl");
        int playlistId = getIntent().getIntExtra("playlistId", -1);
        int startIndex = getIntent().getIntExtra("startIndex", 0);

        List<Song> songs = new ArrayList<>();

        if (playlistId != -1) {
            PlaylistSongsRepository repo = new PlaylistSongsRepository(this);
            songs = repo.getSongs(playlistId);
        } else if (trackId != null) {
            currentSong = new Song(trackId, title, artist, imageUrl, streamUrl);
            songs.add(currentSong);
        }

        // ===== INITIALIZE PLAYER =====
        player = new ExoPlayer.Builder(this).build();

        // ===== ADD MEDIA ITEMS =====
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song s : songs) {
            if (s.getStreamUrl() != null && !s.getStreamUrl().isEmpty()) {
                mediaItems.add(new MediaItem.Builder()
                        .setUri(s.getStreamUrl())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setTitle(s.getTitle())
                                        .setArtist(s.getArtist())
                                        .setArtworkUri(Uri.parse(s.getImageUrl()))
                                        .build()
                        )
                        .build()
                );
            } else {
                Toast.makeText(this, "Track unavailable: " + s.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        if (!mediaItems.isEmpty()) {
            player.setMediaItems(mediaItems, startIndex, 0);
            player.prepare();
        }

        // ===== PLAYER LISTENER =====
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY && !player.isPlaying()) {
                    player.play(); // auto-play when ready
                    binding.btnPlay.setImageResource(R.drawable.pause_button);
                } else if (state == Player.STATE_ENDED) {
                    binding.btnPlay.setImageResource(R.drawable.ic_play_filled_24);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Cannot play track", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                MediaMetadata meta = mediaItem.mediaMetadata;
                currentSong = new Song(
                        "",
                        meta.title != null ? meta.title.toString() : "",
                        meta.artist != null ? meta.artist.toString() : "",
                        meta.artworkUri != null ? meta.artworkUri.toString() : "",
                        mediaItem.localConfiguration != null ? mediaItem.localConfiguration.uri.toString() : ""
                );
                updateUIForSong(currentSong);
            }
        });

        // ===== SETUP UI FOR FIRST SONG =====
        if (!songs.isEmpty()) {
            currentSong = songs.get(Math.min(startIndex, songs.size() - 1));
            updateUIForSong(currentSong);
        }

        setupControls();
        binding.btnAddToPlaylist.setOnClickListener(v -> showAddToPlaylistDialog());
        startSeekBarUpdate();
    }

    private void updateUIForSong(Song song) {
        binding.songTitle.setText(song.getTitle());
        binding.songArtist.setText(song.getArtist());
        Glide.with(this).load(song.getImageUrl()).into(binding.headerArt);

        isFavorite = favoritesRepo.isFavorite(currentUser, song.getTrackId());
        updateFavoriteIcon();
    }

    private void setupControls() {
        // ===== CUSTOM BACK BUTTON =====
        binding.btnBack.setOnClickListener(v -> {
            feedback(v);
            stopPlayerAndCleanup();
            finish();
        });

        binding.btnFavorite.setOnClickListener(v -> {
            feedback(v);
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
            if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem();
        });

        binding.btnNext.setOnClickListener(v -> {
            feedback(v);
            if (player.hasNextMediaItem()) player.seekToNextMediaItem();
        });

        binding.btnShuffle.setOnClickListener(v -> {
            feedback(v);
            isShuffle = !isShuffle;
            player.setShuffleModeEnabled(isShuffle);
            binding.btnShuffle.setAlpha(isShuffle ? 1f : 0.4f);
        });

        binding.btnRepeat.setOnClickListener(v -> {
            feedback(v);
            isRepeat = !isRepeat;
            player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
            binding.btnRepeat.setAlpha(isRepeat ? 1f : 0.4f);
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

    private void showAddToPlaylistDialog() {
        PlaylistRepository playlistRepo = new PlaylistRepository(this);
        var playlists = playlistRepo.getAll(currentUser);
        if (playlists.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("No Playlists")
                    .setMessage("You don't have any playlists. Create one first!")
                    .setPositiveButton("OK", null).show();
            return;
        }

        String[] playlistNames = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) playlistNames[i] = playlists.get(i).getName();

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

    private String formatTime(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void updateFavoriteIcon() {
        int color = isFavorite ? getColor(R.color.md_theme_error) : getColor(android.R.color.white);
        binding.btnFavorite.setColorFilter(color);
        binding.btnFavorite.setAlpha(isFavorite ? 1f : 0.5f);
    }

    private void feedback(View v) {
        v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        v.animate()
                .scaleX(0.9f).scaleY(0.9f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }

    private void stopPlayerAndCleanup() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        stopPlayerAndCleanup();
        super.onDestroy();
    }
}
