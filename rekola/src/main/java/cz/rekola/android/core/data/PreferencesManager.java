package cz.rekola.android.core.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

	public static final String PREFS_NAME = "MyPreferences";
	public static final String PREF_USERNAME = "Username";
	public static final String PREF_PASSWORD = "Password"; // TODO: Encrypt or do not save!!!

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
		return settings.getString(PREF_PASSWORD, null);
	}

	public void setPassword(String password) {
		setStringPref(PREF_PASSWORD, password);
	}

	private void setStringPref(String pref, String data) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(pref, data);
		editor.commit();
	}
}
