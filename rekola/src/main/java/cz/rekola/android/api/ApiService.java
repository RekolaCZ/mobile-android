package cz.rekola.android.api;

import java.util.List;

import cz.rekola.android.api.model.Bike;
import cz.rekola.android.api.model.BorrowedBike;
import cz.rekola.android.api.model.Location;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.api.requestmodel.Ping;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ApiService {
	@POST("/accounts/mine/ping")
	public void ping(@Body Ping body, Callback<Void> callback);

	@POST("/accounts/mine/login")
	public void login(@Body Credentials body, Callback<Token> callback);

	@GET("/bikes")
	public void getBikes(@Header("X-Api-Key") String token, @Query("lat") String lat, @Query("lng") String lng,/*@Path("lat") double lat, @Path("lng") double lng,*/ Callback<List<Bike>> callback);

	@GET("/bikes/mine")
	public void getBorrowedBike(@Header("X-Api-Key") String token, Callback<BorrowedBike> callback);

	@GET("/bikes/lock-code")
	public void borrowBike(@Header("X-Api-Key") String token, @Query("bikeCode") int bikeCode, @Query("lat") String lat, @Query("lng") String lng, Callback<BorrowedBike> callback);
}
