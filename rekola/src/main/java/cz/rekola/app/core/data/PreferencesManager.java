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
    public static final String PREF_PASSWORD = "Password";

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

    public String getPassword() {
        String base64 = settings.getString(PREF_PASSWORD, null);
        if (base64 == null)
            return null;
        try {
            return new String(Base64.decode(base64, Base64.DEFAULT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setPassword(String password) {
        if (password == null) {
            setStringPref(PREF_PASSWORD, password);
            return;
        }
        try {
            setStringPref(PREF_PASSWORD, Base64.encodeToString(password.getBytes("UTF-8"), Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
        }
    }

    private void setStringPref(String pref, String data) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(pref, data);
        editor.commit();
    }
}
