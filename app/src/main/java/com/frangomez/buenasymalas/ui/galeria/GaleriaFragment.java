package com.frangomez.buenasymalas.ui.galeria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.PhotoDao;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.frangomez.buenasymalas.databinding.FragmentGaleriaBinding;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * El museo de las chicanas.
 *
 * <p>El sello —quién perdió, con qué marcador y cuándo— se dibuja acá, en el pie de la tarjeta,
 * y no en los píxeles de la foto: el apodo se edita en el perfil y tiene que cambiar en todas
 * sus fotos de una vez.
 */
public class GaleriaFragment extends Fragment {

    private static final String TODAS = "";

    private FragmentGaleriaBinding binding;
    private final MuseoAdapter adapter = new MuseoAdapter();

    private List<PhotoDao.FotoDeMuseo> todas = new ArrayList<>();
    private String filtroJugador = TODAS;
    private boolean soloPalizas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGaleriaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TrucoRepository repo = TrucoRepository.getInstance(requireContext());

        binding.fotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.fotos.setAdapter(adapter);

        binding.volver.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        repo.derrotasDocumentadas().observe(getViewLifecycleOwner(), cuantas ->
                binding.contador.setText(
                        getResources().getQuantityString(R.plurals.derrotas, cuantas, cuantas)));

        repo.observarMuseo().observe(getViewLifecycleOwner(), fotos -> {
            todas = fotos;
            armarFiltros();
            aplicarFiltros();
        });
    }

    /** Los filtros salen de quiénes hay en el museo: no tiene sentido filtrar por alguien que nunca perdió. */
    private void armarFiltros() {
        binding.filtros.removeAllViews();
        binding.filtros.addView(chip(getString(R.string.filtro_todas),
                filtroJugador.equals(TODAS) && !soloPalizas, () -> {
                    filtroJugador = TODAS;
                    soloPalizas = false;
                }));

        Set<String> nombres = new LinkedHashSet<>();
        for (PhotoDao.FotoDeMuseo f : todas) {
            nombres.add(f.perdedor);
        }
        for (String nombre : nombres) {
            binding.filtros.addView(chip(nombre, nombre.equals(filtroJugador), () -> {
                filtroJugador = nombre;
                soloPalizas = false;
            }));
        }

        binding.filtros.addView(chip(getString(R.string.filtro_palizas), soloPalizas, () -> {
            filtroJugador = TODAS;
            soloPalizas = true;
        }));
    }

    private TextView chip(String texto, boolean activo, Runnable alTocar) {
        TextView v = (TextView) LayoutInflater.from(requireContext())
                .inflate(R.layout.chip_filtro, binding.filtros, false);
        v.setText(texto);
        v.setSelected(activo);
        v.setOnClickListener(x -> {
            alTocar.run();
            armarFiltros();
            aplicarFiltros();
        });
        return v;
    }

    private void aplicarFiltros() {
        List<PhotoDao.FotoDeMuseo> visibles = new ArrayList<>();
        for (PhotoDao.FotoDeMuseo f : todas) {
            if (soloPalizas && !f.match.esPaliza()) {
                continue;
            }
            if (!filtroJugador.equals(TODAS) && !filtroJugador.equals(f.perdedor)) {
                continue;
            }
            visibles.add(f);
        }
        adapter.setFotos(visibles);
        binding.vacio.setVisibility(visibles.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
