package hn.uth.hackaton.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import hn.uth.hackaton.Const;
import hn.uth.hackaton.MainActivity;
import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;
import hn.uth.hackaton.tutorial.IntroActivity;

public class ControlActivity extends AppCompatActivity {
    Preferencias pref = new Preferencias(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        a();
    }

    public void a() {
        String flag = pref.getEstadoTutorial();// prefsTutorial.getString("flag", "NO");

        if (flag.equals("1")) {

            String token = pref.getTokken();// prefs.getString("username", "username");
            String username = pref.getCedulaPadre();

            String url = Const.ip + "login/" + username + "?password=" + token;

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        String control = response.getString("status");

                        switch (control) {
                            case "listo":
                            case "exito":
                                openProfile();
                                break;
                            default:
                                openLogin();
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
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
                    headers.put("Cookie", pref.getCookie());
                    return headers;
                }

            };

            req.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(req);

        } else {//fin del if
            Intent intent = new Intent(this, IntroActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            this.finish();
        }
    }

    private void openProfile() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    private void setCookie(String cookie) {
        SharedPreferences prefs = this.getSharedPreferences("Cookies", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("cookie", cookie);
        edit.apply();
    }
}
