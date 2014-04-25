package cz.rekola.android.core;

import android.content.Context;
import android.content.SharedPreferences;

import cz.rekola.android.api.model.Token;

public class PreferencesManager {

	public static final String PREFS_NAME = "MyPreferences";
	public static final String PREF_USERNAME = "Username";

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

	public Token getToken() {
		return Token.restoreState(settings);
	}

	public void setPersistentObject(PreferencePersistenty persistentObject) {
		SharedPreferences.Editor editor = settings.edit();
		persistentObject.saveState(editor);
		editor.commit();
	}

	private void setStringPref(String pref, String data) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(pref, data);
		editor.commit();
	}
}
