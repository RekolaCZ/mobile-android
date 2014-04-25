package cz.rekola.android.api.model;

import android.content.SharedPreferences;

import cz.rekola.android.core.PreferencePersistenty;

public class Token implements PreferencePersistenty {

	public static final String PREF_API_KEY = "TokenApiKey";

	public String apiKey; // TODO: Is possible get&set with retrofit?

	public Token() {}
	public Token(String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	public void saveState(SharedPreferences.Editor editor) {
		editor.putString(PREF_API_KEY, apiKey);
	}

	public static Token restoreState(SharedPreferences settings) {
		String apiKey = settings.getString(PREF_API_KEY, null);
		return apiKey == null ? null : new Token(apiKey);
	}
}
