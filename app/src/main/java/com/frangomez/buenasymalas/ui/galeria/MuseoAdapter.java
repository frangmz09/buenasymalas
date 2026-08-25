package com.frangomez.buenasymalas.ui.galeria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.frangomez.buenasymalas.R;
import com.frangomez.buenasymalas.data.PhotoDao;
import com.frangomez.buenasymalas.ui.Formatos;
import com.frangomez.buenasymalas.ui.Fotos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Las tarjetas del museo: la foto arriba y el sello en el pie. */
public class MuseoAdapter extends RecyclerView.Adapter<MuseoAdapter.Celda> {

    private final List<PhotoDao.FotoDeMuseo> fotos = new ArrayList<>();

    public void setFotos(List<PhotoDao.FotoDeMuseo> nuevas) {
        fotos.clear();
        fotos.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Celda onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.celda_museo, parent, false);
        return new Celda(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Celda celda, int position) {
        PhotoDao.FotoDeMuseo f = fotos.get(position);

        // El titulo es el apodo si lo tiene; si no, el nombre pelado.
        celda.titulo.setText(f.alias == null || f.alias.isEmpty() ? f.perdedor : f.alias);
        celda.meta.setText(celda.itemView.getContext().getString(R.string.meta_museo,
                Math.max(f.match.scoreA, f.match.scoreB),
                Math.min(f.match.scoreA, f.match.scoreB),
                Formatos.fecha(celda.itemView.getContext(), f.match.playedAt)));

        File archivo = new File(f.photo.filePath);
        if (archivo.exists()) {
            celda.foto.setImageBitmap(Fotos.miniatura(f.photo.filePath, 4));
        } else {
            celda.foto.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    static class Celda extends RecyclerView.ViewHolder {
        final ImageView foto;
        final TextView titulo;
        final TextView meta;

        Celda(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.foto);
            titulo = itemView.findViewById(R.id.titulo);
            meta = itemView.findViewById(R.id.meta);

            // El grid tiene gap 10dp: se pone aca porque el LayoutManager no lo hace solo.
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            int gap = (int) (5 * itemView.getResources().getDisplayMetrics().density);
            lp.setMargins(gap, gap, gap, gap);
            itemView.setLayoutParams(lp);
        }
    }
}
