package cz.rekola.android.api.loader;

import java.util.List;

import cz.rekola.android.api.ApiService;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.core.RekolaApp;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BikesLoader {

	public void load(RekolaApp app) {
		ApiService apiService = app.getApiService();
		apiService.getBikes(14.4, 48.2, new Callback<List<Bike>>() {
			@Override
			public void success(List<Bike> bikes, Response response) {
			}

			@Override
			public void failure(RetrofitError error) {
			}
		});
	}

}
