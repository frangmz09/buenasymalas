package com.frangomez.buenasymalas.ui.foto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.Photo;
import com.frangomez.buenasymalas.data.TrucoRepository;
import com.frangomez.buenasymalas.databinding.FragmentFotoBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * El final de la partida: el resultado, el apodo del perdedor y la foto.
 *
 * <p>La foto se guarda cruda en el almacenamiento interno de la app. El marcador, la fecha y el
 * apodo se dibujan encima recién al mostrarla en el museo, así que si mañana cambia el apodo,
 * cambia en todas sus fotos. Guardarla ya sellada dejaría el apodo congelado en los píxeles.
 */
public class FotoFragment extends Fragment {

    public static final String ARG_MATCH_ID = "matchId";
    public static final String ARG_NOMBRE_GANADOR = "nombreGanador";
    public static final String ARG_NOMBRE_PERDEDOR = "nombrePerdedor";
    public static final String ARG_PUNTAJE_GANADOR = "puntajeGanador";
    public static final String ARG_PUNTAJE_PERDEDOR = "puntajePerdedor";
    public static final String ARG_PERDEDOR_ID = "perdedorId";
    public static final String ARG_ALIAS = "alias";

    private FragmentFotoBinding binding;
    private TrucoRepository repo;

    @Nullable
    private ImageCapture captura;
    private boolean frontal = true;

    private final ActivityResultLauncher<String> pedirCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), dado -> {
                if (dado) {
                    arrancarCamara();
                } else {
                    sinCamara();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = TrucoRepository.getInstance(requireContext());
        Bundle args = requireArguments();

        binding.resultado.setText(getString(R.string.resultado_final,
                args.getString(ARG_NOMBRE_GANADOR, ""),
                args.getInt(ARG_PUNTAJE_GANADOR),
                args.getInt(ARG_PUNTAJE_PERDEDOR),
                args.getString(ARG_NOMBRE_PERDEDOR, "")));

        String perdedor = args.getString(ARG_NOMBRE_PERDEDOR, "");
        String alias = args.getString(ARG_ALIAS, "");
        binding.apodo.setText(alias.isEmpty()
                ? getString(R.string.sin_apodo, perdedor)
                : getString(R.string.apodo_chip, perdedor, alias));

        binding.saltear.setOnClickListener(v -> volverAlInicio());
        binding.galeria.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.de_foto_a_galeria));
        binding.sellar.setOnClickListener(v -> sacarFoto());
        binding.obturador.setOnClickListener(v -> sacarFoto());
        binding.girar.setOnClickListener(v -> {
            frontal = !frontal;
            binding.etiquetaCamara.setText(frontal
                    ? R.string.camara_frontal : R.string.camara_trasera);
            arrancarCamara();
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            arrancarCamara();
        } else {
            pedirCamara.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * La cámara arranca de frente: la foto es del que perdió, sentado del otro lado. Si el
     * equipo no tiene frontal se usa la que haya, porque quedarse sin foto por eso sería peor
     * que sacarla del otro lado.
     */
    private void arrancarCamara() {
        ListenableFuture<ProcessCameraProvider> futuro =
                ProcessCameraProvider.getInstance(requireContext());
        futuro.addListener(() -> {
            if (binding == null) {
                return;
            }
            try {
                ProcessCameraProvider proveedor = futuro.get();
                CameraSelector selector = elegirCamara(proveedor);
                if (selector == null) {
                    sinCamara();
                    return;
                }

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.preview.getSurfaceProvider());
                // La Activity queda fija en portrait (ver AndroidManifest), pero sin fijar acá
                // la rotación de salida, CameraX la toma del Display en el momento del build —
                // en algunos equipos (confirmado en un Samsung real) sale desfasada y la foto
                // de la cámara frontal queda girada 90°. Con la orientación siempre fija, no
                // hace falta consultar el Display: alcanza con pedir 0.
                captura = new ImageCapture.Builder()
                        .setTargetRotation(Surface.ROTATION_0)
                        .build();

                proveedor.unbindAll();
                proveedor.bindToLifecycle(getViewLifecycleOwner(), selector, preview, captura);
            } catch (ExecutionException | InterruptedException | IllegalStateException
                     | IllegalArgumentException | CameraInfoUnavailableException e) {
                sinCamara();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * La preferida si existe, la otra si no, y null si el equipo no tiene ninguna. Hay que
     * preguntar antes de pedirla: {@code bindToLifecycle} con una cámara que no existe tira
     * IllegalArgumentException y se lleva puesta la app.
     */
    @Nullable
    private CameraSelector elegirCamara(ProcessCameraProvider proveedor)
            throws CameraInfoUnavailableException {
        CameraSelector preferida = frontal
                ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
        CameraSelector alternativa = frontal
                ? CameraSelector.DEFAULT_BACK_CAMERA : CameraSelector.DEFAULT_FRONT_CAMERA;

        if (proveedor.hasCamera(preferida)) {
            return preferida;
        }
        if (proveedor.hasCamera(alternativa)) {
            frontal = !frontal;
            binding.etiquetaCamara.setText(
                    frontal ? R.string.camara_frontal : R.string.camara_trasera);
            return alternativa;
        }
        return null;
    }

    /** Sin cámara la partida igual quedó guardada: sólo se pierde la chicana. */
    private void sinCamara() {
        captura = null;
        binding.preview.setVisibility(View.GONE);
        binding.sinCamara.setVisibility(View.VISIBLE);
        binding.obturador.setEnabled(false);
        binding.obturador.setAlpha(0.4f);
        binding.sellar.setEnabled(false);
        binding.sellar.setAlpha(0.4f);
    }

    private void sacarFoto() {
        if (captura == null) {
            return;
        }
        Bundle args = requireArguments();
        long matchId = args.getLong(ARG_MATCH_ID);

        File carpeta = new File(requireContext().getFilesDir(), "fotos");
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            Toast.makeText(requireContext(), R.string.foto_fallo, Toast.LENGTH_SHORT).show();
            return;
        }
        File archivo = new File(carpeta, matchId + ".jpg");

        ImageCapture.OutputFileOptions opciones =
                new ImageCapture.OutputFileOptions.Builder(archivo).build();

        captura.takePicture(opciones, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults resultado) {
                        Photo foto = new Photo();
                        foto.matchId = matchId;
                        foto.filePath = archivo.getAbsolutePath();
                        foto.caption = args.getString(ARG_ALIAS, "");
                        foto.loserPlayerId = args.getLong(ARG_PERDEDOR_ID);
                        repo.guardarFoto(foto);

                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(requireContext(), R.string.foto_guardada,
                                Toast.LENGTH_SHORT).show();
                        volverAlInicio();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), R.string.foto_fallo,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void volverAlInicio() {
        NavHostFragment.findNavController(this).popBackStack(R.id.inicioFragment, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
