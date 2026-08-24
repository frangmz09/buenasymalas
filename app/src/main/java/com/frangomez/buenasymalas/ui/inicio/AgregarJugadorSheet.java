package com.frangomez.buenasymalas.ui.inicio;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Player;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sumar a alguien a la mesa: buscar entre quienes ya jugaron antes, o crear uno nuevo.
 *
 * <p>Arranca mostrando a todo el mundo (menos quien ya está en la mesa) y filtra en memoria a
 * medida que se tipea — la lista de jugadores de esta app es un puñado de amigos, no hace falta
 * pegarle a la base por cada letra. Las stats sí se calculan una sola vez al abrir.
 */
public class AgregarJugadorSheet extends BottomSheetDialogFragment {

    private static final String ARG_EXCLUIR = "excluir";

    private TrucoRepository repo;
    private List<Player> todos = new ArrayList<>();
    private final Map<Long, int[]> stats = new HashMap<>();
    private long[] excluir = new long[0];

    private EditText buscar;
    private ViewGroup resultados;
    private TextView vacio;

    public static AgregarJugadorSheet nueva(long[] excluir) {
        Bundle args = new Bundle();
        args.putLongArray(ARG_EXCLUIR, excluir);
        AgregarJugadorSheet sheet = new AgregarJugadorSheet();
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_sumar_jugador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = TrucoRepository.getInstance(requireContext());
        long[] arg = requireArguments().getLongArray(ARG_EXCLUIR);
        excluir = arg == null ? new long[0] : arg;

        buscar = view.findViewById(R.id.buscar);
        resultados = view.findViewById(R.id.resultados);
        vacio = view.findViewById(R.id.vacio);

        buscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                repintar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        repo.observarJugadores().observe(getViewLifecycleOwner(), jugadores -> {
            todos = jugadores;
            cargarStats();
        });
    }

    /** Una sola pasada a la base al abrir; después se filtra en memoria en cada tecla. */
    private void cargarStats() {
        List<Player> instantanea = todos;
        repo.ejecutar(() -> {
            Map<Long, int[]> nuevas = new HashMap<>();
            for (Player p : instantanea) {
                nuevas.put(p.id, new int[]{repo.jugadas(p.id), repo.ganadas(p.id)});
            }
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                stats.clear();
                stats.putAll(nuevas);
                repintar(buscar.getText().toString());
            });
        });
    }

    private boolean estaExcluido(long id) {
        for (long e : excluir) {
            if (e == id) {
                return true;
            }
        }
        return false;
    }

    private void repintar(String texto) {
        resultados.removeAllViews();
        String query = texto.trim().toLowerCase(Locale.getDefault());

        List<Player> candidatos = new ArrayList<>();
        for (Player p : todos) {
            if (estaExcluido(p.id)) {
                continue;
            }
            if (query.isEmpty() || p.name.toLowerCase(Locale.getDefault()).contains(query)) {
                candidatos.add(p);
            }
        }

        for (Player p : candidatos) {
            resultados.addView(filaResultado(p));
        }

        if (!query.isEmpty()) {
            resultados.addView(filaCrear(texto.trim()));
        }

        vacio.setVisibility(candidatos.isEmpty() && query.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private View filaResultado(Player jugador) {
        View fila = LayoutInflater.from(requireContext())
                .inflate(R.layout.fila_resultado_jugador, resultados, false);

        ((TextView) fila.findViewById(R.id.nombre)).setText(jugador.name);

        TextView avatar = fila.findViewById(R.id.avatar);
        avatar.setText(jugador.inicial());
        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(jugador.color);
        avatar.setBackground(circulo);

        int[] s = stats.get(jugador.id);
        TextView statsView = fila.findViewById(R.id.stats);
        statsView.setText(s == null
                ? jugador.alias
                : getString(R.string.stats_jugador, s[0], s[1]));

        fila.setOnClickListener(v -> elegir(jugador.id));
        return fila;
    }

    private View filaCrear(String textoBuscado) {
        View fila = LayoutInflater.from(requireContext())
                .inflate(R.layout.fila_crear_jugador, resultados, false);
        ((TextView) fila).setText(getString(R.string.crear_a, textoBuscado));
        fila.setOnClickListener(v ->
                JugadorDialog.crear(requireContext(), null, textoBuscado, jugador ->
                        repo.crearJugador(jugador, id -> requireActivity().runOnUiThread(() -> {
                            elegir(id);
                        }))
                ).show());
        return fila;
    }

    /** Ya sea un jugador que ya existía o uno recién creado: para la mesa da lo mismo. */
    private void elegir(long playerId) {
        Fragment padre = getParentFragment();
        if (padre instanceof InicioFragment) {
            ((InicioFragment) padre).jugadorAgregado(playerId);
        }
        dismiss();
    }
}
