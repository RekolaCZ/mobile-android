package cz.rekola.app.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import cz.rekola.app.R;
import cz.rekola.app.api.ApiService;
import cz.rekola.app.api.model.bike.BorrowedBike;
import cz.rekola.app.core.RekolaApp;
import cz.rekola.app.core.version.VersionManager;
import cz.rekola.app.utils.NotificationUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Receive intent from Alarm manager after some time, to check, if bike is not returned
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {21.10.2015}
 **/
public class BikeNotReturnedService extends Service {
    public static final String TAG = BikeNotReturnedService.class.getName();

    public static final String ARG_TOKEN = "token";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!isNetworkAvailable(getApplication())) {
            NotificationUtils.createNotification(getBaseContext());
            stopSelf();
            return START_NOT_STICKY;
        }

        String token = intent.getStringExtra(ARG_TOKEN);
        VersionManager versionManager = new VersionManager(getApplication());
        String baseUrl = getResources().getString(R.string.rekola_base_api_url);
        ApiService apiService = RekolaApp.createApiService(versionManager, baseUrl);

        apiService.getBorrowedBike(token, new Callback<BorrowedBike>() {
            @Override
            public void success(BorrowedBike borrowedBike, Response response) {
                if (borrowedBike != null) { //borrowed
                    NotificationUtils.createNotification(getBaseContext());
                }
                stopSelf();
            }

            @Override
            public void failure(RetrofitError error) {
                stopSelf();
            }
        });

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] activeNetworkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo networkInfo : activeNetworkInfos) {
            if (networkInfo.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }
}
