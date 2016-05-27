package hn.uth.hackaton.Validacion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hn.uth.hackaton.Const;
import hn.uth.hackaton.Login.LoginActivity;
import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class FragmentoValidacion extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView reciclador;
    private ImageView imgVaciov;
    private SwipeRefreshLayout refreshLayout;
    List<Validacion> itemsAux;
    RequestQueue queue;
    Preferencias conf;
    private SharedPreferences prefsValidacion;
    private SharedPreferences.Editor editosValidacion;
    private NewAdapterValidaciones adaptador;

    private String loadNombre() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("nombre_alumno", " ");
    }

    private String loadIdentidadAlumno() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("identidad_alumno", " ");
    }

    private String loadEscuela() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("nombre_escuela", " ");
    }

    public FragmentoValidacion() {
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragmento_validacion, container, false);
        Typeface roboto_condensed = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Condensed.ttf");

        prefsValidacion = this.getActivity().getSharedPreferences("Validacion", Context.MODE_PRIVATE);
        editosValidacion = prefsValidacion.edit();
        conf = new Preferencias(getContext());

        ImageView img = (ImageView) view.findViewById(R.id.imgAlumno);
        TextView nombreAlumno = (TextView) view.findViewById(R.id.txtNombreAlumnoValidacion);

        ImageView img2 = (ImageView) view.findViewById(R.id.imgAlumno2);
        TextView nombreAlumno2 = (TextView) view.findViewById(R.id.txtNombreEscuelaVal);

        img.setImageResource(R.drawable.alumno);
        nombreAlumno.setText(loadNombre());
        nombreAlumno.setTypeface(roboto_condensed);

        img2.setImageResource(R.drawable.escuela);
        nombreAlumno2.setText(loadEscuela());
        nombreAlumno2.setTypeface(roboto_condensed);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) this.getActivity().findViewById(R.id.fabValidaciones);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    busquedaDatos(getView());
                } catch (Exception ignored) {
                }
            }
        });

        reciclador = (RecyclerView) this.getActivity().findViewById(R.id.recicladorValidacion);
        imgVaciov = (ImageView) this.getActivity().findViewById(R.id.vaciov);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        reciclador.setLayoutManager(layoutManager);
        // Obtener el refreshLayout
        refreshLayout = (SwipeRefreshLayout) this.getActivity().findViewById(R.id.swipeRefreshValidacion);

        refreshLayout.setOnRefreshListener(this);

        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                try {
                    busquedaDatos(getView());
                } catch (Exception ignored) {
                }
            }
        });

        queue = Volley.newRequestQueue(getActivity());
    }

    private void busquedaDatos(View view) {
        imgVaciov.setBackgroundResource(R.drawable.exito);

        refreshLayout.setRefreshing(true);

        String token = conf.getTokken().replace("\n", "");

        String url = Const.ip+"api/validaciones?token=" + token.replace(" ", "") + "&alumno_id=" + loadIdentidadAlumno();

        if (isOnline()) {
            JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    editosValidacion.putString("f2", response.toString());
                    editosValidacion.apply();
                    parser(response);
                    refreshLayout.setRefreshing(false);
                }
            }, new Response.ErrorListener() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void onErrorResponse(VolleyError error) {
                    refreshLayout.setRefreshing(false);
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

            queue.add(req);
        } else {
            String strJson = prefsValidacion.getString("f2", "NO");
            if (!strJson.equals("NO")) {
                JSONObject jsonData = null;
                try {
                    jsonData = new JSONObject(strJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                parser(jsonData);
            }
            refreshLayout.setRefreshing(false);
            Snackbar.make(view, "No hay conexión :(", Snackbar.LENGTH_LONG).show();
        }
    }

    public List<Validacion> parser(JSONObject response) {
        itemsAux = new ArrayList<>();

        try {
            if (response.getString("status").equals("exito")) {

                JSONArray dataValidaciones = response.getJSONArray("data");
                String control;


                for (int a = 0; a < dataValidaciones.length(); a++) {

                    JSONObject infoJosn = dataValidaciones.getJSONObject(a);

                    try {
                        control = infoJosn.getString("tipo");
                        if (control.equals("1")) {
                            itemsAux.add(new Validacion(infoJosn.getString("fecha_inicio"), infoJosn.getString("fecha_fin"), infoJosn.getString("dias"),
                                    Integer.valueOf(infoJosn.getString("tipo")), infoJosn.getString("id")));
                        } else {
                            itemsAux.add(new Validacion(Integer.valueOf(infoJosn.getString("tipo")), infoJosn.getString("fecha_inicio"),
                                    infoJosn.getString("id"), infoJosn.getString("mensajes")));
                        }

                        adaptador = new NewAdapterValidaciones(itemsAux, getActivity());


                    } catch (JSONException ignored) {
                    }
                }//termina el for

                if (itemsAux.size() <= 0) {
                    imgVaciov.setBackgroundResource(R.drawable.vacio);
                    imgVaciov.setPadding(25, 25, 25, 25);
                }

                reciclador.setAdapter(adaptador);
            } else if (response.getString("status").equals("vacio")) {
                imgVaciov.setBackgroundResource(R.drawable.vacio);
                imgVaciov.setPadding(25, 25, 25, 25);
            } else if (response.getString("status").equals("error")) {
                Toast.makeText(getContext(), "Ha caducado su sesión, Debe hacer login nuevamente.", Toast.LENGTH_LONG).show();
                EliminaPreferencias();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return itemsAux;
    }

    @Override
    public void onRefresh() {

        busquedaDatos(getView());
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

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

        logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        getActivity().finish();
    }


    public void logout() {
        String url = Const.ip+"logout";
        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parser2(response);
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

    public void parser2(JSONObject response) {
        try {
            if (response.getString("status").equals("exito")) {
                JSONArray dataMensaje = response.getJSONArray("mensajes");

                Toast.makeText(getContext(), dataMensaje.get(0).toString(), Toast.LENGTH_SHORT).show();

            } else if (response.getString("status").equals("error")) {
                JSONArray dataMensaje = response.getJSONArray("mensajes");

                Toast.makeText(getContext(), dataMensaje.get(0).toString(), Toast.LENGTH_SHORT).show();


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
