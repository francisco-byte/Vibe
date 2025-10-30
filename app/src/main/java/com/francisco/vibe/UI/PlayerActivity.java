package com.francisco.vibe.UI;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.francisco.vibe.databinding.ActivityPlayerBinding;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String streamUrl = getIntent().getStringExtra("streamUrl");

        // Set UI
        binding.songTitle.setText(title);
        binding.songArtist.setText(artist);
        Glide.with(this).load(imageUrl).into(binding.headerArt);

        // Create ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        // Create and prepare media
        MediaItem item = new MediaItem.Builder()
                .setUri(Uri.parse(streamUrl))
                .build();

        player.setMediaItem(item);
        player.prepare();
        player.setPlayWhenReady(true); // 👈 ensure autoplay

        // Listener for debugging / status
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_IDLE:
                        System.out.println("Player: IDLE");
                        break;
                    case Player.STATE_BUFFERING:
                        System.out.println("Player: BUFFERING...");
                        break;
                    case Player.STATE_READY:
                        System.out.println("Player: READY — should be playing!");
                        break;
                    case Player.STATE_ENDED:
                        System.out.println("Player: ENDED");
                        break;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                System.out.println("⚠️ Playback error: " + error.getMessage());
                error.printStackTrace();
            }
        });

        // Back button
        binding.backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            binding.playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }
}
