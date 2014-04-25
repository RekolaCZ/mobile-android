package cz.rekola.android.core;

import cz.rekola.android.api.ApiService;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.core.bus.LoginAvailableEvent;
import cz.rekola.android.core.bus.LoginFailedEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DataManager {

	private RekolaApp app;

	private Token token;

	public DataManager(RekolaApp app) {
		this.app = app;
	}

	public void login(Credentials credentials) {
		ApiService apiService = app.getApiService();
		apiService.login(credentials, new Callback<Token>() {
			@Override
			public void success(Token resp, Response response) {
				app.getPreferencesManager().setPersistentObject(resp);
				app.getBus().post(new LoginAvailableEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				app.getBus().post(new LoginFailedEvent());
			}
		});
	}
}
