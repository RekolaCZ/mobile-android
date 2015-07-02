package cz.rekola.app.core;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import cz.rekola.app.api.ApiService;
import cz.rekola.app.core.data.DataManager;
import cz.rekola.app.core.data.PreferencesManager;
import cz.rekola.app.core.loc.MyLocationManager;
import cz.rekola.app.core.version.VersionManager;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RekolaApp extends Application {

    private ApiService apiService;
    private Bus bus;
    private DataManager dataManager;
    private PreferencesManager preferencesManager;
    private MyLocationManager myLocationManager;
    private VersionManager versionManager;

    @Override
    public void onCreate() {
        super.onCreate();

        //How to configure a custom gson object to Retrofit's RestAdapter
        // http://naoyamakino.blogspot.cz/2013/11/how-to-configure-custom-gson-object-to.html
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.REKOLA_API_URL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader(Constants.HEADER_KEY_API_VERSION, versionManager.getApiVersion());
                        request.addHeader(Constants.HEADER_KEY_ACCEPT_LANGUAGE, versionManager.getAcceptLanguage());
                        request.addHeader(Constants.HEADER_KEY_OS, versionManager.getOs());
                    }
                })
                .build();

        apiService = restAdapter.create(ApiService.class);
        bus = new Bus();
        dataManager = new DataManager(this);
        preferencesManager = new PreferencesManager(this);
        myLocationManager = new MyLocationManager(this);
        versionManager = new VersionManager(this);
    }

    @Override
    public void onTerminate() {
        myLocationManager.terminate(); // TODO: Is this necessary?
    }

    public ApiService getApiService() {
        return apiService;
    }

    public Bus getBus() {
        return bus;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void resetDataManager() {
        dataManager = new DataManager(this);
    }

    public PreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public MyLocationManager getMyLocationManager() {
        return myLocationManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }
}
