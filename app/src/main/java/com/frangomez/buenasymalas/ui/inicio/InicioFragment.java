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
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Player;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.frangomez.buenasymalas.databinding.FragmentInicioBinding;
import com.frangomez.buenasymalas.game.Reglas;
import com.frangomez.buenasymalas.ui.WoodDrawable;
import com.frangomez.buenasymalas.ui.marcador.MarcadorFragment;
import com.frangomez.buenasymalas.ui.perfil.PerfilFragment;

import java.util.ArrayList;
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
    private InicioViewModel vm;
    private JugadoresAdapter adapter;

    private int tamEquipo = 1;
    private boolean conFlor = false;
    private int objetivo = Reglas.A_TREINTA;

    /** Última lista completa de jugadores conocida, para resolver ids de la mesa a Player. */
    private List<Player> todosLosJugadores = new ArrayList<>();

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
        vm = new ViewModelProvider(this).get(InicioViewModel.class);

        int madera = ContextCompat.getColor(requireContext(), R.color.madera);
        binding.raiz.setBackground(
                new WoodDrawable(madera, getResources().getDisplayMetrics().density));

        adapter = new JugadoresAdapter(this);
        binding.jugadores.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.jugadores.setAdapter(adapter);
        cablearSwipeParaSacar();

        binding.formato1v1.setOnClickListener(v -> setFormato(1));
        binding.formato2v2.setOnClickListener(v -> setFormato(2));
        binding.formato3v3.setOnClickListener(v -> setFormato(3));

        binding.conFlor.setOnClickListener(v -> setFlor(true));
        binding.sinFlor.setOnClickListener(v -> setFlor(false));
        binding.a15.setOnClickListener(v -> setObjetivo(Reglas.A_QUINCE));
        binding.a30.setOnClickListener(v -> setObjetivo(Reglas.A_TREINTA));

        binding.sumarJugador.setOnClickListener(v ->
                AgregarJugadorSheet.nueva(vm.mesaIdsActuales())
                        .show(getChildFragmentManager(), "sumar"));
        binding.empezar.setOnClickListener(v -> empezar());
        binding.irAlMuseo.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.a_galeria));

        setFormato(tamEquipo);
        setFlor(conFlor);
        setObjetivo(objetivo);

        // "EN LA MESA" es la selección de esta partida, no toda la tabla: se combinan las dos
        // fuentes (todos los jugadores, y quiénes están en la mesa) y se recalcula el
        // subconjunto cada vez que cualquiera de las dos cambie.
        MediatorLiveData<List<Player>> mesa = new MediatorLiveData<>();
        mesa.addSource(repo.observarJugadores(), todos -> {
            todosLosJugadores = todos;
            mesa.setValue(resolverMesa(vm.mesaIdsActuales()));
        });
        mesa.addSource(vm.mesaIds(), ids -> mesa.setValue(resolverMesa(ids)));

        mesa.observe(getViewLifecycleOwner(), jugadores -> {
            adapter.setJugadores(jugadores);
            boolean manoPresente = false;
            for (Player p : jugadores) {
                if (p.id == adapter.getMano()) {
                    manoPresente = true;
                    break;
                }
            }
            if (!manoPresente) {
                adapter.setMano(jugadores.isEmpty() ? 0 : jugadores.get(0).id);
            }
            cargarStats(jugadores);
        });
    }

    /** Resuelve los ids de la mesa a `Player`, en el mismo orden en que se fueron agregando. */
    private List<Player> resolverMesa(long[] ids) {
        List<Player> resultado = new ArrayList<>();
        for (long id : ids) {
            for (Player p : todosLosJugadores) {
                if (p.id == id) {
                    resultado.add(p);
                    break;
                }
            }
        }
        return resultado;
    }

    /** Deslizar una fila la saca de la mesa (no de la base: puede volver a buscarse). */
    private void cablearSwipeParaSacar() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int posicion = viewHolder.getBindingAdapterPosition();
                if (posicion == RecyclerView.NO_POSITION) {
                    return;
                }
                vm.quitar(adapter.getJugadores().get(posicion).id);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.jugadores);
    }

    /** Llamado por {@link AgregarJugadorSheet} al elegir a alguien, exista o se acabe de crear. */
    void jugadorAgregado(long id) {
        vm.agregar(id);
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
