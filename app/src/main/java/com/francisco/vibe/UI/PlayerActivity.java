package com.francisco.vibe.UI;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.francisco.vibe.R;
import com.francisco.vibe.databinding.ActivityPlayerBinding;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ExoPlayer player;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Intent data
        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String streamUrl = getIntent().getStringExtra("streamUrl");

        // UI
        binding.songTitle.setText(title);
        binding.songArtist.setText(artist);
        Glide.with(this).load(imageUrl).into(binding.headerArt);

        // Player
        player = new ExoPlayer.Builder(this).build();
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(streamUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        setupControls();
        startSeekBarUpdate();
    }

    private void setupControls() {

        // BACK
        binding.btnBack.setOnClickListener(v -> finish());

        // PLAY / PAUSE
        binding.btnPlay.setOnClickListener(v -> {
            if (player == null) return;

            if (player.isPlaying()) {
                player.pause();
                binding.btnPlay.setImageResource(R.drawable.ic_play_filled_24);

            } else {
                player.play();
                binding.btnPlay.setImageResource(R.drawable.outline_alarm_pause_24);
            }
        });

        // SEEK BAR DRAG
        binding.seekBar.setOnSeekBarChangeListener(
                new android.widget.SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && player != null) {
                            player.seekTo(progress);
                        }
                    }

                    @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
                }
        );
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
