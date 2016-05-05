package hn.uth.hackaton.Eventos;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class FragmentoEventos extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView reciclador;
    private AdaptadorEventos adaptador;
    private ImageView imgVacioe;
    private RequestQueue queue;
    private SwipeRefreshLayout refreshLayoutE;
    List<Eventos> itemsAux;
    ArrayList<Eventos> data;
    private Preferencias conf;
    private  SharedPreferences prefsEventos;
    private SharedPreferences.Editor editorEvetos;
    private String esc_selec;

    private String loadNombre() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno",Context.MODE_PRIVATE);
        return prefs.getString("nombre_alumno", " ");
    }
    private String loadPadreSelect() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("padre_select", " ");
    }
    private String loadEscuela() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno",Context.MODE_PRIVATE);
        return prefs.getString("nombre_escuela", " ");
    }
    public FragmentoEventos() {
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_eventos, container, false);
        Typeface roboto_condensed = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Condensed.ttf");

        prefsEventos = this.getActivity().getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        editorEvetos = prefsEventos.edit();
        esc_selec = loadEscuela();

        conf = new Preferencias(getContext());

        ImageView img_alumno = (ImageView) view.findViewById(R.id.imgAlumno_eventos);
        TextView nombreAlumno = (TextView) view.findViewById(R.id.txtNombreAlumno_eventos);

        ImageView img_escuela = (ImageView) view.findViewById(R.id.imgEcuela_eventos);
        TextView nombreEscuela_evento = (TextView) view.findViewById(R.id.txtEscuela_eventos);

        img_alumno.setImageResource(R.mipmap.alumno);
        nombreAlumno.setText(loadNombre());
        nombreAlumno.setTypeface(roboto_condensed);

        img_escuela.setImageResource(R.mipmap.escuela);
        nombreEscuela_evento.setText(loadEscuela());
        nombreEscuela_evento.setTypeface(roboto_condensed);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) this.getActivity().findViewById(R.id.fabEventos);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                busquedaDatos(getView());
            }
        });

        reciclador = (RecyclerView) this.getActivity().findViewById(R.id.recicladorEventos);
        imgVacioe = (ImageView) this.getActivity().findViewById(R.id.vacioe);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        reciclador.setLayoutManager(layoutManager);

        // Obtener el refreshLayout
        refreshLayoutE = (SwipeRefreshLayout) this.getActivity().findViewById(R.id.swipeRefreshEventos);

        refreshLayoutE.setOnRefreshListener(this);

        refreshLayoutE.post(new Runnable() {
            @Override
            public void run() {
                refreshLayoutE.setRefreshing(true);
                try {
                    busquedaDatos(getView());
                } catch (Exception ignored) {
                }
            }
        });

        queue = Volley.newRequestQueue(getActivity());

    }

    private void busquedaDatos(View view) {

        imgVacioe.setBackgroundResource(R.drawable.exito);
        refreshLayoutE.setRefreshing(true);

        String url = "http://vaclases.netsti.com/api/eventos?token="+conf.getTokken();
        if (isOnline()) {
            JsonObjectRequest req2 = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                        editorEvetos.putString("f", response.toString());
                        editorEvetos.apply();
                        data = new ArrayList<>();
                        data = (ArrayList<Eventos>) parser(response);
                        refreshLayoutE.setRefreshing(false);
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    refreshLayoutE.setRefreshing(false);
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

            req2.setRetryPolicy(new DefaultRetryPolicy(
                    12000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(req2);
        }else{ //si no hay conexion

            String strJson = prefsEventos.getString("f", "NO");
            if (!strJson.equals("NO")){
                JSONObject jsonData = null;
                try {
                    jsonData = new JSONObject(strJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                parser(jsonData);
            }
            refreshLayoutE.setRefreshing(false);

            Snackbar.make(view, "No hay conexión :(", Snackbar.LENGTH_LONG).show();
        }
    }

    public List<Eventos> parser (JSONObject response){
        // Inicializar la fuente de datos temporal
        itemsAux = new ArrayList<>();

        try {
            if (response.getString("status").equals("exito")) {

                JSONArray dataEventos = response.getJSONArray("data");

                    for (int a = 0; a < dataEventos.length(); a++) {

                        Eventos info = new Eventos();
//
                        JSONObject infoJosn = dataEventos.getJSONObject(a);
                        //JSONObject infoCentro = infoJosn.getJSONObject("centro_educativo");
                        try {

                            if(infoJosn.getString("type_name").equals(esc_selec)) {

                                info.setTitulo_evento(infoJosn.getString("evento_nombre"));
                                info.setDescripcion_evento(infoJosn.getString("evento_descripcion"));
                                info.setFecha(infoJosn.getString("fecha_evento"));
                                info.setIdEvento(infoJosn.getString("id"));

                                itemsAux.add(info);

                                adaptador = new AdaptadorEventos(itemsAux, getActivity());

                            }
                        } catch (JSONException ignored) {
                        }
                    }

                if(itemsAux.size() <= 0){
                    imgVacioe.setBackgroundResource(R.drawable.vacio);
                    imgVacioe.setPadding(25, 25, 25, 25);
                }
                reciclador.setAdapter(adaptador);
            }   else if(response.getString("status").equals("vacio")){
                imgVacioe.setBackgroundResource(R.drawable.vacio);
                imgVacioe.setPadding(25,25,25,25);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return itemsAux;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRefresh() {
        try {
            adaptador.clear();
        }catch (NullPointerException e){
            refreshLayoutE.setRefreshing(false);
            Snackbar.make(getView(), "No tienes eventos pendientes :)", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        busquedaDatos(getView());
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }
}
