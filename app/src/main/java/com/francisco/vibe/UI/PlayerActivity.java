package com.francisco.vibe.UI;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.francisco.vibe.databinding.ActivityPlayerBinding;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String streamUrl = getIntent().getStringExtra("streamUrl");

        binding.songTitle.setText(title);
        binding.songArtist.setText(artist);
        Glide.with(this).load(imageUrl).into(binding.headerArt);

        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        MediaItem mediaItem = new MediaItem.Builder().setUri(Uri.parse(streamUrl)).build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        binding.backButton.setOnClickListener(v -> finish());
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
