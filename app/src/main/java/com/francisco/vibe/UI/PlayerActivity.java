package com.francisco.vibe.UI;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.francisco.vibe.Data.FavoritesRepository;
import com.francisco.vibe.Data.SessionManager;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.R;
import com.francisco.vibe.databinding.ActivityPlayerBinding;

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

        // ================= USER =================
        currentUser = SessionManager.getUsername(this);
        favoritesRepo = new FavoritesRepository(this);

        // ================= INTENT DATA =================
        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String streamUrl = getIntent().getStringExtra("streamUrl");

        currentSong = new Song(title, artist, imageUrl, streamUrl);

        // ================= UI =================
        binding.songTitle.setText(title);
        binding.songArtist.setText(artist);
        Glide.with(this).load(imageUrl).into(binding.headerArt);

        // ================= FAVORITES =================
        isFavorite = favoritesRepo.isFavorite(currentUser, streamUrl);
        updateFavoriteIcon();

        // ================= PLAYER =================
        player = new ExoPlayer.Builder(this).build();
        player.setMediaItem(MediaItem.fromUri(Uri.parse(streamUrl)));
        player.prepare();
        player.play();

        setupControls();
        setupPlayerListener();
        startSeekBarUpdate();
    }

    // ================= CONTROLS =================

    private void setupControls() {

        // BACK
        binding.btnBack.setOnClickListener(v -> {
            feedback(v);
            finish();
        });

        // FAVORITE
        binding.btnFavorite.setOnClickListener(v -> {
            feedback(v);
            favoritesRepo.toggle(currentUser, currentSong);
            isFavorite = !isFavorite;
            updateFavoriteIcon();
        });

        // PLAY / PAUSE
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

        // PREVIOUS
        binding.btnPrev.setOnClickListener(v -> {
            feedback(v);
            if (player != null) {
                player.seekTo(0);
            }
        });

        // NEXT
        binding.btnNext.setOnClickListener(v -> {
            feedback(v);
            if (player != null) {
                player.seekTo(0);
            }
        });

        // SHUFFLE
        binding.btnShuffle.setOnClickListener(v -> {
            feedback(v);
            isShuffle = !isShuffle;
            binding.btnShuffle.setAlpha(isShuffle ? 1f : 0.4f);
        });

        // REPEAT
        binding.btnRepeat.setOnClickListener(v -> {
            feedback(v);
            isRepeat = !isRepeat;

            player.setRepeatMode(
                    isRepeat
                            ? Player.REPEAT_MODE_ONE
                            : Player.REPEAT_MODE_OFF
            );

            binding.btnRepeat.setAlpha(isRepeat ? 1f : 0.4f);
        });

        // SEEK BAR
        binding.seekBar.setOnSeekBarChangeListener(
                new android.widget.SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(
                            android.widget.SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {
                        if (fromUser && player != null) {
                            player.seekTo(progress);
                        }
                    }

                    @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
                }
        );
    }

    // ================= PLAYER LISTENER =================

    private void setupPlayerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    binding.btnPlay.setImageResource(R.drawable.ic_play_filled_24);
                }
            }
        });
    }

    // ================= SEEK BAR =================

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

    // ================= FAVORITE UI =================

    private void updateFavoriteIcon() {
        int color = isFavorite
                ? getColor(R.color.md_theme_error)
                : getColor(android.R.color.white);

        binding.btnFavorite.setColorFilter(color);
        binding.btnFavorite.setAlpha(isFavorite ? 1f : 0.5f);
    }


    // ================= FEEDBACK =================

    private void feedback(View v) {
        v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

        v.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(80)
                .withEndAction(() ->
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                )
                .start();
    }

    // ================= CLEANUP =================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
