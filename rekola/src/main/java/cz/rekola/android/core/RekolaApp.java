package cz.rekola.android.core;

import android.app.Application;

import cz.rekola.android.api.ApiService;
import retrofit.RestAdapter;

public class RekolaApp extends Application {

	private ApiService apiService;

	@Override
	public void onCreate() {
		super.onCreate();

		RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint(Constants.REKOLA_API_URL)
				.build();

		apiService = restAdapter.create(ApiService.class);
	}

	public ApiService getApiService() {
		return apiService;
	}
}
