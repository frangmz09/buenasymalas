package com.frangomez.buenasymalas.game;

/**
 * Las reglas del contador, sin nada de Android encima para poder testearlas.
 *
 * <p>Las malas son los puntos 1 a 15 y las buenas del 16 al 30, tres cuadrados de cinco cada
 * fila. En una partida a 15 se ocupan solo las malas y se gana al completarlas: la misma vista
 * sirve para los dos modos, sin cuadrados partidos.
 */
public final class Reglas {

    public static final int MALAS_HASTA = 15;
    public static final int PUNTOS_POR_CUADRADO = 5;
    public static final int CUADRADOS_POR_FILA = 3;
    public static final int TRAZOS_POR_CUADRADO = 5;

    public static final int A_QUINCE = 15;
    public static final int A_TREINTA = 30;

    private Reglas() {
    }

    /** Puntos que van en la fila de malas. */
    public static int malas(int puntaje) {
        return Math.min(Math.max(puntaje, 0), MALAS_HASTA);
    }

    /** Puntos que van en la fila de buenas. */
    public static int buenas(int puntaje) {
        return Math.max(0, puntaje - MALAS_HASTA);
    }

    /** Cuantos de los cinco trazos estan ganados en un cuadrado de una fila. */
    public static int trazosEnCuadrado(int puntosDeLaFila, int cuadrado) {
        int enEsteCuadrado = puntosDeLaFila - cuadrado * PUNTOS_POR_CUADRADO;
        return Math.min(Math.max(enEsteCuadrado, 0), PUNTOS_POR_CUADRADO);
    }

    /** En que mitad de la cancha esta el puntaje, para la etiqueta de arriba del numero. */
    public static boolean estaEnBuenas(int puntaje) {
        return puntaje > MALAS_HASTA;
    }

    /** Suma acotada al rango de la partida: no se baja de cero ni se pasa del objetivo. */
    public static int sumar(int puntaje, int delta, int objetivo) {
        return Math.min(Math.max(puntaje + delta, 0), objetivo);
    }

    /** El falta envido vale lo que le falta al que va arriba, y nunca menos de uno. */
    public static int faltaEnvido(int puntajeA, int puntajeB, int objetivo) {
        return Math.max(1, objetivo - Math.max(puntajeA, puntajeB));
    }

    /** Una paliza es ganar por la mitad del objetivo o mas. */
    public static boolean esPaliza(int puntajeA, int puntajeB, int objetivo) {
        return Math.abs(puntajeA - puntajeB) >= objetivo / 2;
    }
}
