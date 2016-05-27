package hn.uth.hackaton.Login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
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
import java.util.StringTokenizer;

import hn.uth.hackaton.Const;
import hn.uth.hackaton.MainActivity;
import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class LoginActivity extends AppCompatActivity {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    private EditText user;
    private EditText pass;
    private String url;
    private String username;
    ProgressDialog dialog;
    Preferencias conf;
    private String base64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        conf = new Preferencias(this);

        Typeface Roboto_Light = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
        //Typeface robotoSlab_bold = Typeface.createFromAsset(this.getAssets(), "fonts/RobotoSlab-Bold.ttf");

        user = (EditText) findViewById(R.id.edtuser);
        assert user != null;
        user.setTypeface(Roboto_Light);
        pass = (EditText) findViewById(R.id.edtpass);
        assert pass != null;
        pass.setTypeface(Roboto_Light);
       /* TextView r = (TextView) findViewById(R.id.textView3);
        r.setTypeface(Roboto_Light);*/

        Button logIn = (Button) findViewById(R.id.btnlogin);
        // Button btnRecuCuenta = (Button) findViewById(R.id.btnRecuperaCuenta);
        Button btnRegistrate = (Button) findViewById(R.id.btnregistro);

        assert logIn != null;
        logIn.setTypeface(Roboto_Light);
        //btnRecuCuenta.setTypeface(robotoSlab_bold);
        assert btnRegistrate != null;
        btnRegistrate.setTypeface(Roboto_Light);

        logIn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userLogin();
                    }
                }
        );

        findViewById(R.id.btnregistro).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
            }
        });

        /*findViewById(R.id.btnRecuperaCuenta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
            }
        });*/

    }

    private void userLogin() {

        username = user.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Debe completar los campos requeridos", Toast.LENGTH_LONG).show();
        } else {

            dialog = ProgressDialog.show(this, "", "Cargando contenido");

            try {

                String KEY = password + username;
                byte[] data = KEY.getBytes("UTF-8");
                conf.setCedulaPadre(username);
                base64 = Base64.encodeToString(data, Base64.DEFAULT);
                url = Const.ip+"login/" + username + "?password=" + base64;
                conf.setTokken(base64);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                dialog.cancel();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parser(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), "Ocurrio un problema de comunicacion, Intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                public Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    Map headers = response.headers;
                    Object cookie = headers.get("Set-Cookie");
                    StringTokenizer tokens = new StringTokenizer(cookie.toString(), ";");
                    String first = tokens.nextToken();
                    //guargar preferencia
                    setCookie(first);
                    return super.parseNetworkResponse(response);
                }

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

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(req);
        }
    }

    private void openProfile() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KEY_USERNAME, username);
        intent.putExtra(KEY_PASSWORD, base64);
        startActivity(intent);
        this.finish();
    }

    public void parser(JSONObject response) {

        if (response.length() > 0) {

            try {
                if (response.getString("status").equals("exito")) {

                    JSONObject encargado = response.getJSONObject("encargado");
                    setPadreSelect(encargado.getString("nombre"),encargado.getString("identidad"));
                    JSONArray infoAlumno = encargado.getJSONArray("alumnos");

                    for (int a = 0; a < infoAlumno.length(); a++) {

                        JSONObject infoJsonAlumno = infoAlumno.getJSONObject(a);
                        setNombreAlumno(infoJsonAlumno.getString("nombre"));
                        setIdentidadAlumno(infoJsonAlumno.getString("id"));

                        JSONObject infoJsonEscuela = infoJsonAlumno.getJSONObject("centro_educativo");
                        setNombreEscuela(infoJsonEscuela.getString("nombre"),infoJsonEscuela.getString("codigo"));

                        JSONObject infoJsonMuni = infoJsonEscuela.getJSONObject("municipio");

                        setNombreMunicipio(infoJsonMuni.getString("nombre"));

                        JSONObject infoJsonDepto = infoJsonEscuela.getJSONObject("departamento");
                        setNombreDepartamentoEscuela(infoJsonDepto.getString("nombre"));
                    }

                    openProfile();

                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), "Sesion Iniciada", Toast.LENGTH_SHORT).show();
                } else if (response.getString("status").equals("error")) {
                    JSONArray error = response.getJSONArray("mensajes");

                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), error.get(0).toString(), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.cancel();
            }

        }
    }

    private void setPadreSelect(String nombre, String id) {
        SharedPreferences prefs = this.getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("padre_select", nombre);
        edit.putString("padre_select_id", id);
        edit.apply();
    }

    private void setNombreAlumno(String nombre) {
        SharedPreferences prefs = this.getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("nombre_alumno", nombre);
        edit.apply();
    }

    private void setIdentidadAlumno(String nombre) {
        SharedPreferences prefs = this.getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("identidad_alumno", nombre);
        edit.apply();
    }

    private void setNombreDepartamentoEscuela(String depto) {
        SharedPreferences prefs = this.getSharedPreferences("departamento", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("depto", depto);
        edit.apply();
    }

    private void setNombreMunicipio(String municipio) {
        SharedPreferences prefs = this.getSharedPreferences("municipio", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("muni", municipio);
        edit.apply();
    }

    private void setNombreEscuela(String nombre, String codigo) {
        SharedPreferences prefs = this.getSharedPreferences("alumno", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("nombre_escuela", nombre);
        edit.putString("codigo_escuela", codigo);
        edit.apply();
    }

    private void setCookie(String cookie) {
        SharedPreferences prefs = this.getSharedPreferences("Cookies", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("cookie", cookie);
        edit.apply();
    }

}
