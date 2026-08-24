package com.frangomez.buenasymalas.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.frangomez.buenasymalas.R;

/**
 * La línea punteada de tiza que parte la cancha al medio: 14dp de trazo, 12dp de aire.
 * Un {@code shape} con {@code dashGap} sólo puntea bordes horizontales, así que se dibuja.
 */
public class DashedLineView extends View {

    private static final float TRAZO_DP = 14f;
    private static final float AIRE_DP = 12f;

    private final Paint pintura = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DashedLineView(Context context) {
        this(context, null);
    }

    public DashedLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float d = getResources().getDisplayMetrics().density;
        pintura.setStyle(Paint.Style.STROKE);
        pintura.setColor(ContextCompat.getColor(context, R.color.cobre));
        pintura.setStrokeWidth(3f * d);
        pintura.setPathEffect(new DashPathEffect(new float[]{TRAZO_DP * d, AIRE_DP * d}, 0f));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float x = getWidth() / 2f;
        canvas.drawLine(x, 0f, x, getHeight(), pintura);
    }
}
