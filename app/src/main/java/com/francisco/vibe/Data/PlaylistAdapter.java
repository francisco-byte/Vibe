package com.francisco.vibe.Data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.francisco.vibe.R;

import java.util.List;

public class PlaylistAdapter
        extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    /**
     * Interface responsável por definir o comportamento
     * quando uma playlist é selecionada pelo utilizador.
     */
    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    private List<Playlist> playlists;
    private final OnPlaylistClickListener listener;

    /**
     * Construtor do adapter das playlists.
     * Inicializa a lista de playlists e o listener de cliques.
     */
    public PlaylistAdapter(
            List<Playlist> playlists,
            OnPlaylistClickListener listener
    ) {
        this.playlists = playlists;
        this.listener = listener;
    }

    /**
     * Atualiza a lista de playlists apresentada no RecyclerView
     * e notifica o adapter para atualizar a interface.
     */
    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    /**
     * Cria e devolve um ViewHolder associado ao layout
     * de cada item da lista de playlists.
     */
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

    /**
     * Associa os dados da playlist à ViewHolder correspondente,
     * bem como o comportamento de clique sobre o item.
     */
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

    /**
     * Devolve o número total de playlists existentes na lista.
     */
    @Override
    public int getItemCount() {
        return playlists.size();
    }

    /**
     * ViewHolder responsável por manter as referências
     * aos elementos visuais de cada item da playlist.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        /**
         * Inicializa os componentes visuais do item da playlist.
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvPlaylistName);
        }
    }
}
