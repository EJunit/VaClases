package hn.uth.hackaton.Validacion;

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

public class NewAdapterValidaciones extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Validacion> items;
    private Context mContext;
    public static final String NUM_CUENTA = "MiCuenta";
    SharedPreferences prefs;

    public NewAdapterValidaciones(List<Validacion> items, Context context) {
        this.items = items;
        this.mContext = context;
    }

    //Vista para las validaciones de clases
    class ViewHolder0 extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemClickListener clickListener;

        // Campos respectivos de un item
        public TextView fecha_ini;
        public TextView fecha_fin;

        Typeface robotoSlab_bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/RobotoSlab-Bold.ttf");
        Typeface Roboto_Light = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");

        public ViewHolder0(View v) {
            super(v);
            TextView tituloVal = (TextView) v.findViewById(R.id.txtValidacionClase);
            TextView txtal = (TextView) v.findViewById(R.id.txtal_val);
            tituloVal.setTypeface(robotoSlab_bold);
            txtal.setTypeface(Roboto_Light);
            fecha_ini = (TextView) v.findViewById(R.id.txtfecha1);
            fecha_ini.setTypeface(Roboto_Light);
            fecha_fin = (TextView) v.findViewById(R.id.txtfecha2);
            fecha_fin.setTypeface(Roboto_Light);
            v.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }//fin del ViewHolder de las validaciones de clases

    //Vista para las validaciones de problemas
    class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemClickListener clickListener;
        public TextView fecha;

        Typeface robotoSlab_bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/RobotoSlab-Bold.ttf");
        Typeface Roboto_Light = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");

        public ViewHolder2(View v) {
            super(v);
            TextView tituloVal = (TextView) v.findViewById(R.id.txtValidacionPro);
            tituloVal.setTypeface(robotoSlab_bold);

            fecha = (TextView) v.findViewById(R.id.txtfecha1_pro);
            fecha.setTypeface(Roboto_Light);
            v.setOnClickListener(this);

        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }//fin del ViewHolder de las validaciones de problemas

    @Override
    public int getItemViewType(int position) {

        Validacion multipleRowModel = items.get(position);

        return multipleRowModel.getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //evalua cual xml inflara
        switch (viewType) {
            case 1:
                return new ViewHolder0(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_lista_validacion_clase, parent, false));
            case 2:
                return new ViewHolder2(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_lista_validacion_problemas, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Validacion item = items.get(position);

        if (getItemViewType(position) == 1) {
            ViewHolder0 holder1 = (ViewHolder0) holder;

            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date d = sdf.parse(item.getFecha_ini());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha = new SimpleDateFormat("dd-MMMM");
                String fecha_inicio = format_fecha.format(d);
                holder1.fecha_ini.setText(fecha_inicio);
                //fecha final
                Date d2 = sdf.parse(item.getFecha_fin());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha2 = new SimpleDateFormat("dd-MMMM, yyyy");
                String fecha_fin = format_fecha2.format(d2);

                holder1.fecha_fin.setText(fecha_fin);
            } catch (ParseException ignored) {
            }

            holder1.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position) {

                    String dias = items.get(position).getCant_dias();

                    guardarValidacionClase(dias, items.get(position).getFecha_ini(), items.get(position).getId_validacion_clases());//guarda la fecha y el codigo

                    FragmentActivity activity = (FragmentActivity) (view.getContext());
                    FragmentManager fm = activity.getSupportFragmentManager();
                    ValidacionClasesDialog alertDialog = new ValidacionClasesDialog();
                    alertDialog.show(fm, "ValidacionClasesDialog");
                }
            });

        } else {
            ViewHolder2 holder2 = (ViewHolder2) holder;

            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date d = sdf.parse(item.getFecha_problema());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha = new SimpleDateFormat("dd-MMMM, yyyy");
                String fecha_format = format_fecha.format(d);
                holder2.fecha.setText(fecha_format);//asignamos la fecha al card
            } catch (ParseException ignored) {
            }

            holder2.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position) {

                    //fecha preferencia
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date d3 = null;
                    try {
                        d3 = sdf.parse(items.get(position).getFecha_problema());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha3 = new SimpleDateFormat("yyyy-MM-dd");
                    String fecha_pref = format_fecha3.format(d3);

                    guardarEncuesta(items.get(position).getId_problema(), items.get(position).getPreguntas(), fecha_pref);

                    FragmentActivity activity = (FragmentActivity) (view.getContext());
                    FragmentManager fm = activity.getSupportFragmentManager();
                    ValidacionProblemasDialog alertDialog = new ValidacionProblemasDialog();
                    alertDialog.show(fm, "ValidacionProblemasDialog");
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void guardarValidacionClase(String dias, String fecha, String id_validacion) {
        prefs = mContext.getSharedPreferences(NUM_CUENTA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fecha", fecha);
        editor.putString("dias", dias);
        editor.putString("id_val", id_validacion);//codigo de validacion de clases
        editor.apply();
    }

    public void guardarEncuesta(String id_validacion, String preguntas, String fecha) {
        prefs = mContext.getSharedPreferences(NUM_CUENTA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("preguntas", preguntas);//preguntas
        editor.putString("id_val", id_validacion);//codigo de pregunta
        editor.putString("fechaP", fecha);

        editor.apply();
    }
}
