package cz.rekola.android.api;

import java.util.List;

import cz.rekola.android.api.model.Bike;
import cz.rekola.android.api.model.BorrowedBike;
import cz.rekola.android.api.model.LockCode;
import cz.rekola.android.api.model.Poi;
import cz.rekola.android.api.model.ReturnedBike;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.api.requestmodel.RecoverPassword;
import cz.rekola.android.api.requestmodel.ReturningBike;
import cz.rekola.android.core.Constants;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ApiService {
	@POST("/accounts/mine/login")
	public void login(@Body Credentials body, Callback<Token> callback);

	@PUT("/password-recovery")
	public void recoverPassword(@Body RecoverPassword body, Callback<Object> callback);

	@GET("/bikes/all")
	public void getBikes(@Header(Constants.HEADER_KEY_TOKEN) String token, @Query("lat") String lat, @Query("lng") String lng, Callback<List<Bike>> callback);

	@GET("/bikes/mine")
	public void getBorrowedBike(@Header(Constants.HEADER_KEY_TOKEN) String token, Callback<BorrowedBike> callback);

	@GET("/bikes/lock-code")
	public void borrowBike(@Header(Constants.HEADER_KEY_TOKEN) String token, @Query("bikeCode") int bikeCode, @Query("lat") String lat, @Query("lng") String lng, Callback<LockCode> callback);

	@PUT("/bikes/{id}/return")
	public void returnBike(@Header(Constants.HEADER_KEY_TOKEN) String token, @Path("id") int bikeCode, @Body ReturningBike returningBike, Callback<ReturnedBike> callback);

	@GET("/location/pois")
	public void getPois(@Header(Constants.HEADER_KEY_TOKEN) String token, @Query("lat") String lat, @Query("lng") String lng, Callback<List<Poi>> callback);
}
