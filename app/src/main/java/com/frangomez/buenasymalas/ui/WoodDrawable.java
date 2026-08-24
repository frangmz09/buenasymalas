package com.frangomez.buenasymalas.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * La textura de madera de la mesa: el color base con vetas apenas visibles encima.
 *
 * <p>Equivale al {@code repeating-linear-gradient(93deg, ...)} del prototipo. Se dibuja en vez
 * de tilear un PNG porque el color base cambia por jugador —cada uno elige su mitad de cancha—
 * y un tile obligaría a un recurso por color.
 */
public class WoodDrawable extends Drawable {

    /** Ancho de la veta, en px del prototipo (dp). */
    private static final float VETA_ANCHO_DP = 2f;
    /** Distancia entre vetas, de arranque a arranque. */
    private static final float VETA_PASO_DP = 9f;
    /** Los 93 grados del gradiente: 3 de inclinación respecto de la vertical. */
    private static final float INCLINACION = 3f;
    /** rgba(0,0,0,.16) */
    private static final int VETA_ALPHA = 41;

    private final Paint pintura = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float pasoPx;
    private final float corrimiento;

    @ColorInt
    private int colorBase;

    public WoodDrawable(@ColorInt int colorBase, float densidad) {
        this.colorBase = colorBase;
        this.pasoPx = VETA_PASO_DP * densidad;
        this.corrimiento = (float) Math.tan(Math.toRadians(INCLINACION));
        pintura.setStyle(Paint.Style.STROKE);
        pintura.setStrokeWidth(VETA_ANCHO_DP * densidad);
        pintura.setColor(Color.argb(VETA_ALPHA, 0, 0, 0));
    }

    public void setColorBase(@ColorInt int colorBase) {
        if (this.colorBase != colorBase) {
            this.colorBase = colorBase;
            invalidateSelf();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        canvas.drawColor(colorBase);

        // Las vetas van casi verticales. El desplazamiento por la inclinación se compensa
        // arrancando antes del borde izquierdo, para que no quede una franja lisa.
        float desvio = b.height() * corrimiento;
        for (float x = -desvio; x < b.width() + desvio; x += pasoPx) {
            canvas.drawLine(b.left + x, b.top, b.left + x + desvio, b.bottom, pintura);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        // El fondo de la mesa es opaco por definición; no se le baja la opacidad.
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        pintura.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
