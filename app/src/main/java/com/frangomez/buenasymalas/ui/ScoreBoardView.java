package com.frangomez.buenasymalas.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.game.Reglas;

/**
 * Los cuadraditos de tiza.
 *
 * <p>Un cuadrado son cinco puntos y se dibuja con cinco trazos en orden: lado de arriba, lado
 * derecho, lado de abajo, lado izquierdo y la diagonal, que es el quinto punto. Los trazos
 * todavía no ganados se pintan al 13% de opacidad, como marcas tenues de tiza sobre la mesa.
 *
 * <p>Va en un {@code Canvas} y no en vistas anidadas porque son treinta trazos por lado que
 * cambian con cada punto: dibujarlos permite animar la aparición del último y escalar la
 * geometría a cualquier densidad sin un recurso por dpi.
 */
public class ScoreBoardView extends View {

    /** El prototipo dibuja en un viewBox de 44 con el cuadrado de (8,8) a (36,36). */
    private static final float VIEWBOX = 44f;
    private static final float BORDE = 8f;
    private static final float LADO = 28f;
    private static final float GROSOR = 3.4f;

    private static final int ALPHA_GANADO = 255;
    /** El 13% del diseño: el fantasma del cuadrado. */
    private static final int ALPHA_FANTASMA = 33;

    private static final float CUADRADO_DP = 40f;
    private static final float GAP_CUADRADOS_DP = 5f;
    private static final float GAP_ETIQUETA_DP = 5f;
    private static final float GAP_FILAS_DP = 10f;
    private static final float ETIQUETA_SP = 9f;

    private static final long DURACION_TRAZO_MS = 200L;

    private final Paint tiza = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint etiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float cuadradoPx;
    private final float gapCuadradosPx;
    private final float gapEtiquetaPx;
    private final float gapFilasPx;
    private final float alturaEtiqueta;

    private int puntaje;
    private int objetivo = Reglas.A_TREINTA;

    /** Avance del trazo recién ganado, de 0 a 1. */
    private float avanceUltimo = 1f;
    @Nullable
    private ValueAnimator animador;

    public ScoreBoardView(Context context) {
        this(context, null);
    }

    public ScoreBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float d = getResources().getDisplayMetrics().density;
        float sp = getResources().getDisplayMetrics().scaledDensity;

        cuadradoPx = CUADRADO_DP * d;
        gapCuadradosPx = GAP_CUADRADOS_DP * d;
        gapEtiquetaPx = GAP_ETIQUETA_DP * d;
        gapFilasPx = GAP_FILAS_DP * d;

        tiza.setStyle(Paint.Style.STROKE);
        tiza.setStrokeCap(Paint.Cap.ROUND);
        tiza.setStrokeWidth(GROSOR * (cuadradoPx / VIEWBOX));
        tiza.setColor(Color.WHITE);

        etiqueta.setTextSize(ETIQUETA_SP * sp);
        etiqueta.setLetterSpacing(0.16f);
        etiqueta.setTextAlign(Paint.Align.CENTER);
        Typeface narrow = ResourcesCompat.getFont(context, R.font.archivo_narrow);
        if (narrow != null) {
            etiqueta.setTypeface(narrow);
        }
        alturaEtiqueta = -etiqueta.ascent() + etiqueta.descent();
    }

    /** El color de la tiza sigue al del texto de la mitad de cancha. */
    public void setColorTiza(@ColorInt int color) {
        tiza.setColor(color);
        etiqueta.setColor(color);
        etiqueta.setAlpha(97); // el 38% del diseño
        invalidate();
    }

    public void setObjetivo(int objetivo) {
        this.objetivo = objetivo;
        invalidate();
    }

    /**
     * Cambia el puntaje. Si subió de a uno, anima la aparición del trazo nuevo; cualquier otro
     * salto —un canto, un deshacer— se dibuja de una porque animar cuatro trazos encadenados
     * se ve como un parpadeo.
     */
    public void setPuntaje(int nuevo) {
        boolean sumoUno = nuevo == puntaje + 1;
        puntaje = nuevo;
        if (sumoUno) {
            animarUltimoTrazo();
        } else {
            cancelarAnimacion();
            avanceUltimo = 1f;
            invalidate();
        }
    }

    private void animarUltimoTrazo() {
        cancelarAnimacion();
        animador = ValueAnimator.ofFloat(0f, 1f);
        animador.setDuration(DURACION_TRAZO_MS);
        animador.setInterpolator(new DecelerateInterpolator());
        animador.addUpdateListener(a -> {
            avanceUltimo = (float) a.getAnimatedValue();
            invalidate();
        });
        animador.start();
    }

    private void cancelarAnimacion() {
        if (animador != null) {
            animador.cancel();
            animador = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelarAnimacion();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ancho = (int) Math.ceil(
                Reglas.CUADRADOS_POR_FILA * cuadradoPx
                        + (Reglas.CUADRADOS_POR_FILA - 1) * gapCuadradosPx);
        int filas = filasVisibles();
        int alto = (int) Math.ceil(
                filas * (alturaEtiqueta + gapEtiquetaPx + cuadradoPx)
                        + (filas - 1) * gapFilasPx);
        setMeasuredDimension(
                resolveSize(ancho, widthMeasureSpec),
                resolveSize(alto, heightMeasureSpec));
    }

    /** A 15 la fila de buenas nunca se usa: mostrarla vacía sería prometer puntos que no hay. */
    private int filasVisibles() {
        return objetivo > Reglas.MALAS_HASTA ? 2 : 1;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float y = 0f;
        y = dibujarFila(canvas, y, getContext().getString(R.string.malas), Reglas.malas(puntaje));
        if (filasVisibles() > 1) {
            y += gapFilasPx;
            dibujarFila(canvas, y, getContext().getString(R.string.buenas), Reglas.buenas(puntaje));
        }
    }

    /** Dibuja una fila completa y devuelve la y donde termina. */
    private float dibujarFila(Canvas canvas, float y, String texto, int puntosDeLaFila) {
        canvas.drawText(texto, getWidth() / 2f, y - etiqueta.ascent(), etiqueta);
        float yCuadrados = y + alturaEtiqueta + gapEtiquetaPx;

        for (int i = 0; i < Reglas.CUADRADOS_POR_FILA; i++) {
            float x = i * (cuadradoPx + gapCuadradosPx);
            int trazos = Reglas.trazosEnCuadrado(puntosDeLaFila, i);
            boolean esElUltimo = trazos > 0 && trazos == puntosDeLaFila - i * Reglas.PUNTOS_POR_CUADRADO;
            dibujarCuadrado(canvas, x, yCuadrados, trazos, esElUltimo);
        }
        return yCuadrados + cuadradoPx;
    }

    /**
     * Los cinco trazos del cuadrado. Cada uno son cuatro coordenadas en el viewBox del
     * prototipo, escaladas al tamaño real.
     */
    private void dibujarCuadrado(Canvas canvas, float x, float y, int trazos, boolean animarUltimo) {
        float e = cuadradoPx / VIEWBOX;
        float a = BORDE * e;
        float b = (BORDE + LADO) * e;

        float[][] segmentos = {
                {a, a, b, a},   // lado de arriba
                {b, a, b, b},   // lado derecho
                {b, b, a, b},   // lado de abajo
                {a, b, a, a},   // lado izquierdo
                {a, a, b, b}    // la diagonal: el quinto punto
        };

        for (int i = 0; i < Reglas.TRAZOS_POR_CUADRADO; i++) {
            boolean ganado = i < trazos;
            tiza.setAlpha(ganado ? ALPHA_GANADO : ALPHA_FANTASMA);

            float[] s = segmentos[i];
            float x2 = s[2];
            float y2 = s[3];
            if (ganado && animarUltimo && i == trazos - 1 && avanceUltimo < 1f) {
                x2 = s[0] + (s[2] - s[0]) * avanceUltimo;
                y2 = s[1] + (s[3] - s[1]) * avanceUltimo;
            }
            canvas.drawLine(x + s[0], y + s[1], x + x2, y + y2, tiza);
        }
    }
}
