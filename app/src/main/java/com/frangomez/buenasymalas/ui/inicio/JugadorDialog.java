package com.frangomez.buenasymalas.ui.inicio;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Player;

import java.util.ArrayList;
import java.util.List;

/** Alta y edición de un jugador: nombre, apodo de perdedor y color de su mitad de cancha. */
public final class JugadorDialog {

    public interface AlGuardar {
        void guardar(Player jugador);
    }

    private JugadorDialog() {
    }

    public static Dialog crear(@NonNull Context context, @Nullable Player existente,
                               @NonNull AlGuardar alGuardar) {
        View vista = LayoutInflater.from(context).inflate(R.layout.dialog_jugador, null);
        EditText nombre = vista.findViewById(R.id.nombre);
        EditText alias = vista.findViewById(R.id.alias);
        LinearLayout colores = vista.findViewById(R.id.colores);

        int[] paleta = paleta(context);
        int[] elegido = {existente != null ? existente.color : paleta[0]};

        if (existente != null) {
            nombre.setText(existente.name);
            alias.setText(existente.alias);
        }

        List<Runnable> repintar = new ArrayList<>();
        for (int color : paleta) {
            colores.addView(swatch(context, color, elegido, repintar));
        }

        return new AlertDialog.Builder(context)
                .setTitle(existente == null ? R.string.nuevo_jugador : R.string.editar_jugador)
                .setView(vista)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.guardar, (d, w) -> {
                    String n = nombre.getText().toString().trim();
                    if (n.isEmpty()) {
                        return;
                    }
                    Player jugador = existente != null ? existente : new Player();
                    jugador.name = n;
                    jugador.alias = alias.getText().toString().trim();
                    jugador.color = elegido[0];
                    if (jugador.createdAt == 0) {
                        jugador.createdAt = System.currentTimeMillis();
                    }
                    alGuardar.guardar(jugador);
                })
                .create();
    }

    /** Los seis colores de mitad de cancha, en el orden del diseño. */
    public static int[] paleta(Context context) {
        int[] ids = idsDePaleta(context);
        int[] colores = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            colores[i] = ContextCompat.getColor(context, ids[i]);
        }
        return colores;
    }

    private static int[] idsDePaleta(Context context) {
        android.content.res.TypedArray ta =
                context.getResources().obtainTypedArray(R.array.colores_cancha);
        int[] ids = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            ids[i] = ta.getResourceId(i, 0);
        }
        ta.recycle();
        return ids;
    }

    /** El elegido lleva un anillo de tiza; los demás, nada. */
    private static View swatch(Context context, int color, int[] elegido,
                               List<Runnable> repintar) {
        float d = context.getResources().getDisplayMetrics().density;
        View v = new View(context);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams((int) (38 * d), (int) (38 * d));
        lp.setMarginEnd((int) (8 * d));
        v.setLayoutParams(lp);

        Runnable pintar = () -> {
            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(color);
            fondo.setCornerRadius(10 * d);
            if (color == elegido[0]) {
                fondo.setStroke((int) (2 * d), ContextCompat.getColor(context, R.color.tiza));
            }
            v.setBackground(fondo);
        };
        pintar.run();
        repintar.add(pintar);

        v.setOnClickListener(x -> {
            elegido[0] = color;
            for (Runnable r : repintar) {
                r.run();
            }
        });
        return v;
    }
}
