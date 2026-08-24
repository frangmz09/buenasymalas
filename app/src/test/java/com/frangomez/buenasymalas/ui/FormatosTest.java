package com.frangomez.buenasymalas.ui;

import static org.junit.Assert.assertEquals;

import com.frangomez.buenasymalas.data.Player;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/** No usa Context: solo cubre la parte de Formatos que no depende de Android. */
public class FormatosTest {

    @Test
    public void nombre_de_equipo_de_un_jugador() {
        Player rocho = new Player("Rocho", "El Perro", 0);
        assertEquals("Rocho", Formatos.nombreDeEquipo(Collections.singletonList(rocho)));
    }

    @Test
    public void nombre_de_equipo_de_dos_jugadores() {
        Player rocho = new Player("Rocho", "", 0);
        Player fede = new Player("Fede", "", 0);
        assertEquals("Rocho y Fede", Formatos.nombreDeEquipo(Arrays.asList(rocho, fede)));
    }

    @Test
    public void nombre_de_equipo_vacio() {
        assertEquals("", Formatos.nombreDeEquipo(Collections.emptyList()));
    }
}
