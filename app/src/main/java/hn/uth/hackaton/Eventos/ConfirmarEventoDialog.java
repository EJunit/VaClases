package hn.uth.hackaton.Eventos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class ConfirmarEventoDialog extends DialogFragment {

    private Preferencias conf;
    private RadioButton b1;
    private  RadioButton b2;

    public ConfirmarEventoDialog() {
    }

    private String loadIdentidadAlumno() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno",Context.MODE_PRIVATE);
        return prefs.getString("identidad_alumno", " ");
    }

    private String loadIEvento() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("id_evento", " ");
    }

    private String loadFechaEvento() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("fecha_evento", " ");
    }

    private String respuesta;
    private ProgressDialog dialog;
    private ProgressDialog dialog2;

    @Override
    public void onResume() {
        super.onResume();
        dialog2 = ProgressDialog.show(getContext(), "", "Espere..");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") final View v = inflater.inflate(R.layout.dialog_participacion_evento, null);

        conf = new Preferencias(getContext());

        Typeface robotoSlab_bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoSlab-Bold.ttf");
        final Typeface Roboto_Light = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        RadioGroup radioGroupEventos = (RadioGroup) v.findViewById(R.id.radioEve);
        Button btnCancelar = (Button) v.findViewById(R.id.btnEve_cance);
        Button btnListo = (Button) v.findViewById(R.id.btnEve_list);
        TextView tituloEvento = (TextView) v.findViewById(R.id.txtTitulo_dialog_Eve);
        tituloEvento.setTypeface(robotoSlab_bold);

        b1 = (RadioButton)v.findViewById(R.id.rdbSi);
        b1.setTypeface(Roboto_Light);
        b2 = (RadioButton)v.findViewById(R.id.rdbNo);
        b2.setTypeface(Roboto_Light);

        String urlsts = "http://vaclases.netsti.com/api/eventos?token="+conf.getTokken();

        JsonObjectRequest reqsts =  new JsonObjectRequest(urlsts, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray dataJson = response.getJSONArray("data");

                    JSONObject dataParticipacion;

                    for(int a=0; a<dataJson.length();a++){

                        dataParticipacion = dataJson.getJSONObject(a);

                        JSONArray arrayParticipacion = dataParticipacion.getJSONArray("participacion");

                        if(arrayParticipacion.length()>0){
                            for(int c=0;c<arrayParticipacion.length();c++) {
                                JSONObject b = arrayParticipacion.getJSONObject(c);
                                if(loadIEvento().equals(b.getString("evento_id"))){
                                    if(b.getString("status").equals("1")){
                                        b1.setChecked(true);
                                        dialog2.cancel();
                                    }else{
                                        b2.setChecked(true);
                                        dialog2.cancel();
                                    }
                                }
                            }
                        }else{
                            dialog2.cancel();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERROR",error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Cookie", conf.getCookie());
                return headers;
            }
        };

        reqsts.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(reqsts);

        btnListo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(getContext(), "", "Cargando contenido");

                String token = conf.getTokken().replace(" ","");

                String url = "http://vaclases.netsti.com/api/participacion?token="+token.replace("\n", "")+"&status="+respuesta+"&alumno_id="+loadIdentidadAlumno()+"&evento_id="+loadIEvento();

                JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String r = response.getString("status");

                            if(r.equals("exito")){
                                if(respuesta.equals("1")) {
                                    Toast.makeText(getContext(), "Confirmacion ingresada", Toast.LENGTH_SHORT).show();
                                    addCalendarEvent(loadFechaEvento());
                                    dialog.cancel();
                                    dismiss();
                                }else{
                                    Toast.makeText(getContext(), "Confirmacion ingresada", Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                    dismiss();
                                }
                            }else {
                                Toast.makeText(getContext(),"error al confirmar",Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        } catch (JSONException e) {
                            dialog.cancel();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),"Error al conectarse con el servidor, Intente de nuevo",Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Cookie", conf.getCookie());
                        return headers;
                    }
                };

                req.setRetryPolicy(new DefaultRetryPolicy(15000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                requestQueue.add(req);
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        radioGroupEventos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rdbSi:
                        respuesta = "1";
                        break;
                    case R.id.rdbNo:
                        respuesta = "2";
                        break;
                }
            }
        });

        builder.setView(v);

        return builder.create();
    }

    public void addCalendarEvent(String fecha ) {

        Date d = null;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            d = sdf.parse(fecha);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format_anio = new SimpleDateFormat("yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format_mes = new SimpleDateFormat("MM");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format_dia = new SimpleDateFormat("dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format_hora = new SimpleDateFormat("HH");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format_min = new SimpleDateFormat("mm");

        int año = Integer.parseInt(format_anio.format(d));
        int mes = Integer.parseInt(format_mes.format(d))-1;
        int dia = Integer.parseInt(format_dia.format(d));
        int hra = Integer.parseInt(format_hora.format(d));
        int min = Integer.parseInt(format_min.format(d));

        long calID = 1;
        long startMillis;
        long endMillis;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(año, mes, dia, hra, min);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(año, mes, dia, hra, min);
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, "VaClases: Recordatorio");
        values.put(CalendarContract.Events.DESCRIPTION, "Tienes un Evento programado de tu Institucion");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "CST");

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Uri event= cr.insert(CalendarContract.Events.CONTENT_URI, values);

        long id = ContentUris.parseId(event);

        ContentValues values2 = new ContentValues();
        values2.put(CalendarContract.Reminders.EVENT_ID, id);
        values2.put(CalendarContract.Reminders.METHOD,
                CalendarContract.Reminders.METHOD_ALERT);
        values2.put(CalendarContract.Reminders.MINUTES, 60);

        cr.insert(CalendarContract.Reminders.CONTENT_URI, values2);
    }
}

