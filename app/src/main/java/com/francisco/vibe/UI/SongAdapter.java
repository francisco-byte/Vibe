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

    /**
     * Interface responsável por definir o comportamento
     * quando uma música é selecionada pelo utilizador.
     */
    public interface OnSongClickListener {
        void onSongClicked(Song song);
    }

    /**
     * Interface responsável por definir o comportamento
     * quando uma música é pressionada de forma prolongada.
     */
    public interface OnSongLongClickListener {
        void onSongLongClicked(Song song);
    }

    /**
     * Construtor utilizado em listas que não necessitam de clique prolongado,
     * como por exemplo a lista de favoritos.
     */
    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this(songs, listener, null);
    }

    /**
     * Construtor utilizado em listas que necessitam de clique normal e clique prolongado,
     * como por exemplo a gestão de músicas dentro de uma playlist.
     */
    public SongAdapter(List<Song> songs, OnSongClickListener listener, OnSongLongClickListener longClickListener) {
        this.songs = songs;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    /**
     * Devolve a lista de músicas atualmente associada ao adapter.
     */
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * Atualiza o conteúdo da lista de músicas e força a atualização do RecyclerView.
     */
    public void setSongs(List<Song> newSongs) {
        songs.clear();
        songs.addAll(newSongs);
        notifyDataSetChanged();
    }

    /**
     * Cria e devolve um ViewHolder associado ao layout de cada item de música.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Associa os dados de uma música ao item do RecyclerView,
     * carregando o título, artista e capa, e configurando os eventos de clique.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        Glide.with(holder.itemView.getContext()).load(song.getImageUrl()).into(holder.cover);

        holder.itemView.setOnClickListener(v -> listener.onSongClicked(song));

        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onSongLongClicked(song);
                return true;
            });
        }
    }

    /**
     * Devolve o número total de músicas existentes na lista.
     */
    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * ViewHolder responsável por manter as referências aos elementos visuais
     * de cada item de música apresentado na lista.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView cover;

        /**
         * Inicializa os componentes visuais do item de música.
         */
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
            cover = itemView.findViewById(R.id.song_cover);
        }
    }
}
