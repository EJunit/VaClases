package hn.uth.hackaton.Mensajes;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hn.uth.hackaton.Const;
import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class FragmentoMensaje extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView reciclador;
    private ImageView imgVacio;
    private AdaptadorMensajes adaptador;
    private RequestQueue queue;
    private SwipeRefreshLayout refreshLayoutM;
    List<Mensajes> itemsAux;
    private Preferencias conf;
    TextView nombreAlumno;
    private SharedPreferences prefsMensajes;
    private SharedPreferences.Editor editorMensajes;
    ArrayList<Mensajes> data;
    private String fechaActual;
    Calendar calendar;
    Date fechaActual2;
    Date fechaMensaje2;

    private String loadNombre() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("nombre_alumno", " ");
    }

    private String loadEscuela() {
        SharedPreferences prefs = getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("nombre_escuela", " ");
    }

    public FragmentoMensaje() {
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragmento_mensaje, container, false);
        conf = new Preferencias(getContext());

        prefsMensajes = this.getActivity().getSharedPreferences("Mensajes", Context.MODE_PRIVATE);
        editorMensajes = prefsMensajes.edit();

        Typeface roboto_condensed = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Condensed.ttf");

        ImageView img = (ImageView) view.findViewById(R.id.imgAlumno);
        nombreAlumno = (TextView) view.findViewById(R.id.txtNombreAlumno);

        ImageView img2 = (ImageView) view.findViewById(R.id.imgAlumno2);
        TextView nombreAlumno2 = (TextView) view.findViewById(R.id.txtNombreAlumno2);

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

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        Date date = cal.getTime();

        fechaActual = sdf.format(date);

        FloatingActionButton fab = (FloatingActionButton) this.getActivity().findViewById(R.id.fabMensajes);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                busquedaDatos(getView());
            }
        });

        reciclador = (RecyclerView) this.getActivity().findViewById(R.id.recicladorMensajes);
        imgVacio = (ImageView) this.getActivity().findViewById(R.id.vacio);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        reciclador.setLayoutManager(layoutManager);
        // Obtener el refreshLayout
        refreshLayoutM = (SwipeRefreshLayout) this.getActivity().findViewById(R.id.swipeRefreshMensajes);

        refreshLayoutM.setOnRefreshListener(this);

        refreshLayoutM.post(new Runnable() {
            @Override
            public void run() {
                refreshLayoutM.setRefreshing(true);
                try {
                    busquedaDatos(getView());
                } catch (Exception ignored) {
                }
            }
        });

        queue = Volley.newRequestQueue(getActivity());
    }

    private void busquedaDatos(View view) {
        imgVacio.setBackgroundResource(R.drawable.exito);

        refreshLayoutM.setRefreshing(true);

        String url = Const.ip+"api/mensajes?token=" + conf.getTokken();

        if (isOnline()) {

            data = new ArrayList<>();

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    editorMensajes.putString("f3", response.toString());
                    editorMensajes.apply();
                    data = (ArrayList<Mensajes>) parser(response);
                    refreshLayoutM.setRefreshing(false);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    refreshLayoutM.setRefreshing(false);
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

        } else {//evaluacion de la conectividad
            String strJson = prefsMensajes.getString("f3", "NO");
            if (!strJson.equals("NO")) {
                JSONObject jsonData = null;
                try {
                    jsonData = new JSONObject(strJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                parser(jsonData);
            }
            refreshLayoutM.setRefreshing(false);

            Snackbar.make(view, "No hay conexión :(", Snackbar.LENGTH_LONG).show();
        }
    }

    public List<Mensajes> parser(JSONObject response) {
        // Inicializar la fuente de datos temporal
        itemsAux = new ArrayList<>();

        try {
            if (response.getString("status").equals("exito")) {

                JSONArray dataMensajes = response.getJSONArray("data");

                for (int a = 0; a <= dataMensajes.length(); a++) {

                    Mensajes info = new Mensajes();
                    try {
                        JSONObject infoJosn = dataMensajes.getJSONObject(a);

                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String strFecha = fechaActual;
                        String srtFechaM = infoJosn.getString("fecha");
                        fechaActual2 = null;
                        fechaMensaje2 = null;
                        try {
                            fechaActual2 = formatoDelTexto.parse(strFecha);
                            fechaMensaje2 = formatoDelTexto.parse(srtFechaM);
                            calendar = Calendar.getInstance();
                            calendar.setTime(fechaMensaje2); // Configuramos la fecha que se recibe
                            calendar.add(Calendar.DAY_OF_YEAR, 5);
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }

                        assert fechaActual2 != null;
                        if (fechaActual2.before(calendar.getTime())) {

                            info.setTitulo(infoJosn.getString("titulo"));
                            info.setMensaje(infoJosn.getString("mensaje"));
                            info.setFecha(infoJosn.getString("fecha"));
                            info.setSend_type(infoJosn.getString("type_name"));

                            itemsAux.add(info);

                            adaptador = new AdaptadorMensajes(getActivity(), itemsAux);


                        }
                    } catch (JSONException ignored) {
                    }
                }

                if (itemsAux.size() <= 0) {
                    imgVacio.setBackgroundResource(R.drawable.vacio);
                    imgVacio.setPadding(25, 25, 25, 25);
                }

                reciclador.setAdapter(adaptador);
            } else if (response.getString("status").equals("vacio")) {
                imgVacio.setBackgroundResource(R.drawable.vacio);
                imgVacio.setPadding(25, 25, 25, 25);
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
        } catch (NullPointerException e) {
            refreshLayoutM.setRefreshing(false);
            Snackbar.make(getView(), "No tienes Mensajes pendientes :)", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        busquedaDatos(getView());
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
