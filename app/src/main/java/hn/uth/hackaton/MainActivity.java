package hn.uth.hackaton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import hn.uth.hackaton.Login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    Preferencias conf;

    private String loadUser() {
        SharedPreferences prefs = this.getSharedPreferences("MiCuenta",Context.MODE_PRIVATE);
        return prefs.getString("username", "numCuenta");
    }

    private String loadEscuela() {
        SharedPreferences prefs = this.getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("nombre_escuela", " ");
    }
    private String loadDepto() {
        SharedPreferences prefs = this.getSharedPreferences("departamento", Context.MODE_PRIVATE);
        return prefs.getString("depto", " ");
    }
    private String loadMuni() {
        SharedPreferences prefs = this.getSharedPreferences("municipio", Context.MODE_PRIVATE);
        return prefs.getString("muni", " ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OneSignal.startInit(this).setAutoPromptLocation(true).init();
        conf = new Preferencias(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setLogo(R.drawable.logo_toolbar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);

        ArrayList<String> channels = new ArrayList<>();
        String escuela = loadEscuela().replace(" ","").replace("é","e").replace("á","a").replace("í","i").replace("ó","o").replace("ú","u");
        String depto = loadDepto().replace(" ","").replace("é","e").replace("á","a").replace("í","i").replace("ó","o").replace("ú","u");
        String muni = loadMuni().replace(" ","").replace("é","e").replace("á", "a").replace("í","i").replace("ó","o").replace("ú","u");

        channels.add(escuela);
        channels.add(depto);
        channels.add(muni);
        channels.add("Honduras");

        //ParseUtils.registroChannels(channels);
        //registro de tags
        JSONObject tags = new JSONObject();
        try {
            tags.put("escuela", escuela);
            tags.put("departamento", depto);
            tags.put("municipio", muni);
            tags.put("pais", "Honduras");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OneSignal.sendTags(tags);
        //fin de registro de tags de OneSignal

        //ParseUtils.verifyParseConfiguration(this);

        String email = loadUser().replace("\n","").replace(" ","")+"@vaclases.com";

        Fragment fragmentoGenerico = new FragmentoTabs();
        fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.contenedor_inicio, fragmentoGenerico)
                .commit();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       // client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cambio_pass) {
            FragmentActivity activity = this;
            FragmentManager fm = activity.getSupportFragmentManager();
            CambioPassDialog alertDialog = new CambioPassDialog();
            alertDialog.show(fm, "CambioPassDialog");
        } else if (id == R.id.action_salir) {
            if(isOnline()){
                EliminaPreferencias();
            }else{
             Toast.makeText(this,"Es necesaria una conexión a internet para cerrar sesión",Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.hijo) {
            FragmentManager fm = this.getSupportFragmentManager();
            SeleccionHijoDialog alertDialog = new SeleccionHijoDialog();
            alertDialog.show(fm, "SeleccionHijoDialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout(){
        String url ="http://vaclases.netsti.com/logout";
        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parser(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    public void parser(JSONObject response){
        try {
            if (response.getString("status").equals("exito")){
                JSONArray dataMensaje = response.getJSONArray("mensajes");

                Toast.makeText(getApplicationContext(), dataMensaje.get(0).toString(),Toast.LENGTH_SHORT).show();

            }else if (response.getString("status").equals("error")){
                JSONArray dataMensaje = response.getJSONArray("mensajes");

                Toast.makeText(getApplicationContext(), dataMensaje.get(0).toString(),Toast.LENGTH_SHORT).show();


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void EliminaPreferencias(){

        SharedPreferences prefsEventos = this.getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorEvetos = prefsEventos.edit();

         SharedPreferences prefsMensajes = this.getSharedPreferences("Mensajes", Context.MODE_PRIVATE);
         SharedPreferences.Editor editorMensajes = prefsMensajes.edit();

        SharedPreferences prefsValidacion = this.getSharedPreferences("Validacion", Context.MODE_PRIVATE);
        SharedPreferences.Editor editosValidacion = prefsValidacion.edit();

        SharedPreferences prefsCuenta= this.getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorCuenta = prefsCuenta.edit();

        SharedPreferences prefsAlumno= this.getSharedPreferences("alumno", Context.MODE_PRIVATE);
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

        Collection<String> tempList = new ArrayList<>();
        tempList.add("escuela");
        tempList.add("departamento");
        tempList.add("municipio");
        OneSignal.deleteTags(tempList);

        logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        finish();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

}
