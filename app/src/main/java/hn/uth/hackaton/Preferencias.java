package hn.uth.hackaton;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferencias {

    private final String KEY_USER = "username";
    private final String KEY_CEDULA = "cedulaPadre";
    public static final String KEY_FLAG = "flags";
    public static final String KEY_COOKIES = "cookie";

    private Context mContext;

    public Preferencias(Context context) {
        mContext = context;
    }

    private SharedPreferences getSettingsCuenta() {
        String SHARED_PREFS_FILE = "MiCuenta";
        return mContext.getSharedPreferences(SHARED_PREFS_FILE, 0);
    }

    private SharedPreferences getSettingsTuto() {
        String SHARED_PREFS_FILE = "tutorial";
        return mContext.getSharedPreferences(SHARED_PREFS_FILE, 0);
    }

    private SharedPreferences getSettingsCookies() {
        String SHARED_PREFS_FILE = "Cookies";
        return mContext.getSharedPreferences(SHARED_PREFS_FILE, 0);
    }

    public String getEstadoTutorial() {
        return getSettingsTuto().getString(KEY_FLAG, "NO");
    }

    public void setFlagTuto(String flag) {
        SharedPreferences.Editor editor = getSettingsTuto().edit();
        editor.putString(KEY_FLAG, flag);
        editor.apply();
    }

    public String getTokken() {
        return getSettingsCuenta().getString(KEY_USER, "username");
    }

    public void setTokken(String user) {
        SharedPreferences.Editor editor = getSettingsCuenta().edit();
        editor.putString(KEY_USER, user);
        editor.apply();
    }

    public String getCedulaPadre() {
        return getSettingsCuenta().getString(KEY_CEDULA, "padre");
    }

    public void setCedulaPadre(String user) {
        SharedPreferences.Editor editor = getSettingsCuenta().edit();
        editor.putString(KEY_CEDULA, user);
        editor.apply();
    }


    public String getCookie() {
        return getSettingsCookies().getString(KEY_COOKIES, "no guardo la cookie");
    }

}
