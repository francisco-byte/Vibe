package com.francisco.vibe.UI;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.databinding.ItemSongBinding;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public interface OnSongClickListener {
        void onSongClicked(Song song);
    }

    private final List<Song> songs;
    private final OnSongClickListener listener;

    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemSongBinding b;
        ViewHolder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSongBinding b = ItemSongBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song s = songs.get(position);
        holder.b.title.setText(s.getTitle());
        holder.b.artist.setText(s.getArtist());
        Glide.with(holder.itemView).load(s.getImageUrl()).into(holder.b.cover);
        holder.b.getRoot().setOnClickListener(v -> listener.onSongClicked(s));
    }

    @Override
    public int getItemCount() { return songs.size(); }
}
