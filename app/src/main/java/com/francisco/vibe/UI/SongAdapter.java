package com.francisco.vibe.UI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.francisco.vibe.Data.Song;
import com.francisco.vibe.R;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private final List<Song> songs;
    private final OnSongClickListener listener;
    private final OnSongLongClickListener longClickListener;

    public interface OnSongClickListener {
        void onSongClicked(Song song);
    }

    public interface OnSongLongClickListener {
        void onSongLongClicked(Song song);
    }
    // Existing constructor for favorites etc.
    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this(songs, listener, null); // pass null for long-click
    }

    // New constructor for playlists
    public SongAdapter(List<Song> songs, OnSongClickListener listener, OnSongLongClickListener longClickListener) {
        this.songs = songs;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public List<Song> getSongs() {
        return songs;
    }


    public void setSongs(List<Song> newSongs) {
        songs.clear();
        songs.addAll(newSongs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        Log.d("SongAdapter", "Loading image: " + song.getImageUrl());

        Glide.with(holder.itemView.getContext()).load(song.getImageUrl()).into(holder.cover);

        holder.itemView.setOnClickListener(v -> listener.onSongClicked(song));

        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onSongLongClicked(song);
                return true; // consume the event
            });
        }
    }


    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView cover;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
            cover = itemView.findViewById(R.id.song_cover);
        }
    }
}
