package cz.rekola.app.core.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Simplify to using shared preference
 */

public class PreferencesManager {

    public static final String PREFS_NAME = "MyPreferences";
    public static final String PREF_USERNAME = "Username";
    public static final String PREF_TOKEN = "Token";

    private SharedPreferences settings;

    public PreferencesManager(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public String getUsername() {
        return settings.getString(PREF_USERNAME, null);
    }

    public void setUsername(String username) {
        setStringPref(PREF_USERNAME, username);
    }

    public String getToken() {
        return settings.getString(PREF_TOKEN, "");
    }

    public void setToken(String token) {
        setStringPref(PREF_TOKEN, token);
    }

    private void setStringPref(String pref, String data) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(pref, data);
        editor.apply();
    }
}
