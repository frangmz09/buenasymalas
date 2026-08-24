package com.frangomez.buenasymalas.ui.marcador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.SavedStateHandle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MatchViewModelTest {

    @Rule
    public InstantTaskExecutorRule reglaDeHilo = new InstantTaskExecutorRule();

    private MatchViewModel vm;

    @Before
    public void armarPartida() {
        vm = new MatchViewModel(new SavedStateHandle());
        vm.configurar(30, true, 1, 0L);
    }

    @Test
    public void arranca_cero_a_cero() {
        assertEquals(0, vm.a());
        assertEquals(0, vm.b());
        assertFalse(vm.puedeDeshacer());
    }

    @Test
    public void sumar_de_a_uno() {
        vm.sumar(true, 1);
        vm.sumar(true, 1);
        vm.sumar(false, 1);
        assertEquals(2, vm.a());
        assertEquals(1, vm.b());
    }

    @Test
    public void no_baja_de_cero() {
        vm.sumar(true, -1);
        assertEquals(0, vm.a());
    }

    @Test
    public void no_pasa_del_objetivo() {
        vm.sumar(true, 28);
        vm.sumar(true, 6);
        assertEquals(30, vm.a());
        assertTrue(vm.termino());
        assertTrue(vm.ganoA());
    }

    @Test
    public void un_canto_es_un_solo_paso_de_deshacer() {
        vm.sumar(true, 1);
        vm.sumar(true, 4); // vale cuatro
        assertEquals(5, vm.a());

        vm.deshacer();
        assertEquals(1, vm.a());

        vm.deshacer();
        assertEquals(0, vm.a());
        assertFalse(vm.puedeDeshacer());
    }

    @Test
    public void deshacer_restaura_los_dos_lados() {
        vm.sumar(true, 3);
        vm.sumar(false, 2);
        vm.deshacer();
        assertEquals(3, vm.a());
        assertEquals(0, vm.b());
    }

    @Test
    public void deshacer_sin_nada_apilado_no_rompe() {
        vm.deshacer();
        assertEquals(0, vm.a());
    }

    @Test
    public void la_pila_se_corta_en_veinte() {
        for (int i = 0; i < 25; i++) {
            vm.sumar(true, 1);
        }
        assertEquals(25, vm.a());

        for (int i = 0; i < 20; i++) {
            vm.deshacer();
        }
        // Se perdieron los cinco pasos mas viejos: no se vuelve a cero.
        assertEquals(5, vm.a());
        assertFalse(vm.puedeDeshacer());
    }

    @Test
    public void nueva_limpia_marcador_y_pila() {
        vm.sumar(true, 7);
        vm.nueva();
        assertEquals(0, vm.a());
        assertFalse(vm.puedeDeshacer());
    }

    @Test
    public void el_estado_sobrevive_a_la_muerte_del_proceso() {
        SavedStateHandle handle = new SavedStateHandle();
        MatchViewModel antes = new MatchViewModel(handle);
        antes.configurar(15, false, 2, 3L);
        antes.sumar(true, 6);
        antes.sumar(false, 2);

        // El mismo handle es lo que Android devuelve al recrear el ViewModel.
        MatchViewModel despues = new MatchViewModel(handle);
        assertEquals(6, despues.a());
        assertEquals(2, despues.b());
        assertEquals(15, despues.objetivo());
        assertEquals(2, despues.tamEquipo());
        assertEquals(3L, despues.mano());
        assertTrue(despues.puedeDeshacer());
    }

    @Test
    public void el_falta_envido_sale_del_marcador_actual() {
        vm.sumar(true, 22);
        vm.sumar(false, 18);
        assertEquals(8, vm.faltaEnvido());
    }
}
