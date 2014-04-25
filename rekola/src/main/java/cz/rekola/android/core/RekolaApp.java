package cz.rekola.android.core;

import android.app.Application;

import com.squareup.otto.Bus;

import cz.rekola.android.api.ApiService;
import retrofit.RestAdapter;

public class RekolaApp extends Application {

	private ApiService apiService;
	private Bus bus;
	private DataManager dataManager;
	private PreferencesManager preferencesManager;

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
}
