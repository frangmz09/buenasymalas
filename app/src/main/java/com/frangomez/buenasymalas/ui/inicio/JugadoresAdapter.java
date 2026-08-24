package com.frangomez.buenasymalas.ui.inicio;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** La lista de "en la mesa": quién juega, cómo le viene yendo y quién reparte primero. */
public class JugadoresAdapter extends RecyclerView.Adapter<JugadoresAdapter.Fila> {

    /** Jugadas y ganadas por jugador. Llegan de la base, así que pueden tardar. */
    private final Map<Long, int[]> stats = new HashMap<>();
    private final List<Player> jugadores = new ArrayList<>();

    private final Escuchas escuchas;
    private long manoId;

    public interface Escuchas {
        void alTocarMano(Player jugador);

        void alTocarJugador(Player jugador);
    }

    public JugadoresAdapter(Escuchas escuchas) {
        this.escuchas = escuchas;
    }

    public void setJugadores(List<Player> nuevos) {
        jugadores.clear();
        jugadores.addAll(nuevos);
        notifyDataSetChanged();
    }

    public void setStats(Map<Long, int[]> nuevas) {
        stats.clear();
        stats.putAll(nuevas);
        notifyDataSetChanged();
    }

    /** La mano es exclusiva: dársela a uno se la saca al anterior. */
    public void setMano(long playerId) {
        manoId = playerId;
        notifyDataSetChanged();
    }

    public long getMano() {
        return manoId;
    }

    public List<Player> getJugadores() {
        return jugadores;
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fila_jugador, parent, false);
        return new Fila(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int position) {
        Player jugador = jugadores.get(position);
        fila.nombre.setText(jugador.name);
        fila.avatar.setText(jugador.inicial());

        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(jugador.color);
        fila.avatar.setBackground(circulo);

        int[] s = stats.get(jugador.id);
        fila.stats.setText(s == null
                ? jugador.alias
                : fila.itemView.getContext().getString(R.string.stats_jugador, s[0], s[1]));

        fila.mano.setSelected(jugador.id == manoId);
        fila.mano.setOnClickListener(v -> escuchas.alTocarMano(jugador));
        fila.itemView.setOnClickListener(v -> escuchas.alTocarJugador(jugador));
    }

    @Override
    public int getItemCount() {
        return jugadores.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView avatar;
        final TextView nombre;
        final TextView stats;
        final TextView mano;

        Fila(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            nombre = itemView.findViewById(R.id.nombre);
            stats = itemView.findViewById(R.id.stats);
            mano = itemView.findViewById(R.id.mano);
        }
    }
}
