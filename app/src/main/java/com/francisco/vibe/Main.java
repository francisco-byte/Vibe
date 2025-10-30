package com.francisco.vibe;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.francisco.vibe.Data.Song;
import com.francisco.vibe.databinding.MainActivityBinding;
import com.francisco.vibe.UI.PlayerActivity;
import com.francisco.vibe.UI.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private MainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // ✅ Use ViewBinding to load main_activity.xml
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Build demo songs list
        List<Song> demoSongs = getDemoSongs();

        // ✅ Connect RecyclerView
        SongAdapter adapter = new SongAdapter(demoSongs, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private List<Song> getDemoSongs() {
        List<Song> list = new ArrayList<>();
        list.add(new Song("Vibe Flow", "DJ Nova",
                "https://picsum.photos/seed/vibe1/600/600",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
        list.add(new Song("Ocean Drive", "Sunset Avenue",
                "https://picsum.photos/seed/vibe2/600/600",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"));
        list.add(new Song("City Lights", "Synthwave Soul",
                "https://picsum.photos/seed/vibe3/600/600",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"));
        return list;
    }

    @Override
    public void onSongClicked(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("title", song.getTitle());
        intent.putExtra("artist", song.getArtist());
        intent.putExtra("imageUrl", song.getImageUrl());
        intent.putExtra("streamUrl", song.getStreamUrl());
        startActivity(intent);
    }
}
