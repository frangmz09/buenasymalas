package com.frangomez.buenasymalas.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReglasTest {

    @Test
    public void las_malas_llegan_hasta_quince() {
        assertEquals(0, Reglas.malas(0));
        assertEquals(7, Reglas.malas(7));
        assertEquals(15, Reglas.malas(15));
        assertEquals(15, Reglas.malas(22));
    }

    @Test
    public void las_buenas_arrancan_en_dieciseis() {
        assertEquals(0, Reglas.buenas(15));
        assertEquals(1, Reglas.buenas(16));
        assertEquals(15, Reglas.buenas(30));
    }

    @Test
    public void un_cuadrado_cerrado_son_cinco_trazos() {
        // Con cinco puntos, el primer cuadrado esta completo y los otros dos vacios.
        assertEquals(5, Reglas.trazosEnCuadrado(5, 0));
        assertEquals(0, Reglas.trazosEnCuadrado(5, 1));
        assertEquals(0, Reglas.trazosEnCuadrado(5, 2));
    }

    @Test
    public void los_trazos_se_reparten_de_a_cinco() {
        // Siete puntos: un cuadrado cerrado y dos palitos en el siguiente.
        assertEquals(5, Reglas.trazosEnCuadrado(7, 0));
        assertEquals(2, Reglas.trazosEnCuadrado(7, 1));
        assertEquals(0, Reglas.trazosEnCuadrado(7, 2));
    }

    @Test
    public void quince_puntos_llenan_los_tres_cuadrados() {
        for (int i = 0; i < Reglas.CUADRADOS_POR_FILA; i++) {
            assertEquals(5, Reglas.trazosEnCuadrado(15, i));
        }
    }

    @Test
    public void la_zona_cambia_despues_del_quince() {
        assertFalse(Reglas.estaEnBuenas(15));
        assertTrue(Reglas.estaEnBuenas(16));
    }

    @Test
    public void el_puntaje_no_baja_de_cero_ni_pasa_el_objetivo() {
        assertEquals(0, Reglas.sumar(0, -1, 30));
        assertEquals(30, Reglas.sumar(29, 4, 30));
        assertEquals(15, Reglas.sumar(14, 1, 15));
    }

    @Test
    public void el_falta_envido_vale_lo_que_le_falta_al_que_va_arriba() {
        assertEquals(8, Reglas.faltaEnvido(22, 18, 30));
        assertEquals(8, Reglas.faltaEnvido(18, 22, 30));
        assertEquals(30, Reglas.faltaEnvido(0, 0, 30));
    }

    @Test
    public void el_falta_envido_nunca_vale_menos_de_uno() {
        assertEquals(1, Reglas.faltaEnvido(29, 29, 30));
        assertEquals(1, Reglas.faltaEnvido(30, 12, 30));
    }

    @Test
    public void una_paliza_es_ganar_por_la_mitad_del_objetivo() {
        assertTrue(Reglas.esPaliza(30, 4, 30));
        assertTrue(Reglas.esPaliza(30, 15, 30));
        assertFalse(Reglas.esPaliza(30, 22, 30));
        assertTrue(Reglas.esPaliza(15, 7, 15));
    }
}
