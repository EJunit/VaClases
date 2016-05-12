package hn.uth.hackaton.Mensajes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hn.uth.hackaton.R;

public class AdaptadorMensajes extends RecyclerView.Adapter<AdaptadorMensajes.ViewHolder> {

    private List<Mensajes> items;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
        public TextView titulo;
        public TextView mensaje;
        public TextView fecha;

        Typeface robotoSlab_bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/RobotoSlab-Bold.ttf");
        Typeface Roboto_Light = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        Typeface roboto_regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");

        public ViewHolder(View v) {
            super(v);
            titulo = (TextView) v.findViewById(R.id.titulo);
            titulo.setTypeface(robotoSlab_bold);
            mensaje = (TextView) v.findViewById(R.id.mensaje);
            mensaje.setTypeface(Roboto_Light);
            fecha = (TextView) v.findViewById(R.id.fecha);
            fecha.setTypeface(roboto_regular);
        }
    }

    public AdaptadorMensajes(Context context, List<Mensajes> items) {
        this.items = items;
        this.mContext = context;
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_lista_mensaje, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Mensajes item = items.get(i);

        viewHolder.titulo.setText(item.getTitulo());
        viewHolder.mensaje.setText(item.getMensaje());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        try {
            d = sdf.parse(item.getFecha());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha = new SimpleDateFormat("dd-MMMM, yyyy");
        String fecha_inicio = format_fecha.format(d);
        viewHolder.fecha.setText(fecha_inicio);
    }
}