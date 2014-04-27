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
import cz.rekola.android.core.bus.BorrowBikeEvent;
import cz.rekola.android.core.bus.BorrowBikeFailedEvent;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.core.bus.IsBorrowedBikeAvailableEvent;
import cz.rekola.android.core.bus.IsBorrowedBikeFailedEvent;
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
	private BorrowedBike borrowedBike; // Can be null even after successful request.
	private Boolean isBorrowedBike; // NULL if the state is not known yet.

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

	/**
	 * Whether a bike is borrowed.
	 * @return Whether a bike is borrowed or NULL if not known.
	 */
	public Boolean isBorrowedBike() {
		if (isBorrowedBike != null) {
			return isBorrowedBike;
		}

		ApiService apiService = app.getApiService();
		apiService.getBorrowedBike(token.apiKey, new Callback<BorrowedBike>() {
			@Override
			public void success(BorrowedBike borrowedBike, Response response) {
				DataManager.this.borrowedBike = borrowedBike;
				isBorrowedBike = true;
				app.getBus().post(new IsBorrowedBikeAvailableEvent());
			}
			@Override
			public void failure(RetrofitError error) {
				if (error.getResponse().getStatus() == 404) {
					isBorrowedBike = false;
					app.getBus().post(new IsBorrowedBikeAvailableEvent());
				} else {
					app.getBus().post(new IsBorrowedBikeFailedEvent());
				}
			}
		});

		return null;
	}

	/**
	 * Returns borrowed bike.
	 * @return Borrowed bike if known, NULL if no bike borrowed, NULL if not known whether a bike is borrowed.
	 */
	public BorrowedBike getBorrowedBike() {
		return borrowedBike;
	}

	public void borrowBike(int bikeCode) {
		ApiService apiService = app.getApiService();
		apiService.borrowBike(token.apiKey, bikeCode, FAKE_LAT, FAKE_LNG, new Callback<BorrowedBike>() {
			@Override
			public void success(BorrowedBike borrowedBike, Response response) {
				DataManager.this.borrowedBike = borrowedBike;
				isBorrowedBike = true;
				app.getBus().post(new BorrowBikeEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				BaseError err;
				BorrowBikeFailedEvent.EState state;
				switch(error.getResponse().getStatus()) {
					case 400:
						state = BorrowBikeFailedEvent.EState.WRONG_CODE;
						err = (MessageError) error.getBodyAs(MessageError.class);
						break;
					case 403:
						state = BorrowBikeFailedEvent.EState.FORBIDDEN;
						err = (MessageError) error.getBodyAs(MessageError.class);
						break;
					case 409:
						state = BorrowBikeFailedEvent.EState.CONFLICT;
						err = (BikeConflictError) error.getBodyAs(BikeConflictError.class);
						break;
					default:
						state = BorrowBikeFailedEvent.EState.UNKNOWN;
						err = null;
				}
				app.getBus().post(new BorrowBikeFailedEvent(state, err));
			}
		});
	}
}
