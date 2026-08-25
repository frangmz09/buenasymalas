package com.frangomez.buenasymalas.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

/**
 * Decodificar las fotos del museo respetando su rotación real.
 *
 * <p>CameraX no rota los píxeles del JPEG al guardarlo: escribe la rotación en el tag EXIF y
 * deja que quien lo muestre la aplique. {@link BitmapFactory#decodeFile} solo, sin leer ese
 * tag, deja la miniatura de costado en cualquier cámara cuyo sensor no venga "derecho" —algo
 * que el emulador no reproduce, porque su cámara simulada no tiene esa rotación de sensor.
 */
public final class Fotos {

    private Fotos() {
    }

    /** Miniatura reducida por {@code inSampleSize} y ya rotada como corresponde. */
    @Nullable
    public static Bitmap miniatura(String path, int inSampleSize) {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opciones);
        if (bitmap == null) {
            return null;
        }
        return rotarSegunExif(bitmap, path);
    }

    private static Bitmap rotarSegunExif(Bitmap bitmap, String path) {
        int grados = gradosDeRotacion(path);
        if (grados == 0) {
            return bitmap;
        }
        Matrix matriz = new Matrix();
        matriz.postRotate(grados);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matriz, true);
    }

    private static int gradosDeRotacion(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientacion = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientacion) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }
}
