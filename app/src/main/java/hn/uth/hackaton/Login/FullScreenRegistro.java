package hn.uth.hackaton.Login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import hn.uth.hackaton.Const;
import hn.uth.hackaton.R;

public class FullScreenRegistro extends DialogFragment {

    private EditText edtUsername;
    private EditText edtTelefono;
    private EditText edtTelefono2;
    ProgressDialog dialog;

    public FullScreenRegistro() {
        // Constructor publico vacio
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Obtener instancia de la action bar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            // Habilitar el Up Button
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Cambiar icono del Up Button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registro_dialog, container, false);

        Typeface Roboto_Light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        edtUsername = (EditText) view.findViewById(R.id.edtcedula);
        edtUsername.setTypeface(Roboto_Light);
        edtTelefono = (EditText) view.findViewById(R.id.edttel);
        edtTelefono.setTypeface(Roboto_Light);
        edtTelefono2 = (EditText) view.findViewById(R.id.edttel2);
        edtTelefono2.setTypeface(Roboto_Light);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fullscreen_dialog, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                break;
            case R.id.action_save:
                try {
                    consulta();
                } catch (Exception ignored) {
                    //consulta();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    public void consulta() {

        final String username = edtUsername.getText().toString().trim();
        final String usertel = edtTelefono.getText().toString().trim();
        String usertel2 = edtTelefono2.getText().toString().trim();

        if (username.isEmpty() || usertel.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
        } else if (!Objects.equals(usertel, usertel2)) {
            Toast.makeText(getContext(), "Los numeros telefonicos no son iguales", Toast.LENGTH_SHORT).show();
        } else {

            dialog = ProgressDialog.show(getContext(), "", "Cargando contenido");
            String url = Const.ip+"registrar/" + username + "/" + usertel;

            JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parser(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.cancel();
                    Log.i("Error registro", error.getMessage());
                    Toast.makeText(getContext(), "Error al conectarse al servidor, intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            requestQueue.add(req);
        }
    }

    public void parser(JSONObject response) {
        try {
            if (response.getString("status").equals("registrado")) {
                dialog.cancel();
                FragmentActivity activity = (FragmentActivity) (getContext());
                FragmentManager fm = activity.getSupportFragmentManager();
                RegistroOkDialog alertDialog = new RegistroOkDialog();
                alertDialog.show(fm, "RegistroOkDialog");

            } else if (response.getString("status").equals("no_existe")) {
                dialog.cancel();
                Toast.makeText(getContext(), "Su numero de identidad no se encuentra registrada en SACE",
                        Toast.LENGTH_SHORT).show();
            } else if (response.getString("status").equals("no_hijos")) {
                dialog.cancel();
                Toast.makeText(getContext(), "No se encontraron hijos registrados a su nombre, " +
                                "Comuniquese con el encargado de su centro educativo y actualice sus datos.",
                        Toast.LENGTH_LONG).show();
            } else if (response.getString("status").equals("existe")) {
                dialog.cancel();
                Toast.makeText(getContext(), "El usuario ya existe, no requiere registro",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            dialog.cancel();
        }
    }

}
