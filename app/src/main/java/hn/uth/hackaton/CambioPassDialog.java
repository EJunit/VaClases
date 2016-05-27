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
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import hn.uth.hackaton.Login.LoginActivity;

public class CambioPassDialog extends DialogFragment {

    Preferencias conf;
    private EditText old_pass;
    private EditText new_pass;
    private EditText confirm_pass;
    ProgressDialog dialog;

    public CambioPassDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_cambio_pass, null);
        conf = new Preferencias(getContext());

        Typeface Roboto_Light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        old_pass = (EditText) v.findViewById(R.id.edtold_pass);
        old_pass.setTypeface(Roboto_Light);
        new_pass = (EditText) v.findViewById(R.id.edt_new_pass);
        new_pass.setTypeface(Roboto_Light);
        confirm_pass = (EditText) v.findViewById(R.id.edt_confirm_pass);
        confirm_pass.setTypeface(Roboto_Light);

        Button btnCambio = (Button) v.findViewById(R.id.btnCambioPass);
        btnCambio.setTypeface(Roboto_Light);

        Button btnCancel = (Button) v.findViewById(R.id.btnCancelPass);
        btnCancel.setTypeface(Roboto_Light);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnCambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = ProgressDialog.show(getActivity(), "", "Realizando Cambio...");

                String new_passUrl = "";
                String old_passUrl = "";

                String tempOldPass = old_pass.getText().toString();
                String tempNewPass = new_pass.getText().toString();
                String tempConfirmPass = confirm_pass.getText().toString();

                if (tempNewPass.equals(tempConfirmPass)) {
                    try {

                        String KEY = tempOldPass + conf.getCedulaPadre();
                        String KEY2 = tempNewPass + conf.getCedulaPadre();

                        byte[] data = KEY.getBytes("UTF-8");
                        byte[] data2 = KEY2.getBytes("UTF-8");

                        old_passUrl = Base64.encodeToString(data, Base64.DEFAULT);
                        new_passUrl = Base64.encodeToString(data2, Base64.DEFAULT);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String url = "http://vaclases.netsti.com/password/" + conf.getCedulaPadre() + "?old=" + old_passUrl.replace("\n", "").replace(" ", "") + "&new="
                            + new_passUrl.replace("\n", "").replace(" ", "");

                    JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            parser(response);

                        }
                    }, new Response.ErrorListener() {
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
                            15000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                    requestQueue.add(req);

                } else {
                    dialog.cancel();
                    Toast.makeText(getContext(), "La contraseñas son distintas", Toast.LENGTH_SHORT).show();
                }
            }
        });


        builder.setView(v);

        return builder.create();
    }

    public void parser(JSONObject response) {
        try {
            if (response.getString("status").equals("exito")) {
                JSONArray msg = response.getJSONArray("mensajes");
                Toast.makeText(getContext(), String.valueOf(msg.get(0)), Toast.LENGTH_LONG).show();
                dialog.cancel();
                EliminaPreferencias();
            } else if (response.getString("status").equals("error")) {
                JSONArray msg = response.getJSONArray("mensajes");
                dialog.cancel();
                Toast.makeText(getContext(), String.valueOf(msg.get(0)), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void EliminaPreferencias() {

        SharedPreferences prefsEventos = getActivity().getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorEvetos = prefsEventos.edit();

        SharedPreferences prefsMensajes = getActivity().getSharedPreferences("Mensajes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorMensajes = prefsMensajes.edit();

        SharedPreferences prefsValidacion = getActivity().getSharedPreferences("Validacion", Context.MODE_PRIVATE);
        SharedPreferences.Editor editosValidacion = prefsValidacion.edit();

        SharedPreferences prefsCuenta = getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorCuenta = prefsCuenta.edit();

        SharedPreferences prefsAlumno = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorAlumno = prefsAlumno.edit();

        editorEvetos.clear();
        editorEvetos.apply();

        editorMensajes.clear();
        editorMensajes.apply();

        editosValidacion.clear();
        editosValidacion.apply();

        editorCuenta.clear();
        editorCuenta.apply();

        editorAlumno.clear();
        editorAlumno.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        getActivity().finish();

        logout();
    }

    public void logout() {
        String url = Const.ip+"logout";
        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Session", "Session Close");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(req);
    }

}

