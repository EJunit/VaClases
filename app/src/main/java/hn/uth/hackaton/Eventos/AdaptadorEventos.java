package hn.uth.hackaton.Eventos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hn.uth.hackaton.ItemClickListener;
import hn.uth.hackaton.R;

public class AdaptadorEventos extends RecyclerView.Adapter<AdaptadorEventos.ViewHolder> {

    private List<Eventos> items;
    private Context mContext;
    public static final String NUM_CUENTA = "MiCuenta";
    SharedPreferences prefs;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListener clickListener;
        // Campos respectivos de un item
        public TextView actividad;
        public TextView nombre;
        public TextView fecha;

        Typeface robotoSlab_bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/RobotoSlab-Bold.ttf");
        Typeface roboto_regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface Roboto_Light = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");

        public ViewHolder(View v) {
            super(v);
            nombre = (TextView) v.findViewById(R.id.txtactividad);
            nombre.setTypeface(robotoSlab_bold);
            actividad = (TextView) v.findViewById(R.id.txtDescEvento);
            actividad.setTypeface(Roboto_Light);
            fecha = (TextView) v.findViewById(R.id.txtfechaEvento);
            fecha.setTypeface(roboto_regular);
            v.setOnClickListener(this);

        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }

    public AdaptadorEventos(List<Eventos> items, Context context) {
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
                .inflate(R.layout.item_lista_eventos, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Eventos item = items.get(i);

        viewHolder.actividad.setText(item.getDescripcion_evento());
        viewHolder.nombre.setText(item.getTitulo_evento());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = null;
        try {
            d = sdf.parse(item.getFecha());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha = new SimpleDateFormat("dd-MMMM, yyyy - hh:mm a");
        //@SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha2 = new SimpleDateFormat("HH.mm:ss");
        String fecha_inicio = format_fecha.format(d);
        //String d2= format_fecha2.format(d);

        viewHolder.fecha.setText(fecha_inicio);

        viewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                guardarPreferencias(items.get(position).getIdEvento(), items.get(position).getFecha());//guarda el codigo del evento o actividad
                FragmentActivity activity = (FragmentActivity) (view.getContext());
                FragmentManager fm = activity.getSupportFragmentManager();

                ConfirmarEventoDialog alertDialog = new ConfirmarEventoDialog();
                alertDialog.show(fm, "ConfirmarEventoDialog");
            }
        });
    }

    public void guardarPreferencias(String IdEvento, String fecha) {
        prefs = mContext.getSharedPreferences(NUM_CUENTA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("id_evento", IdEvento);
        editor.putString("fecha_evento", fecha);
        editor.apply();
    }
}