package cz.rekola.android.core.data;

import java.util.List;

import cz.rekola.android.api.ApiService;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.core.bus.LoginAvailableEvent;
import cz.rekola.android.core.bus.LoginFailedEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DataManager {

	private RekolaApp app;

	private Token token;
	private List<Bike> bikes;

	public DataManager(RekolaApp app) {
		this.app = app;
	}

	public void login(Credentials credentials) {
		ApiService apiService = app.getApiService();
		apiService.login(credentials, new Callback<Token>() {

			@Override
			public void success(Token resp, Response response) {
				token = resp;
				app.getPreferencesManager().setPersistentObject(resp); // TODO: Do we save it?
				app.getBus().post(new LoginAvailableEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				app.getBus().post(new LoginFailedEvent());
			}
		});
	}

	public List<Bike> getBikes() {
		if (bikes != null) {
			return bikes;
		}

		ApiService apiService = app.getApiService();
		apiService.getBikes(token.apiKey, "50.071667", "14.433804", new Callback<List<Bike>>() {

			@Override
			public void success(List<Bike> bikes, Response response) {
				DataManager.this.bikes = bikes;
				app.getBus().post(new BikesAvailableEvent());
			}
			@Override
			public void failure(RetrofitError error) {
				app.getBus().post(new BikesFailedEvent());
			}
		});

		return null;
	}
}
