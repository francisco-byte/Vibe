package com.francisco.vibe.Data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.francisco.vibe.Data.Playlist;
import com.francisco.vibe.R;

import java.util.List;

public class PlaylistAdapter
        extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    private List<Playlist> playlists;
    private final OnPlaylistClickListener listener;

    public PlaylistAdapter(
            List<Playlist> playlists,
            OnPlaylistClickListener listener
    ) {
        this.playlists = playlists;
        this.listener = listener;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Playlist playlist = playlists.get(position);
        holder.name.setText(playlist.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistClick(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvPlaylistName);
        }
    }
}
