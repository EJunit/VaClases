package hn.uth.hackaton.Validacion;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hn.uth.hackaton.Const;
import hn.uth.hackaton.MainActivity;
import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class ValidacionProblemasDialog extends DialogFragment {

    Typeface Roboto_Light;
    Typeface robotoSlab_bold;
    JSONObject obj = new JSONObject();
    LinearLayout r;
    private Preferencias conf;

    public ValidacionProblemasDialog() {

    }

    private String loadIdValidacion() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("id_val", "id_val");
    }

    private String loadFechaProblema() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("fechaP", " ");
    }

    private String loadIdentidadAlumno() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("alumno", Context.MODE_PRIVATE);
        return prefs.getString("identidad_alumno", " ");
    }

    private String loadEncuesta() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("MiCuenta", Context.MODE_PRIVATE);
        return prefs.getString("preguntas", "no hay");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    @SuppressLint("SetTextI18n")
    public AlertDialog createLoginDialogo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_validacion_problemas, null);

        conf = new Preferencias(getContext());

        r = (LinearLayout) v.findViewById(R.id.LinearEncuesta);
        try {
            busquedaDatos();
        } catch (Exception ignored) {
        }

        robotoSlab_bold = Typeface.createFromAsset(v.getContext().getAssets(), "fonts/RobotoSlab-Bold.ttf");
        Roboto_Light = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        Button btnCancelar = (Button) v.findViewById(R.id.btnVal_cance_pro);
        Button btnValidar = (Button) v.findViewById(R.id.btnVal_val_pro);
        TextView titulo_dialog = (TextView) v.findViewById(R.id.txtTitulo_dialog_val_pro);
        titulo_dialog.setTypeface(robotoSlab_bold);

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnValidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext()).setTitle("Confirmación")
                        .setMessage("¿Deséas confirmar tus respuestas? No podras cambiarlas luego, recuerda que las casillas" +
                                "que no marques, se tomaran como un 'No'.")
                        .setIcon(R.drawable.info_dialog)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String respuestas = "";
                                for (int a = 1; a <= obj.length(); a++) {
                                    try {
                                        String respuesta = String.valueOf(obj.get(String.valueOf(a)));
                                        respuestas = respuestas + String.valueOf(a) + "," + respuesta + "," + loadFechaProblema() + ";";
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                SaveValidacion(respuestas);
                            }
                        })//fin del positive clic
                        .setNegativeButton(android.R.string.no, null).show();
            }

        });

        builder.setView(v);

        return builder.create();
    }

    private void busquedaDatos() {

        try {
            JSONObject encuesta = new JSONObject(loadEncuesta());
            parser(encuesta, r);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void openProfile() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
        getActivity().finish();
    }

    // public void SaveValidacion(String respuesta, String fecha, String index) {
    public void SaveValidacion(String respuestas) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", "Guardando...");
        dialog.show();
        String token = conf.getTokken().replace(" ", "");

        String url = Const.ip + "api/confirma?token=" + token.replace("\n", "") +
                "&validacion_id=" + loadIdValidacion() + "&alumno_id=" + loadIdentidadAlumno() + "&type=2" +
                "&respuestas=" + respuestas;

        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String r = response.getString("status");

                    switch (r) {
                        case "exito":
                            dialog.hide();
                            Toast.makeText(getContext(), "Respuestas Ingresadas, gracias por el aporte", Toast.LENGTH_SHORT).show();
                            dismiss();
                            openProfile();
                            break;
                        default:
                            dialog.hide();
                            Toast.makeText(getContext(), "error al confirmar", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error al conectarse con el servidor, Intente de nuevo", Toast.LENGTH_SHORT).show();
                //  dialog.cancel();
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
    }//fin de la funcion save

    @SuppressLint("SetTextI18n")
    public void parser(JSONObject response, LinearLayout r) {

        String id_valor;
        int i;

        final RadioButton[] rb = new RadioButton[response.length()];

        for (int a = 1; a <= response.length(); a++) {

            try {
                id_valor = String.valueOf(a);
                JSONObject p = response.getJSONObject(String.valueOf(a));//esta seria la pregunta en el texto
                TextView tx = new TextView(getContext());//Creamos el TextField de la pregunta
                tx.setPadding(20, 20, 10, 10);
                tx.setTextColor(ContextCompat.getColor(getContext(), R.color.letraPrimary));
                tx.setTypeface(robotoSlab_bold);

                tx.setText(p.getString("mensaje"));

                RadioGroup rg = new RadioGroup(getContext()); //Creamos el RadioGroup
                rg.setOrientation(RadioGroup.HORIZONTAL);
                rg.setPadding(25, 10, 10, 5);

                RadioGroup contrasGrp = new RadioGroup(getContext());
                contrasGrp.setOrientation(RadioGroup.VERTICAL);

                for (i = 0; i < 2; i++) {
                    rb[i] = new RadioButton(getContext());
                    rg.addView(rb[i]);

                    switch (i) {
                        case 0: {
                            rb[i].setText("SI");
                            rb[i].setTypeface(Roboto_Light);
                            rb[i].setTextColor(ContextCompat.getColor(getContext(), R.color.letraPrimary));
                            final String finalId_valor = id_valor;
                            rb[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    //respuesta positiva a la pregunta
                                    try {
                                        obj.put(finalId_valor, "1");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        }
                        default: {
                            rb[i].setText("NO");
                            rb[i].setTypeface(Roboto_Light);
                            final String finalId_valor = id_valor;
                            rb[i].setTextColor(ContextCompat.getColor(getContext(), R.color.letraPrimary));
                            rb[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    //respuesta negativa a la pregunta
                                    try {
                                        obj.put(finalId_valor, "0");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        }
                    }

                }

                r.addView(tx);
                r.addView(rg);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

