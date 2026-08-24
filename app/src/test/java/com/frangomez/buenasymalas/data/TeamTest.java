package com.frangomez.buenasymalas.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

public class TeamTest {

    @Test
    public void rosterKey_ordena_los_ids() {
        // La misma dupla tiene que dar la misma clave sin importar como se cargo.
        assertEquals(Team.rosterKeyDe(Arrays.asList(7L, 3L)), Team.rosterKeyDe(Arrays.asList(3L, 7L)));
        assertEquals("3-7", Team.rosterKeyDe(Arrays.asList(7L, 3L)));
    }

    @Test
    public void rosterKey_de_un_solo_jugador() {
        assertEquals("11", Team.rosterKeyDe(Arrays.asList(11L)));
    }

    @Test
    public void rosterKey_de_un_trio() {
        assertEquals("2-5-9", Team.rosterKeyDe(Arrays.asList(9L, 2L, 5L)));
    }
}
