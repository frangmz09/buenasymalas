package com.frangomez.buenasymalas.ui.historial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.frangomez.buenasymalas.R;

/** La barra que muestra de un vistazo quién domina el cabeza a cabeza. */
public class BarraProporcionView extends View {

    private final Paint izquierda = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint derecha = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF caja = new RectF();

    private int ganadasA;
    private int ganadasB;

    public BarraProporcionView(Context context) {
        this(context, null);
    }

    public BarraProporcionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        izquierda.setColor(ContextCompat.getColor(context, R.color.bordo));
        derecha.setColor(ContextCompat.getColor(context, R.color.negro_22));
    }

    public void setRecord(int ganadasA, int ganadasB) {
        this.ganadasA = ganadasA;
        this.ganadasB = ganadasB;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float radio = getHeight() / 2f;
        int total = ganadasA + ganadasB;

        caja.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(caja, radio, radio, derecha);

        if (total == 0) {
            return;
        }
        caja.set(0, 0, getWidth() * (ganadasA / (float) total), getHeight());
        canvas.drawRoundRect(caja, radio, radio, izquierda);
    }
}
