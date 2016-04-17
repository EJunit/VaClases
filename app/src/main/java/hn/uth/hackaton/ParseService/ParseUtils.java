package hn.uth.hackaton.ParseService;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class ParseUtils {

    private static String TAG = ParseUtils.class.getSimpleName();

    public static void verifyParseConfiguration(Context context) {
        if (TextUtils.isEmpty(AppConfig.PARSE_APPLICATION_ID) || TextUtils.isEmpty(AppConfig.PARSE_CLIENT_KEY)) {
            Toast.makeText(context, "Por favor configure su Parse Application ID y Client Key en AppConfig.java", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }
    }

    public static void registerParse(Context context) {
        // Inicializacion de la libreria de Parse
        Parse.initialize(context, AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static void registroChannels(ArrayList<String> channels) {
        // Inicializacion de la libreria de Parse
        for (int a=0;a<channels.size();a++){
            ParsePush.subscribeInBackground(channels.get(a));
        }
    }

    public static void removeChannels(){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.remove("channels");
        installation.saveEventually(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {

            }
        });
    }

    public static void subscribeWithEmail(String email) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        installation.put("email", email);

        installation.saveInBackground();
    }
}
