package com.frangomez.buenasymalas.ui.historial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.ui.Fotos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Las últimas partidas entre dos jugadores. */
public class PartidasAdapter extends RecyclerView.Adapter<PartidasAdapter.Fila> {

    /** Una partida ya resuelta para mostrar: los joins se hacen fuera del hilo principal. */
    public static class Item {
        public String resultado;
        public String meta;
        public int colorBarra;
        @Nullable
        public String fotoPath;
    }

    private final List<Item> items = new ArrayList<>();

    public void setItems(List<Item> nuevos) {
        items.clear();
        items.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Fila(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fila_partida, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int position) {
        Item item = items.get(position);
        fila.resultado.setText(item.resultado);
        fila.meta.setText(item.meta);
        fila.barra.setBackgroundColor(item.colorBarra);

        if (item.fotoPath != null && new File(item.fotoPath).exists()) {
            fila.miniatura.setVisibility(View.VISIBLE);
            fila.miniatura.setImageBitmap(Fotos.miniatura(item.fotoPath, 8));
        } else {
            fila.miniatura.setVisibility(View.GONE);
            fila.miniatura.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final View barra;
        final TextView resultado;
        final TextView meta;
        final ImageView miniatura;

        Fila(@NonNull View itemView) {
            super(itemView);
            barra = itemView.findViewById(R.id.barra);
            resultado = itemView.findViewById(R.id.resultado);
            meta = itemView.findViewById(R.id.meta);
            miniatura = itemView.findViewById(R.id.miniatura);
        }
    }
}
