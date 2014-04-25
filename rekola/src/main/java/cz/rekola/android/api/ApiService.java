package cz.rekola.android.api;

import java.util.List;

import cz.rekola.android.api.model.Bike;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.api.requestmodel.Ping;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface ApiService {
	@POST("/accounts/mine/ping")
	public void ping(@Body Ping body, Callback<Void> callback);

	@POST("/accounts/mine/login")
	public void login(@Body Credentials body, Callback<Token> callback);

	@GET("/bikes?lat={lat}&lng={lng}")
	public void getBikes(@Path("lat") double lat, @Path("lng") double lng, Callback<List<Bike>> bikesCallback);
}
