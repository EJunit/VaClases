package hn.uth.hackaton.Validacion;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hn.uth.hackaton.Const;
import hn.uth.hackaton.MainActivity;
import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class ValidacionClasesDialog extends DialogFragment {

    LinearLayout r;
    Typeface Roboto_Light;
    Typeface robotoSlab_bold;
    JSONObject obj = new JSONObject();
    JSONArray arrayFechas = new JSONArray();
    private Preferencias conf;

    public ValidacionClasesDialog() {
    }

    private String loadIdValidacion() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("id_val", "id_val");
    }

    private String loadIdentidadAlumno() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("identidad_alumno", " ");
    }

    private String loadDias() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("dias", "no hay");
    }

    private String loadFecha() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("fecha", "no hay");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public Date sumarRestarDiasFecha(Date fecha, int dias) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(fecha); // Configuramos la fecha que se recibe
        calendar.add(Calendar.DAY_OF_YEAR, dias);  // numero de días a añadir, o restar en caso de días<0

        return calendar.getTime(); // Devuelve el objeto Date con los nuevos días añadidos
    }

    @SuppressLint("SetTextI18n")
    public AlertDialog createLoginDialogo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_validacion_clases, null);

        robotoSlab_bold = Typeface.createFromAsset(v.getContext().getAssets(), "fonts/RobotoSlab-Bold.ttf");
        Roboto_Light = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        conf = new Preferencias(getContext());

        int dias = Integer.parseInt(loadDias());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {

            Date d = sdf.parse(loadFecha());

            @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha = new SimpleDateFormat("yyyy-MM-dd");

            for (int a = 0; a < dias; a++) {

                String fecha = format_fecha.format(sumarRestarDiasFecha(d, a));

                try {
                    JSONObject res = new JSONObject();
                    res.put("_id", a + 1);
                    res.put("fecha", fecha);
                    arrayFechas.put(res);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Button btnCancelar = (Button) v.findViewById(R.id.btnVal_cance);
        Button btnValidar = (Button) v.findViewById(R.id.btnVal_val);
        TextView titulo_dialog = (TextView) v.findViewById(R.id.txtTitulo_dialog_val);
        titulo_dialog.setTypeface(robotoSlab_bold);

        r = (LinearLayout) v.findViewById(R.id.LinearValidacionClase);
        try {
            busquedaDatos();
        } catch (Exception ignored) {
        }

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnValidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext()).setTitle("Confirmación")
                        .setMessage("¿Deseas confirmar tus respuestas? No podrás cambiarlas luego, recuerda que las casillas que no marques, se tomaran como días en los que no hubo clases.")
                        .setIcon(R.drawable.info_dialog)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                JSONObject Infofecha;
                                String respuestas ="";
                                for (int a = 1; a <= arrayFechas.length(); a++) {

                                    try {
                                        Infofecha = arrayFechas.getJSONObject(a - 1);

                                        String fecha = Infofecha.getString("fecha");
                                        String respuesta = String.valueOf(obj.get(String.valueOf(a)));

                                        respuestas = respuestas + String.valueOf(a)+","+respuesta+","+fecha+";";

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                SaveValidacion(respuestas);
                            }
                        })//fin del positive clic
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });

        builder.setView(v);

        return builder.create();
    }

    private void openProfile() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
        getActivity().finish();
    }

    public void SaveValidacion(String respuestas) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", "Guardando...");
        dialog.show();
        String token = conf.getTokken().replace(" ", "");

        String url = Const.ip+"api/confirma?token=" + token.replace("\n", "")+
                "&validacion_id=" + loadIdValidacion()+"&alumno_id=" + loadIdentidadAlumno()+"&type=1"+
                "&respuestas="+respuestas;

        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String r = response.getString("status");

                    switch (r) {
                        case "exito":
                            dialog.hide();
                            Toast.makeText(getContext(), "Validaciones Ingresadas, gracias por su aporte", Toast.LENGTH_SHORT).show();
                            dismiss();
                            openProfile();
                            break;
                        default:
                            Toast.makeText(getContext(), "error al confirmar", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.hide();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Cookie", conf.getCookie());
                return headers;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(req);
    }//fin de metodo save

    private void busquedaDatos() {
        parser(arrayFechas, r);
    }

    @SuppressLint("SetTextI18n")
    public void parser(JSONArray response, LinearLayout r) {

        String id_valor;

        if (response.length() > 0) {

            for (int a = 0; a < response.length(); a++) {

                try {
                    JSONObject infoJosn = response.getJSONObject(a);

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date d3 = null;
                    try {
                        d3 = sdf.parse(infoJosn.getString("fecha"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha3 = new SimpleDateFormat("EEEE");
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha4 = new SimpleDateFormat("dd");
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat format_fecha5 = new SimpleDateFormat("MMMM");

                    String fecha_pref = format_fecha3.format(d3) + " - " + format_fecha4.format(d3) + "/ " + format_fecha5.format(d3);

                    CheckBox ch = new CheckBox(getContext());
                    ch.setPadding(25, 22, 20, 20);
                    ch.setText(fecha_pref);
                    r.addView(ch);
                    id_valor = infoJosn.getString("_id");

                    ch.setTypeface(Roboto_Light);
                    ch.setTextColor(ContextCompat.getColor(getContext(), R.color.letraPrimary));
                    final String finalId_valor = id_valor;

                    if (!ch.isChecked()) {
                        obj.put(finalId_valor, "0");
                    }
                    ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            try {
                                if (buttonView.isChecked()) {
                                    obj.put(finalId_valor, "1");
                                } else {
                                    obj.put(finalId_valor, "0");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (JSONException ignored) {
                }
            }
        }
    }
}

