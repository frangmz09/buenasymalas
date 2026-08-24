package com.frangomez.buenasymalas.ui.inicio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;

/**
 * Quiénes están en la mesa para la próxima partida.
 *
 * <p>Vive en el {@link SavedStateHandle} y no en un campo: si volvés de una partida a Inicio,
 * la mesa tiene que seguir armada para la revancha sin tener que volver a buscar a todos, y eso
 * incluye sobrevivir a que el sistema mate el proceso mientras el teléfono estaba en el bolsillo.
 */
public class InicioViewModel extends ViewModel {

    private static final String K_MESA = "mesaIds";

    private final SavedStateHandle estado;

    public InicioViewModel(SavedStateHandle estado) {
        this.estado = estado;
        if (!estado.contains(K_MESA)) {
            estado.set(K_MESA, new long[0]);
        }
    }

    public LiveData<long[]> mesaIds() {
        return estado.getLiveData(K_MESA, new long[0]);
    }

    public long[] mesaIdsActuales() {
        long[] ids = estado.get(K_MESA);
        return ids == null ? new long[0] : ids;
    }

    public boolean contiene(long id) {
        for (long existente : mesaIdsActuales()) {
            if (existente == id) {
                return true;
            }
        }
        return false;
    }

    public void agregar(long id) {
        if (contiene(id)) {
            return;
        }
        long[] actuales = mesaIdsActuales();
        long[] nuevos = Arrays.copyOf(actuales, actuales.length + 1);
        nuevos[actuales.length] = id;
        estado.set(K_MESA, nuevos);
    }

    public void quitar(long id) {
        long[] actuales = mesaIdsActuales();
        int posicion = -1;
        for (int i = 0; i < actuales.length; i++) {
            if (actuales[i] == id) {
                posicion = i;
                break;
            }
        }
        if (posicion < 0) {
            return;
        }
        long[] nuevos = new long[actuales.length - 1];
        System.arraycopy(actuales, 0, nuevos, 0, posicion);
        System.arraycopy(actuales, posicion + 1, nuevos, posicion, actuales.length - posicion - 1);
        estado.set(K_MESA, nuevos);
    }
}
