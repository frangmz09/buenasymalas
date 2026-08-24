package com.frangomez.buenasymalas.ui.inicio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Player;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.frangomez.buenasymalas.databinding.FragmentInicioBinding;
import com.frangomez.buenasymalas.game.Reglas;
import com.frangomez.buenasymalas.ui.WoodDrawable;
import com.frangomez.buenasymalas.ui.marcador.MarcadorFragment;
import com.frangomez.buenasymalas.ui.perfil.PerfilFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Armar la mesa: formato, reglas, quiénes juegan y quién es mano.
 *
 * <p>Los equipos salen del orden de la lista: los primeros {@code tamEquipo} son un lado y los
 * siguientes el otro. Es lo que se hace en la mesa —"vos y yo contra ellos dos"— y ahorra una
 * pantalla de armado de equipos que nadie pidió.
 */
public class InicioFragment extends Fragment implements JugadoresAdapter.Escuchas {

    private FragmentInicioBinding binding;
    private TrucoRepository repo;
    private JugadoresAdapter adapter;

    private int tamEquipo = 1;
    private boolean conFlor = false;
    private int objetivo = Reglas.A_TREINTA;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = TrucoRepository.getInstance(requireContext());

        int madera = ContextCompat.getColor(requireContext(), R.color.madera);
        binding.raiz.setBackground(
                new WoodDrawable(madera, getResources().getDisplayMetrics().density));

        adapter = new JugadoresAdapter(this);
        binding.jugadores.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.jugadores.setAdapter(adapter);

        binding.formato1v1.setOnClickListener(v -> setFormato(1));
        binding.formato2v2.setOnClickListener(v -> setFormato(2));
        binding.formato3v3.setOnClickListener(v -> setFormato(3));

        binding.conFlor.setOnClickListener(v -> setFlor(true));
        binding.sinFlor.setOnClickListener(v -> setFlor(false));
        binding.a15.setOnClickListener(v -> setObjetivo(Reglas.A_QUINCE));
        binding.a30.setOnClickListener(v -> setObjetivo(Reglas.A_TREINTA));

        binding.sumarJugador.setOnClickListener(v ->
                JugadorDialog.crear(requireContext(), null, this::guardar).show());
        binding.empezar.setOnClickListener(v -> empezar());
        binding.irAlMuseo.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.a_galeria));

        setFormato(tamEquipo);
        setFlor(conFlor);
        setObjetivo(objetivo);

        repo.observarJugadores().observe(getViewLifecycleOwner(), jugadores -> {
            adapter.setJugadores(jugadores);
            if (adapter.getMano() == 0 && !jugadores.isEmpty()) {
                adapter.setMano(jugadores.get(0).id);
            }
            cargarStats(jugadores);
        });
    }

    private void guardar(Player jugador) {
        repo.guardarJugador(jugador);
    }

    // --- Reglas de la partida ----------------------------------------------------------------

    private void setFormato(int nuevo) {
        tamEquipo = nuevo;
        binding.formato1v1.setSelected(nuevo == 1);
        binding.formato2v2.setSelected(nuevo == 2);
        binding.formato3v3.setSelected(nuevo == 3);
    }

    private void setFlor(boolean nuevo) {
        conFlor = nuevo;
        binding.conFlor.setSelected(nuevo);
        binding.sinFlor.setSelected(!nuevo);
    }

    private void setObjetivo(int nuevo) {
        objetivo = nuevo;
        binding.a15.setSelected(nuevo == Reglas.A_QUINCE);
        binding.a30.setSelected(nuevo == Reglas.A_TREINTA);
    }

    // --- Jugadores ---------------------------------------------------------------------------

    @Override
    public void alTocarMano(Player jugador) {
        adapter.setMano(jugador.id);
    }

    /**
     * La fila lleva al perfil, no al diálogo de edición: desde el perfil se ve el récord contra
     * cada rival, que es lo que uno quiere mirar antes de empezar. Editar está adentro.
     */
    @Override
    public void alTocarJugador(Player jugador) {
        Bundle args = new Bundle();
        args.putLong(PerfilFragment.ARG_PLAYER_ID, jugador.id);
        NavHostFragment.findNavController(this).navigate(R.id.a_perfil, args);
    }

    /** Las stats y la chicana salen de la base, así que se arman fuera del hilo principal. */
    private void cargarStats(List<Player> jugadores) {
        repo.ejecutar(() -> {
            Map<Long, int[]> stats = new HashMap<>();
            for (Player p : jugadores) {
                stats.put(p.id, new int[]{repo.jugadas(p.id), repo.ganadas(p.id)});
            }
            String chicana = chicana(jugadores);

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                adapter.setStats(stats);
                binding.chicana.setText(chicana);
            });
        });
    }

    /**
     * La línea del pie. Sale del historial: la peor racha manda, y si nadie viene mal, el que
     * viene ganando. Sin partidas todavía, una promesa.
     */
    private String chicana(List<Player> jugadores) {
        Player peor = null;
        int peorRacha = 0;
        Player mejor = null;
        int mejorRacha = 0;

        for (Player p : jugadores) {
            int racha = repo.racha(p.id);
            if (racha <= -2 && racha < peorRacha) {
                peorRacha = racha;
                peor = p;
            }
            if (racha >= 2 && racha > mejorRacha) {
                mejorRacha = racha;
                mejor = p;
            }
        }
        if (peor != null) {
            return getString(R.string.chicana_racha, peor.name, -peorRacha);
        }
        if (mejor != null) {
            return getString(R.string.chicana_invicto, mejor.name, mejorRacha);
        }
        return getString(R.string.chicana_primera);
    }

    // --- Arranque ----------------------------------------------------------------------------

    /**
     * Se puede empezar sin cargar a nadie: el marcador queda como "Nosotros" y "Ellos", que es
     * lo que uno quiere la primera vez que abre la app en la mesa. Lo que no se puede es
     * empezar con gente cargada pero incompleta para el formato elegido.
     */
    private void empezar() {
        List<Player> mesa = adapter.getJugadores();
        int necesarios = tamEquipo * 2;

        if (!mesa.isEmpty() && mesa.size() < necesarios) {
            Toast.makeText(requireContext(),
                    getString(R.string.faltan_jugadores, formatoLegible()),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        if (!mesa.isEmpty()) {
            args.putLongArray(MarcadorFragment.ARG_JUGADORES_A, ids(mesa, 0, tamEquipo));
            args.putLongArray(MarcadorFragment.ARG_JUGADORES_B, ids(mesa, tamEquipo, necesarios));
        }
        args.putInt(MarcadorFragment.ARG_OBJETIVO, objetivo);
        args.putBoolean(MarcadorFragment.ARG_CON_FLOR, conFlor);
        args.putInt(MarcadorFragment.ARG_TAM_EQUIPO, tamEquipo);
        args.putLong(MarcadorFragment.ARG_MANO, adapter.getMano());

        NavHostFragment.findNavController(this).navigate(R.id.a_marcador, args);
    }

    private long[] ids(List<Player> mesa, int desde, int hasta) {
        long[] ids = new long[hasta - desde];
        for (int i = desde; i < hasta; i++) {
            ids[i - desde] = mesa.get(i).id;
        }
        return ids;
    }

    private String formatoLegible() {
        TextView chip = tamEquipo == 1 ? binding.formato1v1
                : tamEquipo == 2 ? binding.formato2v2 : binding.formato3v3;
        return chip.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
