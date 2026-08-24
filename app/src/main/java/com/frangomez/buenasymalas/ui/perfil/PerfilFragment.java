package com.frangomez.buenasymalas.ui.perfil;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Match;
import com.frangomez.buenasymalas.data.Player;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.frangomez.buenasymalas.databinding.FragmentPerfilBinding;
import com.frangomez.buenasymalas.ui.Formatos;
import com.frangomez.buenasymalas.ui.WoodDrawable;
import com.frangomez.buenasymalas.ui.historial.HistorialFragment;
import com.frangomez.buenasymalas.ui.inicio.JugadorDialog;

import java.util.ArrayList;
import java.util.List;

/** El perfil de un jugador: cómo le va, contra quién, de qué color juega y su peor noche. */
public class PerfilFragment extends Fragment {

    public static final String ARG_PLAYER_ID = "playerId";

    /** Un rival ya resuelto: el récord sale de varias consultas y no puede ir en el hilo de UI. */
    private static class Rival {
        Player jugador;
        int ganadas;
        int perdidas;
    }

    private FragmentPerfilBinding binding;
    private TrucoRepository repo;
    private long playerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = TrucoRepository.getInstance(requireContext());
        playerId = requireArguments().getLong(ARG_PLAYER_ID);

        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(ContextCompat.getColor(requireContext(), R.color.tiza));
        binding.avatar.setBackground(circulo);

        binding.volver.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        repo.observarJugador(playerId).observe(getViewLifecycleOwner(), jugador -> {
            if (jugador != null) {
                pintarCabecera(jugador);
                cargarDerivados(jugador);
            }
        });
    }

    private void pintarCabecera(Player jugador) {
        binding.avatar.setText(jugador.inicial());
        binding.nombre.setText(jugador.name);
        binding.alias.setText(jugador.alias.isEmpty()
                ? "" : getString(R.string.alias_fmt, jugador.alias.toUpperCase()));

        binding.editar.setOnClickListener(v ->
                JugadorDialog.crear(requireContext(), jugador, repo::guardarJugador).show());

        armarPaleta(jugador);
        pintarPreview(jugador.color);
    }

    /** Los seis colores de mitad de cancha; el elegido lleva doble anillo. */
    private void armarPaleta(Player jugador) {
        binding.colores.removeAllViews();
        float d = getResources().getDisplayMetrics().density;

        for (int color : JugadorDialog.paleta(requireContext())) {
            View swatch = new View(requireContext());
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams((int) (38 * d), (int) (38 * d));
            lp.setMarginEnd((int) (8 * d));
            swatch.setLayoutParams(lp);

            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(color);
            fondo.setCornerRadius(10 * d);
            if (color == jugador.color) {
                fondo.setStroke((int) (3 * d),
                        ContextCompat.getColor(requireContext(), R.color.madera_oscura));
            }
            swatch.setBackground(fondo);

            swatch.setOnClickListener(v -> {
                jugador.color = color;
                repo.guardarJugador(jugador);
            });
            binding.colores.addView(swatch);
        }
    }

    /** La vista previa muestra el marcador partido en dos con su color de un lado. */
    private void pintarPreview(int color) {
        float d = getResources().getDisplayMetrics().density;
        binding.previewIzq.setBackground(new WoodDrawable(color, d));
        binding.previewDer.setBackground(new WoodDrawable(
                ContextCompat.getColor(requireContext(), R.color.pano), d));
    }

    private void cargarDerivados(Player jugador) {
        repo.ejecutar(() -> {
            int jugadas = repo.jugadas(playerId);
            int ganadas = repo.ganadas(playerId);
            List<Rival> rivales = armarRivales();
            Match peor = repo.peorDerrota(playerId);

            String peorResultado = null;
            String peorMeta = null;
            if (peor != null) {
                peorResultado = Formatos.resultado(requireContext(), peor,
                        Formatos.nombreDeEquipo(repo.integrantes(peor.teamAId)),
                        Formatos.nombreDeEquipo(repo.integrantes(peor.teamBId)));
                peorMeta = Formatos.meta(requireContext(), peor);
            }

            if (!isAdded()) {
                return;
            }
            String resultado = peorResultado;
            String meta = peorMeta;
            requireActivity().runOnUiThread(() -> {
                binding.jugadas.setText(String.valueOf(jugadas));
                binding.ganadas.setText(String.valueOf(ganadas));
                binding.perdidas.setText(String.valueOf(jugadas - ganadas));

                binding.peorResultado.setText(resultado == null
                        ? getString(R.string.sin_derrotas) : resultado);
                binding.peorMeta.setText(meta == null ? "" : meta);

                pintarRivales(rivales);
            });
        });
    }

    private List<Rival> armarRivales() {
        List<Rival> rivales = new ArrayList<>();
        for (Player otro : repo.rivalesDe(playerId)) {
            int[] record = repo.recordEntre(playerId, otro.id);
            Rival rival = new Rival();
            rival.jugador = otro;
            rival.ganadas = record[0];
            rival.perdidas = record[1];
            rivales.add(rival);
        }
        return rivales;
    }

    private void pintarRivales(List<Rival> rivales) {
        binding.rivales.removeAllViews();
        binding.sinRivales.setVisibility(rivales.isEmpty() ? View.VISIBLE : View.GONE);

        for (Rival rival : rivales) {
            View fila = LayoutInflater.from(requireContext())
                    .inflate(R.layout.fila_rival, binding.rivales, false);

            ((TextView) fila.findViewById(R.id.nombre)).setText(rival.jugador.name);
            ((TextView) fila.findViewById(R.id.record))
                    .setText(getString(R.string.record, rival.ganadas, rival.perdidas));
            pintarEtiqueta(fila.findViewById(R.id.etiqueta), rival);

            fila.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong(HistorialFragment.ARG_PLAYER_A, playerId);
                args.putLong(HistorialFragment.ARG_PLAYER_B, rival.jugador.id);
                NavHostFragment.findNavController(this).navigate(R.id.a_historial, args);
            });
            binding.rivales.addView(fila);
        }
    }

    /** "invicto" pesa más que "lo domina": si nunca perdió contra alguien, eso es lo que se dice. */
    private void pintarEtiqueta(TextView etiqueta, Rival rival) {
        int texto;
        int color;
        if (rival.perdidas == 0) {
            texto = R.string.invicto;
            color = R.color.verde;
        } else if (rival.ganadas > rival.perdidas) {
            texto = R.string.lo_domina;
            color = R.color.verde;
        } else {
            texto = R.string.lo_sufre;
            color = R.color.bordo;
        }
        etiqueta.setText(texto);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(ContextCompat.getColor(requireContext(), color));
        fondo.setCornerRadius(20 * getResources().getDisplayMetrics().density);
        etiqueta.setBackground(fondo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
