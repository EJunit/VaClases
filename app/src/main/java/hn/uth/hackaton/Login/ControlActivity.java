package hn.uth.hackaton.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

            String username = pref.getTokken();// prefs.getString("username", "username");
            Log.i("tokken de preferenci", username);
            if (username.equals("username")) {
                openLogin();
            } else {
                openProfile();
            }
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
}
