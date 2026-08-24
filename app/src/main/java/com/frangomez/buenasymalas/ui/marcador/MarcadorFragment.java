package com.frangomez.buenasymalas.ui.marcador;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Player;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.frangomez.buenasymalas.databinding.FragmentMarcadorBinding;
import com.frangomez.buenasymalas.databinding.PanelEquipoBinding;
import com.frangomez.buenasymalas.game.Reglas;
import com.frangomez.buenasymalas.ui.WoodDrawable;

import java.util.ArrayList;
import java.util.List;

/** El marcador: dos mitades de cancha y un tap por punto. */
public class MarcadorFragment extends Fragment {

    public static final String ARG_JUGADORES_A = "jugadoresA";
    public static final String ARG_JUGADORES_B = "jugadoresB";
    public static final String ARG_OBJETIVO = "objetivo";
    public static final String ARG_CON_FLOR = "conFlor";
    public static final String ARG_TAM_EQUIPO = "tamEquipo";
    public static final String ARG_MANO = "mano";

    private FragmentMarcadorBinding binding;
    private MatchViewModel vm;
    private TrucoRepository repo;

    private String nombreA = "";
    private String nombreB = "";
    private int colorA;
    private int colorB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMarcadorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(MatchViewModel.class);
        repo = TrucoRepository.getInstance(requireContext());

        Bundle args = requireArguments();
        vm.configurar(
                args.getInt(ARG_OBJETIVO, Reglas.A_TREINTA),
                args.getBoolean(ARG_CON_FLOR, false),
                args.getInt(ARG_TAM_EQUIPO, 1),
                args.getLong(ARG_MANO, 0L));

        cablearPanel(binding.panelA, true);
        cablearPanel(binding.panelB, false);

        binding.deshacer.setOnClickListener(v -> {
            vm.deshacer();
            refrescar();
        });
        binding.nueva.setOnClickListener(v -> confirmarNueva());
        binding.cantos.setOnClickListener(v -> abrirCantos());

        binding.panelA.marcador.setObjetivo(vm.objetivo());
        binding.panelB.marcador.setObjetivo(vm.objetivo());

        cargarEquipos(args.getLongArray(ARG_JUGADORES_A), args.getLongArray(ARG_JUGADORES_B));

        vm.puntajeA().observe(getViewLifecycleOwner(), p -> refrescar());
        vm.puntajeB().observe(getViewLifecycleOwner(), p -> refrescar());
    }

    /**
     * Un tap en cualquier parte de la mitad suma un punto. Los botones no propagan el suyo: si
     * lo hicieran, tocar el + sumaría dos.
     */
    private void cablearPanel(PanelEquipoBinding panel, boolean equipoA) {
        panel.panel.setOnClickListener(v -> sumar(equipoA, 1));
        panel.sumar.setOnClickListener(v -> sumar(equipoA, 1));
        panel.restar.setOnClickListener(v -> sumar(equipoA, -1));
    }

    private void sumar(boolean equipoA, int puntos) {
        if (vm.termino() && puntos > 0) {
            return;
        }
        vm.sumar(equipoA, puntos);
        refrescar();
    }

    /**
     * Los nombres y colores salen de la base, así que llegan tarde: hasta entonces los paneles
     * quedan con el color de mesa por defecto.
     */
    private void cargarEquipos(@Nullable long[] idsA, @Nullable long[] idsB) {
        repo.ejecutar(() -> {
            List<Player> a = jugadores(idsA);
            List<Player> b = jugadores(idsB);
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                nombreA = nombreDe(a, R.string.nosotros);
                nombreB = nombreDe(b, R.string.ellos);
                colorA = pintar(binding.panelA, a, R.color.madera, nombreA);
                colorB = pintar(binding.panelB, b, R.color.pano, nombreB);
                refrescar();
            });
        });
    }

    private List<Player> jugadores(@Nullable long[] ids) {
        List<Player> lista = new ArrayList<>();
        if (ids == null) {
            return lista;
        }
        for (long id : ids) {
            Player p = repo.jugador(id);
            if (p != null) {
                lista.add(p);
            }
        }
        return lista;
    }

    /**
     * En 1v1 el equipo se llama como el jugador; en 2v2 y 3v3, como sus integrantes. Si nadie
     * se cargó, quedan "Nosotros" y "Ellos": se puede contar una partida sin dar de alta a
     * nadie, que es lo que uno quiere la primera vez que abre la app en la mesa.
     */
    private String nombreDe(List<Player> equipo, int siNoHayNadie) {
        if (equipo.isEmpty()) {
            return getString(siNoHayNadie);
        }
        StringBuilder sb = new StringBuilder();
        for (Player p : equipo) {
            if (sb.length() > 0) {
                sb.append(" y ");
            }
            sb.append(p.name);
        }
        return sb.toString();
    }

    /** Devuelve el color con el que quedó pintada esa mitad: lo reusa la hoja de cantos. */
    private int pintar(PanelEquipoBinding panel, List<Player> equipo, int colorPorDefecto,
                       String nombre) {
        int color = equipo.isEmpty()
                ? ContextCompat.getColor(requireContext(), colorPorDefecto)
                : equipo.get(0).color;
        float densidad = getResources().getDisplayMetrics().density;
        panel.panel.setBackground(new WoodDrawable(color, densidad));
        panel.marcador.setColorTiza(ContextCompat.getColor(requireContext(), R.color.tiza));
        panel.nombre.setText(nombre);
        return color;
    }

    private void refrescar() {
        int a = vm.a();
        int b = vm.b();

        binding.panelA.puntaje.setText(String.valueOf(a));
        binding.panelB.puntaje.setText(String.valueOf(b));
        binding.panelA.marcador.setPuntaje(a);
        binding.panelB.marcador.setPuntaje(b);
        binding.panelA.zona.setText(zona(a));
        binding.panelB.zona.setText(zona(b));
        binding.deshacer.setEnabled(vm.puedeDeshacer());
        binding.deshacer.setAlpha(vm.puedeDeshacer() ? 1f : 0.4f);
        binding.estado.setText(lineaDeEstado(a, b));
    }

    private String zona(int puntaje) {
        return getString(Reglas.estaEnBuenas(puntaje) ? R.string.buenas : R.string.malas);
    }

    private String lineaDeEstado(int a, int b) {
        if (vm.termino()) {
            return getString(R.string.estado_terminada);
        }
        if (a == b) {
            return getString(R.string.estado_parejos);
        }
        String arriba = a > b ? nombreA : nombreB;
        return getString(R.string.estado_arriba, arriba, Math.abs(a - b));
    }

    private void confirmarNueva() {
        if (!vm.vaEnSerio()) {
            vm.nueva();
            refrescar();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.nueva_confirmar_titulo)
                .setMessage(R.string.nueva_confirmar_cuerpo)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.empezar_de_nuevo, (d, w) -> {
                    vm.nueva();
                    refrescar();
                })
                .show();
    }

    private void abrirCantos() {
        CantosBottomSheet.nueva(vm.conFlor(), vm.faltaEnvido(), nombreA, nombreB, colorA, colorB)
                .show(getChildFragmentManager(), "cantos");
    }

    /** Un canto suma sus puntos como un solo paso de deshacer. */
    void sumarCanto(boolean equipoA, int puntos) {
        sumar(equipoA, puntos);
    }

    @Override
    public void onResume() {
        super.onResume();
        // La partida dura media hora sobre la mesa: la pantalla no se apaga en el medio.
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
