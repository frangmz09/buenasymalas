package com.frangomez.buenasymalas.ui.inicio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.ui.WoodDrawable;

/** Armar la mesa: formato, reglas, quienes juegan y quien es mano. */
public class InicioFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int madera = ContextCompat.getColor(requireContext(), R.color.madera);
        float densidad = getResources().getDisplayMetrics().density;
        view.findViewById(R.id.raiz).setBackground(new WoodDrawable(madera, densidad));

        view.findViewById(R.id.empezar).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.a_marcador));
    }
}
