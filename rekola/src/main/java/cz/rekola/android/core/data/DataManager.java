package cz.rekola.android.core.data;

import java.util.List;

import cz.rekola.android.api.ApiService;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.api.model.BorrowedBike;
import cz.rekola.android.api.model.LockCode;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.model.error.BaseError;
import cz.rekola.android.api.model.error.BikeConflictError;
import cz.rekola.android.api.model.error.MessageError;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.api.requestmodel.ReturningBike;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.bus.BorrowedBikeAvailableEvent;
import cz.rekola.android.core.bus.BorrowedBikeFailedEvent;
import cz.rekola.android.core.bus.LockCodeEvent;
import cz.rekola.android.core.bus.LockCodeFailedEvent;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.core.bus.LoginAvailableEvent;
import cz.rekola.android.core.bus.LoginFailedEvent;
import cz.rekola.android.core.bus.ReturnBikeEvent;
import cz.rekola.android.core.bus.ReturnBikeFailedEvent;
import cz.rekola.android.core.loc.MyLocation;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DataManager {

	private RekolaApp app;

	private Token token;
	private List<Bike> bikes;
	private MyBikeWrapper myBike;

	public DataManager(RekolaApp app) {
		this.app = app;
	}

	public void login(Credentials credentials) {
		ApiService apiService = app.getApiService();
		apiService.login(app.getVersionManager().getUserAgent(), credentials, new Callback<Token>() {
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

	public Token getToken() {
		return token;
	}

	public List<Bike> getBikes() {
		if (bikes != null) {
			return bikes;
		}

		ApiService apiService = app.getApiService();
		MyLocation myLoc = app.getMyLocationManager().getLastKnownMyLocation();
		apiService.getBikes(app.getVersionManager().getUserAgent(), token.apiKey, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<List<Bike>>() {
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

	public MyBikeWrapper getBorrowedBike() {
		if (myBike != null) {
			return myBike;
		}

		updateBorrowedBike();
		return null;
	}

	private void updateBorrowedBike() {
		ApiService apiService = app.getApiService();
		apiService.getBorrowedBike(app.getVersionManager().getUserAgent(), token.apiKey, new Callback<BorrowedBike>() {
			@Override
			public void success(BorrowedBike borrowedBike, Response response) {
				myBike = new MyBikeWrapper(borrowedBike);
				app.getBus().post(new BorrowedBikeAvailableEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				if (error.getResponse().getStatus() == 404) {
					myBike = new MyBikeWrapper();
					app.getBus().post(new BorrowedBikeAvailableEvent());
				} else {
					app.getBus().post(new BorrowedBikeFailedEvent());
				}
			}
		});
	}

	public void borrowBike(int bikeCode) {
		ApiService apiService = app.getApiService();
		MyLocation myLoc = app.getMyLocationManager().getLastKnownMyLocation();
		apiService.borrowBike(app.getVersionManager().getUserAgent(), token.apiKey, bikeCode, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<LockCode>() {
			@Override
			public void success(LockCode lockCode, Response response) {
				myBike = new MyBikeWrapper(lockCode);
				updateBorrowedBike();
				app.getBus().post(new LockCodeEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				BaseError err;
				LockCodeFailedEvent.EState state;
				switch (error.getResponse().getStatus()) {
					case 400:
						state = LockCodeFailedEvent.EState.WRONG_CODE;
						err = (MessageError) error.getBodyAs(MessageError.class);
						break;
					case 403:
						state = LockCodeFailedEvent.EState.FORBIDDEN;
						err = (MessageError) error.getBodyAs(MessageError.class);
						break;
					case 409:
						state = LockCodeFailedEvent.EState.CONFLICT;
						err = (BikeConflictError) error.getBodyAs(BikeConflictError.class);
						break;
					default:
						state = LockCodeFailedEvent.EState.UNKNOWN;
						err = null;
				}
				app.getBus().post(new LockCodeFailedEvent(state, err));
			}
		});
	}

	public void returnBike(int bikeCode, ReturningBike returningBike) {
		ApiService apiService = app.getApiService();
		apiService.returnBike(app.getVersionManager().getUserAgent(), token.apiKey, bikeCode, returningBike, new Callback<Object>() {

			@Override
			public void success(Object empty, Response response) {
				myBike = new MyBikeWrapper();
				updateBorrowedBike();
				app.getBus().post(new ReturnBikeEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				BaseError err;
				ReturnBikeFailedEvent.EState state;
				switch (error.getResponse().getStatus()) {
					case 409:
						state = ReturnBikeFailedEvent.EState.CONFLICT;
						err = (BikeConflictError) error.getBodyAs(BikeConflictError.class);
						break;
					default:
						state = ReturnBikeFailedEvent.EState.UNKNOWN;
						err = null;
				}
				app.getBus().post(new ReturnBikeFailedEvent(state, err));
			}
		});
	}
}
