package com.frangomez.buenasymalas.ui;

import android.content.Context;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Match;
import com.frangomez.buenasymalas.data.Player;

import java.util.Calendar;
import java.util.List;

/** Cómo se escriben las partidas en el historial y en el museo. */
public final class Formatos {

    private Formatos() {
    }

    /** "Rocho y Fede", o el nombre solo en 1v1. */
    public static String nombreDeEquipo(List<Player> integrantes) {
        StringBuilder sb = new StringBuilder();
        for (Player p : integrantes) {
            if (sb.length() > 0) {
                sb.append(" y ");
            }
            sb.append(p.name);
        }
        return sb.toString();
    }

    /** "Rocho 30 — 22 Nacho", siempre con el ganador a la izquierda. */
    public static String resultado(Context ctx, Match m, String nombreA, String nombreB) {
        boolean ganoA = m.winnerTeamId == m.teamAId;
        return ctx.getString(R.string.resultado_final,
                ganoA ? nombreA : nombreB,
                Math.max(m.scoreA, m.scoreB),
                Math.min(m.scoreA, m.scoreB),
                ganoA ? nombreB : nombreA);
    }

    /** "hoy · 2v2 · paliza". La fecha exacta importa menos que si fue hoy o ayer. */
    public static String meta(Context ctx, Match m) {
        StringBuilder sb = new StringBuilder(fecha(ctx, m.playedAt));
        sb.append(" · ").append(ctx.getString(R.string.formato_meta, m.teamSize));
        if (m.esPaliza()) {
            sb.append(" · ").append(ctx.getString(R.string.paliza));
        }
        return sb.toString();
    }

    public static String fecha(Context ctx, long cuando) {
        Calendar hoy = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(cuando);

        if (mismoDia(hoy, c)) {
            return ctx.getString(R.string.fecha_hoy);
        }
        hoy.add(Calendar.DAY_OF_YEAR, -1);
        if (mismoDia(hoy, c)) {
            return ctx.getString(R.string.fecha_ayer);
        }
        return android.text.format.DateFormat.getDateFormat(ctx).format(c.getTime());
    }

    private static boolean mismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
}
