package com.frangomez.buenasymalas.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un lado del marcador en una partida. En 1v1 es un jugador solo.
 *
 * <p>El {@link #rosterKey} son los ids de sus integrantes ordenados y unidos por guion. Es lo
 * que le da identidad estable a una dupla entre partidas distintas: sin el, "Rocho y Fede"
 * seria un equipo nuevo cada vez y no habria record de la dupla armada.
 */
@Entity(tableName = "team", indices = {@Index("match_id"), @Index("roster_key")})
public class Team {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "match_id")
    public long matchId;

    @NonNull
    @ColumnInfo(name = "roster_key")
    public String rosterKey = "";

    public Team() {
    }

    @androidx.room.Ignore
    public Team(long matchId, @NonNull String rosterKey) {
        this.matchId = matchId;
        this.rosterKey = rosterKey;
    }

    /** Arma la clave de un roster a partir de los ids de sus jugadores. */
    @NonNull
    public static String rosterKeyDe(@NonNull List<Long> playerIds) {
        List<Long> ordenados = new ArrayList<>(playerIds);
        Collections.sort(ordenados);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordenados.size(); i++) {
            if (i > 0) {
                sb.append('-');
            }
            sb.append(ordenados.get(i));
        }
        return sb.toString();
    }
}
