package cz.rekola.android.core.data;

import android.content.SharedPreferences;

public interface PreferencePersistenty {

	public void saveState(SharedPreferences.Editor editor);

}
