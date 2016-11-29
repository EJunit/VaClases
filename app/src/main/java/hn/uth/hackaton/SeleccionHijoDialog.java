package hn.uth.hackaton;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.util.HashMap;
import java.util.Map;

public class SeleccionHijoDialog extends DialogFragment {

    ProgressDialog dialog;
    LinearLayout r;
    Preferencias conf;
    Typeface Roboto_Light;
    private int cantHijo;

    private void setNombreAlumno(String nombre) {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("nombre_alumno", nombre);
        edit.apply();
    }

    private void setIdentidadAlumno(String nombre) {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("identidad_alumno", nombre);
        edit.apply();
    }

    private void setNombreEscuela(String nombre) {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("nombre_escuela", nombre);
        edit.apply();
    }

    private void setEscuelaSelect(String nombre) {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("escuela_select", nombre);
        edit.apply();
    }

    private String loadNombre() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("nombre_alumno", "nombre");
    }

    public SeleccionHijoDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = ProgressDialog.show(getContext(), "", "Cargando contenido");
        return createSeleccionHijoDialog();
    }

    //Crea un diálogo con una lista de radios
    public AlertDialog createSeleccionHijoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_seleccion_hijos, null);

        Roboto_Light = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        r = (LinearLayout) v.findViewById(R.id.LinearHijo);
        conf = new Preferencias(getContext());

        String url = Const.ip + "api/encargado?token=" + conf.getTokken();

        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parser(response, r);
                dialog.cancel();
            }
        }, new Response.ErrorListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.cancel();
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
                12000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(req);

        Button ok = (Button) v.findViewById(R.id.btn_hijos);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cantHijo > 1) {
                    Toast.makeText(getContext(), "Selecciono a : " + loadNombre(), Toast.LENGTH_SHORT).show();
                    dismiss();
                    openProfile();
                } else {
                    dismiss();
                }
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

    public void parser(JSONObject response, LinearLayout r) {

        try {
            if (response.getString("status").equals("exito")) {

                JSONObject infoData = response.getJSONObject("data");

                JSONArray alumnos = infoData.getJSONArray("alumnos");

                for (int a = 0; a < alumnos.length(); a++) {
                    try {

                        final RadioButton[] rb = new RadioButton[alumnos.length()];

                        RadioGroup rg = new RadioGroup(getContext()); //Creamos el RadioGroup
                        rg.setOrientation(RadioGroup.VERTICAL);
                        rg.setPadding(30, 30, 10, 20);

                        RadioGroup contrasGrp = new RadioGroup(getContext());
                        contrasGrp.setOrientation(RadioGroup.VERTICAL);

                        final JSONObject infoAlumno = alumnos.getJSONObject(a);
                        cantHijo = alumnos.length();
                        rb[a] = new RadioButton(getContext());
                        rg.addView(rb[a]);

                        rb[a].setText(infoAlumno.getString("nombre"));
                        rb[a].setTypeface(Roboto_Light);
                        rb[a].setTextColor(ContextCompat.getColor(getContext(), R.color.letraPrimary));

                        if (infoAlumno.getString("nombre").equals(loadNombre())) {
                            rb[a].setChecked(true);
                        }

                        rb[a].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                if (isChecked) {

                                    SharedPreferences prefsAlumno = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editorAlumno = prefsAlumno.edit();
                                    editorAlumno.clear();
                                    editorAlumno.apply();

                                    try {
                                        JSONObject infoEscuelaAlumno = infoAlumno.getJSONObject("centro_educativo");
                                        setNombreEscuela(infoEscuelaAlumno.getString("nombre"));
                                        setNombreAlumno(infoAlumno.getString("nombre"));
                                        setIdentidadAlumno(infoAlumno.getString("id"));
                                        setEscuelaSelect(infoEscuelaAlumno.getString("codigo"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        });

                        r.addView(rg);

                        dialog.cancel();
                    } catch (JSONException ignored) {

                    }
                }
            }//llave que termina la condicion del status de exito
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

