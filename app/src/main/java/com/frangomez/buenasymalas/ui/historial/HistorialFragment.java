package com.frangomez.buenasymalas.ui.historial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Match;
import com.frangomez.buenasymalas.data.Photo;
import com.frangomez.buenasymalas.data.Player;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.frangomez.buenasymalas.databinding.FragmentHistorialBinding;
import com.frangomez.buenasymalas.ui.Formatos;
import com.frangomez.buenasymalas.ui.WoodDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * El cabeza a cabeza entre dos jugadores.
 *
 * <p>Cuenta todas las partidas donde estuvieron en lados opuestos, sea 1v1 o 2v2: el que te
 * gana con compañero también te está ganando.
 */
public class HistorialFragment extends Fragment {

    public static final String ARG_PLAYER_A = "playerA";
    public static final String ARG_PLAYER_B = "playerB";

    private FragmentHistorialBinding binding;
    private TrucoRepository repo;
    private final PartidasAdapter adapter = new PartidasAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = TrucoRepository.getInstance(requireContext());

        binding.cabecera.setBackground(new WoodDrawable(
                ContextCompat.getColor(requireContext(), R.color.madera),
                getResources().getDisplayMetrics().density));

        binding.volver.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.partidas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.partidas.setAdapter(adapter);

        long idA = requireArguments().getLong(ARG_PLAYER_A);
        long idB = requireArguments().getLong(ARG_PLAYER_B);
        cargar(idA, idB);
    }

    private void cargar(long idA, long idB) {
        repo.ejecutar(() -> {
            Player a = repo.jugador(idA);
            Player b = repo.jugador(idB);
            if (a == null || b == null) {
                return;
            }

            int[] record = repo.recordEntre(idA, idB);
            int rachaA = repo.racha(idA);
            List<PartidasAdapter.Item> items = armarItems(idA, idB);

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> pintar(a, b, record, rachaA, items));
        });
    }

    private List<PartidasAdapter.Item> armarItems(long idA, long idB) {
        List<PartidasAdapter.Item> items = new ArrayList<>();
        for (Match m : repo.cruces(idA, idB)) {
            PartidasAdapter.Item item = new PartidasAdapter.Item();

            String nombreA = Formatos.nombreDeEquipo(repo.integrantes(m.teamAId));
            String nombreB = Formatos.nombreDeEquipo(repo.integrantes(m.teamBId));
            item.resultado = Formatos.resultado(requireContext(), m, nombreA, nombreB);
            item.meta = Formatos.meta(requireContext(), m);

            // Bordó si ganó el de la izquierda del título, verde si ganó el otro.
            Long equipoDeA = repo.equipoEn(idA, m.id);
            boolean ganoA = equipoDeA != null && equipoDeA == m.winnerTeamId;
            item.colorBarra = ContextCompat.getColor(requireContext(),
                    ganoA ? R.color.bordo : R.color.verde);

            Photo foto = repo.fotoDe(m.id);
            item.fotoPath = foto == null ? null : foto.filePath;

            items.add(item);
        }
        return items;
    }

    private void pintar(Player a, Player b, int[] record, int rachaA,
                        List<PartidasAdapter.Item> items) {
        binding.titulo.setText(getString(R.string.versus, a.name, b.name));
        binding.totalA.setText(String.valueOf(record[0]));
        binding.totalB.setText(String.valueOf(record[1]));

        // El que va perdiendo se apaga: se lee quién domina sin leer los números.
        binding.totalA.setAlpha(record[0] >= record[1] ? 1f : 0.55f);
        binding.totalB.setAlpha(record[1] >= record[0] ? 1f : 0.55f);

        binding.racha.setText(rachaA == 0
                ? getString(R.string.sin_racha)
                : getString(R.string.racha_valor,
                        rachaA > 0 ? a.name : b.name, Math.abs(rachaA)));

        binding.proporcion.setRecord(record[0], record[1]);

        adapter.setItems(items);
        binding.vacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
