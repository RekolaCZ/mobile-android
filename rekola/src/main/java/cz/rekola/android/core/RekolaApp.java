package cz.rekola.android.core;

import android.app.Application;

import com.squareup.otto.Bus;

import cz.rekola.android.api.ApiService;
import cz.rekola.android.core.data.DataManager;
import cz.rekola.android.core.data.PreferencesManager;
import cz.rekola.android.core.loc.MyLocationManager;
import cz.rekola.android.core.version.VersionManager;
import retrofit.RestAdapter;

public class RekolaApp extends Application {

	private ApiService apiService;
	private Bus bus;
	private DataManager dataManager;
	private PreferencesManager preferencesManager;
	private MyLocationManager myLocationManager;
	private VersionManager versionManager;

	@Override
	public void onCreate() {
		super.onCreate();

		RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint(Constants.REKOLA_API_URL)
				.build();

		apiService = restAdapter.create(ApiService.class);
		bus = new Bus();
		dataManager = new DataManager(this);
		preferencesManager = new PreferencesManager(this);
		myLocationManager = new MyLocationManager(this);
		versionManager = new VersionManager(this);
	}


	@Override
	public void onTerminate() {
		myLocationManager.terminate(); // TODO: Is this necessary?
	}

	public ApiService getApiService() {
		return apiService;
	}

	public Bus getBus() {
		return bus;
	}

	public DataManager getDataManager() { return dataManager; }

	public PreferencesManager getPreferencesManager() {
		return preferencesManager;
	}

	public MyLocationManager getMyLocationManager() { return myLocationManager; }

	public VersionManager getVersionManager() { return versionManager; }
}
