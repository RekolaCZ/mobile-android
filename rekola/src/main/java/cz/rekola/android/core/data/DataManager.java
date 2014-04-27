package cz.rekola.android.core.data;

import java.util.List;

import cz.rekola.android.api.ApiService;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.api.model.BorrowedBike;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.model.error.BaseError;
import cz.rekola.android.api.model.error.BikeConflictError;
import cz.rekola.android.api.model.error.MessageError;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.bus.BikeBorrowFailedEvent;
import cz.rekola.android.core.bus.BikeBorrowedEvent;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.core.bus.LoginAvailableEvent;
import cz.rekola.android.core.bus.LoginFailedEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DataManager {

	private static final String FAKE_LAT = "50.071667";
	private static final String FAKE_LNG = "14.433804";

	private RekolaApp app;

	private Token token;
	private List<Bike> bikes;
	private BorrowedBike borrowedBike;

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
		apiService.getBikes(token.apiKey, FAKE_LAT, FAKE_LNG, new Callback<List<Bike>>() {
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

	public BorrowedBike getBorrowedBike() {
		return borrowedBike;
	}

	public void borrowBike(int bikeCode) {
		ApiService apiService = app.getApiService();
		apiService.borrowBike(token.apiKey, bikeCode, FAKE_LAT, FAKE_LNG, new Callback<BorrowedBike>() {
			@Override
			public void success(BorrowedBike borrowedBike, Response response) {
				DataManager.this.borrowedBike = borrowedBike;
				app.getBus().post(new BikeBorrowedEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				BaseError err;
				BikeBorrowFailedEvent.EState state;
				switch(error.getResponse().getStatus()) {
					case 400:
						state = BikeBorrowFailedEvent.EState.WRONG_CODE;
						err = (MessageError) error.getBodyAs(MessageError.class);
						break;
					case 403:
						state = BikeBorrowFailedEvent.EState.FORBIDDEN;
						err = (MessageError) error.getBodyAs(MessageError.class);
						break;
					case 409:
						state = BikeBorrowFailedEvent.EState.CONFLICT;
						err = (BikeConflictError) error.getBodyAs(BikeConflictError.class);
						break;
					default:
						state = BikeBorrowFailedEvent.EState.UNKNOWN;
						err = null;
				}
				app.getBus().post(new BikeBorrowFailedEvent(state, err));
			}
		});
	}
}
