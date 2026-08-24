package com.frangomez.buenasymalas.ui.marcador;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.frangomez.buenasymalas.game.Reglas;

import java.util.Arrays;

/**
 * El estado de la partida en curso.
 *
 * <p>Todo vive en el {@link SavedStateHandle} y no en campos: una partida de truco dura media
 * hora sobre la mesa, con el teléfono girando de mano en mano, y si Android mata el proceso
 * mientras tanto el marcador tiene que volver como estaba.
 */
public class MatchViewModel extends ViewModel {

    private static final String K_A = "scoreA";
    private static final String K_B = "scoreB";
    private static final String K_OBJETIVO = "objetivo";
    private static final String K_CON_FLOR = "conFlor";
    private static final String K_TAM_EQUIPO = "tamEquipo";
    private static final String K_MANO = "mano";
    private static final String K_UNDO = "undo";

    /** Veinte pasos atrás alcanzan para cualquier discusión de mesa. */
    private static final int MAX_UNDO = 20;

    private final SavedStateHandle estado;

    public MatchViewModel(SavedStateHandle estado) {
        this.estado = estado;
        if (!estado.contains(K_A)) {
            estado.set(K_A, 0);
            estado.set(K_B, 0);
            estado.set(K_UNDO, new int[0]);
        }
    }

    public LiveData<Integer> puntajeA() {
        return estado.getLiveData(K_A, 0);
    }

    public LiveData<Integer> puntajeB() {
        return estado.getLiveData(K_B, 0);
    }

    public int a() {
        return valor(K_A);
    }

    public int b() {
        return valor(K_B);
    }

    private int valor(String clave) {
        Integer v = estado.get(clave);
        return v == null ? 0 : v;
    }

    public int objetivo() {
        Integer v = estado.get(K_OBJETIVO);
        return v == null ? Reglas.A_TREINTA : v;
    }

    public boolean conFlor() {
        Boolean v = estado.get(K_CON_FLOR);
        return v != null && v;
    }

    public int tamEquipo() {
        Integer v = estado.get(K_TAM_EQUIPO);
        return v == null ? 1 : v;
    }

    public long mano() {
        Long v = estado.get(K_MANO);
        return v == null ? 0L : v;
    }

    /** Configura la partida. Sólo tiene efecto la primera vez: después manda lo guardado. */
    public void configurar(int objetivo, boolean conFlor, int tamEquipo, long manoPlayerId) {
        if (estado.contains(K_OBJETIVO)) {
            return;
        }
        estado.set(K_OBJETIVO, objetivo);
        estado.set(K_CON_FLOR, conFlor);
        estado.set(K_TAM_EQUIPO, tamEquipo);
        estado.set(K_MANO, manoPlayerId);
    }

    /**
     * Suma —o resta— puntos a un lado. Un canto de cuatro puntos entra como un solo paso de
     * deshacer: en la mesa se cantó una vez, no cuatro.
     */
    public void sumar(boolean equipoA, int puntos) {
        apilar();
        String clave = equipoA ? K_A : K_B;
        estado.set(clave, Reglas.sumar(valor(clave), puntos, objetivo()));
    }

    private void apilar() {
        int[] pila = pila();
        int[] nueva;
        if (pila.length >= MAX_UNDO * 2) {
            nueva = Arrays.copyOfRange(pila, 2, pila.length + 2);
        } else {
            nueva = Arrays.copyOf(pila, pila.length + 2);
        }
        nueva[nueva.length - 2] = a();
        nueva[nueva.length - 1] = b();
        estado.set(K_UNDO, nueva);
    }

    private int[] pila() {
        int[] p = estado.get(K_UNDO);
        return p == null ? new int[0] : p;
    }

    public boolean puedeDeshacer() {
        return pila().length > 0;
    }

    public void deshacer() {
        int[] pila = pila();
        if (pila.length == 0) {
            return;
        }
        estado.set(K_A, pila[pila.length - 2]);
        estado.set(K_B, pila[pila.length - 1]);
        estado.set(K_UNDO, Arrays.copyOf(pila, pila.length - 2));
    }

    public void nueva() {
        estado.set(K_A, 0);
        estado.set(K_B, 0);
        estado.set(K_UNDO, new int[0]);
    }

    public boolean termino() {
        return a() >= objetivo() || b() >= objetivo();
    }

    /** Cierto si el que llegó al objetivo es el equipo A. Sólo tiene sentido si terminó. */
    public boolean ganoA() {
        return a() >= objetivo();
    }

    /** La partida ya avanzó lo suficiente como para que borrarla duela. */
    public boolean vaEnSerio() {
        return a() > 0 || b() > 0;
    }

    public int faltaEnvido() {
        return Reglas.faltaEnvido(a(), b(), objetivo());
    }
}
