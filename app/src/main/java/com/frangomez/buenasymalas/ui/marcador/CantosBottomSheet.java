package com.frangomez.buenasymalas.ui.marcador;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.frangomez.buenasymalas.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * La hoja de cantos.
 *
 * <p>Cada fila tiene los puntos del canto y los dos lados de la mesa: se toca a quién se le
 * suman y la hoja baja, así el marcador queda a la vista con el punto ya puesto. El falta
 * envido se calcula al abrirla, porque vale lo que le falta al que va arriba en ese momento.
 */
public class CantosBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CON_FLOR = "conFlor";
    private static final String ARG_FALTA = "falta";
    private static final String ARG_NOMBRE_A = "nombreA";
    private static final String ARG_NOMBRE_B = "nombreB";
    private static final String ARG_COLOR_A = "colorA";
    private static final String ARG_COLOR_B = "colorB";

    public static CantosBottomSheet nueva(boolean conFlor, int faltaEnvido,
                                          String nombreA, String nombreB,
                                          int colorA, int colorB) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_CON_FLOR, conFlor);
        args.putInt(ARG_FALTA, faltaEnvido);
        args.putString(ARG_NOMBRE_A, nombreA);
        args.putString(ARG_NOMBRE_B, nombreB);
        args.putInt(ARG_COLOR_A, colorA);
        args.putInt(ARG_COLOR_B, colorB);

        CantosBottomSheet hoja = new CantosBottomSheet();
        hoja.setArguments(args);
        return hoja;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_cantos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        ViewGroup lista = view.findViewById(R.id.lista);
        view.findViewById(R.id.cerrar).setOnClickListener(v -> dismiss());

        int falta = args.getInt(ARG_FALTA);
        agregar(lista, R.string.canto_envido, 2, getString(R.string.no_querido, 1));
        agregar(lista, R.string.canto_real_envido, 3, getString(R.string.no_querido, 1));
        agregar(lista, R.string.canto_falta_envido, falta, getString(R.string.falta_hint));
        agregar(lista, R.string.canto_truco, 2, getString(R.string.no_querido, 1));
        agregar(lista, R.string.canto_retruco, 3, getString(R.string.no_querido, 2));
        agregar(lista, R.string.canto_vale_cuatro, 4, getString(R.string.no_querido, 3));

        if (args.getBoolean(ARG_CON_FLOR)) {
            agregar(lista, R.string.canto_flor, 3, getString(R.string.flor_hint));
            agregar(lista, R.string.canto_contraflor, 6, getString(R.string.no_querido, 4));
        }
    }

    private void agregar(ViewGroup lista, int nombreRes, int puntos, String aclaracion) {
        Bundle args = requireArguments();
        View fila = LayoutInflater.from(requireContext())
                .inflate(R.layout.fila_canto, lista, false);

        ((TextView) fila.findViewById(R.id.nombre)).setText(nombreRes);
        ((TextView) fila.findViewById(R.id.aclaracion)).setText(aclaracion);
        ((TextView) fila.findViewById(R.id.puntos)).setText(String.valueOf(puntos));

        lado(fila.findViewById(R.id.lado_a), args.getString(ARG_NOMBRE_A),
                args.getInt(ARG_COLOR_A), true, puntos);
        lado(fila.findViewById(R.id.lado_b), args.getString(ARG_NOMBRE_B),
                args.getInt(ARG_COLOR_B), false, puntos);

        lista.addView(fila);
    }

    /** El botón de un lado se pinta del color de su mitad de cancha, para no leer el nombre. */
    private void lado(TextView boton, String nombre, int color, boolean equipoA, int puntos) {
        boton.setText(nombre);
        boton.setTextColor(ContextCompat.getColor(requireContext(), R.color.tiza));

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 9f, getResources().getDisplayMetrics()));
        boton.setBackground(fondo);

        boton.setOnClickListener(v -> {
            Fragment padre = getParentFragment();
            if (padre instanceof MarcadorFragment) {
                ((MarcadorFragment) padre).sumarCanto(equipoA, puntos);
            }
            dismiss();
        });
    }
}
