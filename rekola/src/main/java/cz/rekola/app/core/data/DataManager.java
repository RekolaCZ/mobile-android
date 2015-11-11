package cz.rekola.app.core.data;

import android.util.Log;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.rekola.app.R;
import cz.rekola.app.api.ApiService;
import cz.rekola.app.api.model.bike.Bike;
import cz.rekola.app.api.model.bike.BorrowedBike;
import cz.rekola.app.api.model.bike.Issue;
import cz.rekola.app.api.model.bike.LockCode;
import cz.rekola.app.api.model.bike.ReturnedBike;
import cz.rekola.app.api.model.defaultValues.DefaultValues;
import cz.rekola.app.api.model.error.BikeConflictError;
import cz.rekola.app.api.model.error.MessageError;
import cz.rekola.app.api.model.map.Boundaries;
import cz.rekola.app.api.model.map.Poi;
import cz.rekola.app.api.model.user.Account;
import cz.rekola.app.api.model.user.Login;
import cz.rekola.app.api.requestmodel.Credentials;
import cz.rekola.app.api.requestmodel.IssueReport;
import cz.rekola.app.api.requestmodel.RecoverPassword;
import cz.rekola.app.api.requestmodel.ReturningBike;
import cz.rekola.app.core.RekolaApp;
import cz.rekola.app.core.bus.AuthorizationRequiredEvent;
import cz.rekola.app.core.bus.DataLoadingFinished;
import cz.rekola.app.core.bus.DataLoadingStarted;
import cz.rekola.app.core.bus.IncompatibleApiEvent;
import cz.rekola.app.core.bus.LockCodeEvent;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.core.bus.PasswordRecoveryEvent;
import cz.rekola.app.core.bus.dataAvailable.AccountAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.BikeIssuesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.BikesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.BorrowedBikeAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.BoundariesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.DefaultValuesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.LoginAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.PoisAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.ReturnBikeEvent;
import cz.rekola.app.core.bus.dataFailed.BikeIssueFailedEvent;
import cz.rekola.app.core.bus.dataFailed.BikesFailedEvent;
import cz.rekola.app.core.bus.dataFailed.BorrowedBikeFailedEvent;
import cz.rekola.app.core.bus.dataFailed.DefaultValuesFailedEvent;
import cz.rekola.app.core.bus.dataFailed.LockCodeFailedEvent;
import cz.rekola.app.core.bus.dataFailed.LoginFailedEvent;
import cz.rekola.app.core.bus.dataFailed.PasswordRecoveryFailed;
import cz.rekola.app.core.bus.dataFailed.PoisFailedEvent;
import cz.rekola.app.core.bus.dataFailed.ReturnBikeFailedEvent;
import cz.rekola.app.core.loc.MyLocation;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Data manager for get and store data from api. Storing is only in this class and this class is part of MainActivity
 * <p/>
 * How it works:
 * 1) All data is stored in class variables e.g. List<Bike> bikes;
 * 2) Some Fragment need data, so it will call getApp().getDataManager().getBikes(true);
 * 3) First time it will return null, but it will run asynchronous request to api
 * 4) If data is available or it fail, it is driven by event library Otto
 * 5) In fragment is subscribe method e.g.
 *
 * @Subscribe public void bikesAvailable(BikesAvailableEvent event) {
 * setupMap();
 * }
 * <p/>
 * and that's all
 */

public class DataManager {

    public static final String TAG = "DataManager";

    private RekolaApp app;

    private String apiKey;
    private List<Bike> bikes;
    HashMap<Integer, List<Issue>> bikeIssuesMap = new HashMap<>(); //HashMap<bikeId, issues>
    private MyBikeWrapper myBike;
    private List<Poi> pois;
    private Account account;
    private Boundaries boundaries;
    private DefaultValues defaultValues;

    private LoadingManager loadingManager;

    public DataManager(RekolaApp app) {
        this.app = app;
        loadingManager = new LoadingManager();
    }

    public void login(Credentials credentials) {
        loginRecursive(credentials, true);
    }

    /**
     * Hack-fix for retrofit java.io.EOFException thrown sometimes on first try in POST, PUT requests containing \n.
     * GET requests are fine.
     */
    private void loginRecursive(final Credentials credentials, final boolean retry) {
        if (!loadingManager.addLoading(DataLoad.LOGIN)) {
            return;
        }

        ApiService apiService = app.getApiService();
        apiService.login(credentials, new Callback<Login>() {
            @Override
            public void success(Login resp, Response response) {
                loadingManager.removeLoading(DataLoad.LOGIN);

                apiKey = resp.apiKey;
                app.getPreferencesManager().setToken(apiKey);
                app.getPreferencesManager().setWebViewBikeDetail(resp.showWebviewForBikedetail);

                app.getBus().post(new LoginAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.LOGIN);

                // retrofit.RetrofitError: java.io.EOFException: https://github.com/square/retrofit/issues/397
                if (error.toString().contains("java.io.EOFException") && retry) {
                    loginRecursive(credentials, false);
                    return;
                }

                app.getBus().post(new LoginFailedEvent());
                handleGlobalError(error, app.getResources().getString(R.string.error_title_login_failed));
            }
        });
    }

    public void logout() {
        ApiService apiService = app.getApiService();
        apiService.logout(apiKey, new Callback<Object>() {
            @Override
            public void success(Object unused, Response response) {
                //only invalidate Token, so user don't need to know about success or failure
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "logout error " + error.getMessage());
            }
        });
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isOperational() {
        return !apiKey.equals("");
    }

    public boolean showWebViewForBikeDetail() {
        return app.getPreferencesManager().getWebViewBikeDetail();
    }

    public void recoverPassword(RecoverPassword email) {
        if (!loadingManager.addLoading(DataLoad.PASSWORD_RECOVERY)) {
            return;
        }

        ApiService apiService = app.getApiService();
        apiService.recoverPassword(email, new Callback<Object>() {
            @Override
            public void success(Object unused, Response response) {
                loadingManager.removeLoading(DataLoad.PASSWORD_RECOVERY);
                app.getBus().post(new PasswordRecoveryEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.PASSWORD_RECOVERY);
                app.getBus().post(new PasswordRecoveryFailed());
                handleGlobalError(error, app.getResources().getString(R.string.error_title_password_recovery_failed));
            }
        });
    }

    public List<Bike> getBikes(boolean forceUpdate) {
        if ((bikes != null && !forceUpdate) || !loadingManager.addLoading(DataLoad.BIKES)) {
            return bikes;
        }

        ApiService apiService = app.getApiService();
        MyLocation myLoc = app.getMyLocationManager().getLastKnownMyLocation();
        apiService.getBikes(apiKey, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<List<Bike>>() {
            @Override
            public void success(List<Bike> bikes, Response response) {
                loadingManager.removeLoading(DataLoad.BIKES);
                DataManager.this.bikes = bikes;
                app.getBus().post(new BikesAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.BIKES);
                app.getBus().post(new BikesFailedEvent());
                handleGlobalError(error, app.getResources().getString(R.string.error_get_bikes_failed));
            }
        });

        return null;
    }

    public DefaultValues getDefaultValues() {
        if (defaultValues != null || !loadingManager.addLoading(DataLoad.DEFAULT_VALUES)) {
            return defaultValues;
        }

        app.getApiService().getDefaultValues(apiKey, new Callback<DefaultValues>() {
            @Override
            public void success(DefaultValues defaultValues, Response response) {
                loadingManager.removeLoading(DataLoad.DEFAULT_VALUES);
                DataManager.this.defaultValues = defaultValues;
                app.getBus().post(new DefaultValuesAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.DEFAULT_VALUES);
                app.getBus().post(new DefaultValuesFailedEvent());
                handleGlobalError(error, app.getResources().getString(R.string.error_get_default_values_failed));
            }
        });

        return null;
    }

    public Bike getBike(int bikeId) {
        if (myBike != null && myBike.bike != null && myBike.bike.id == bikeId) {
            return myBike.bike;
        } else if (bikes != null) {
            for (Bike bike : bikes) {
                if (bike.id == bikeId)
                    return bike;
            }
        } else //bike not found
            getBikes(false);

        return null;
    }

    public MyBikeWrapper getBorrowedBike(boolean forceUpdate) {
        if (myBike != null && !forceUpdate) {
            return myBike;
        }

        // Get borrowed bike might be called from fragments created while the activity is terminating. Well done android optimizations..
        if (isOperational()) {
            updateBorrowedBike();
        }
        return null;
    }

    private void updateBorrowedBike() {
        if (!loadingManager.addLoading(DataLoad.BORROWED_BIKE))
            return;

        ApiService apiService = app.getApiService();
        apiService.getBorrowedBike(apiKey, new Callback<BorrowedBike>() {
            @Override
            public void success(BorrowedBike borrowedBike, Response response) {
                loadingManager.removeLoading(DataLoad.BORROWED_BIKE);
                myBike = new MyBikeWrapper(borrowedBike);
                app.getBus().post(new BorrowedBikeAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.BORROWED_BIKE);
                if (error.getResponse() != null) {
                    if (error.getResponse().getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        myBike = new MyBikeWrapper();
                        app.getBus().post(new BorrowedBikeAvailableEvent());
                        return;
                    }
                }
                app.getBus().post(new BorrowedBikeFailedEvent());
                Log.e(TAG, error.toString());
                handleGlobalError(error, app.getResources().getString(R.string.error_get_borrowed_bike_failed));
            }
        });
    }

    public void borrowBike(int bikeCode) {
        if (!loadingManager.addLoading(DataLoad.BORROW_BIKE)) {
            return;
        }

        ApiService apiService = app.getApiService();
        MyLocation myLoc = app.getMyLocationManager().getLastKnownMyLocation();
        apiService.borrowBike(apiKey, bikeCode, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<LockCode>() {
            @Override
            public void success(LockCode lockCode, Response response) {
                loadingManager.removeLoading(DataLoad.BORROW_BIKE);
                myBike = new MyBikeWrapper(lockCode);
                updateBorrowedBike();
                app.getBus().post(new LockCodeEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.BORROW_BIKE);
                LockCodeFailedEvent event = new LockCodeFailedEvent(LockCodeFailedEvent.EState.UNKNOWN, null);
                if (error.getResponse() != null) {
                    switch (error.getResponse().getStatus()) {
                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            event = new LockCodeFailedEvent(LockCodeFailedEvent.EState.WRONG_CODE, null);
                            break;
                        case HttpURLConnection.HTTP_FORBIDDEN:
                            event = new LockCodeFailedEvent(LockCodeFailedEvent.EState.FORBIDDEN, null);
                            break;
                        case HttpURLConnection.HTTP_CONFLICT:
                            event = new LockCodeFailedEvent(LockCodeFailedEvent.EState.FORBIDDEN, (BikeConflictError) error.getBodyAs(BikeConflictError.class));
                            break;
                    }
                }
                app.getBus().post(event);
                handleGlobalError(error, app.getResources().getString(R.string.error_borrow_bike_failed));
            }
        });
    }

    public void returnBike(final int bikeCode, final ReturningBike returningBike) {
        returnBikeRecursive(bikeCode, returningBike, true);
    }

    private void returnBikeRecursive(final int bikeCode, final ReturningBike returningBike, final boolean retry) {
        if (!loadingManager.addLoading(DataLoad.RETURN_BIKE)) {
            return;
        }

        ApiService apiService = app.getApiService();
        apiService.returnBike(apiKey, bikeCode, returningBike, new Callback<ReturnedBike>() {

            @Override
            public void success(ReturnedBike returnedBike, Response response) {
                loadingManager.removeLoading(DataLoad.RETURN_BIKE);
                myBike = new MyBikeWrapper();
                updateBorrowedBike();
                app.getBus().post(new ReturnBikeEvent(returnedBike.successUrl));
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.RETURN_BIKE);

                // retrofit.RetrofitError: java.io.EOFException: https://github.com/square/retrofit/issues/397
                if (error.toString().contains("java.io.EOFException") && retry) {
                    returnBikeRecursive(bikeCode, returningBike, false);
                    return;
                }

                ReturnBikeFailedEvent event = new ReturnBikeFailedEvent(ReturnBikeFailedEvent.EState.UNKNOWN, null);
                if (error.getResponse() != null) {
                    switch (error.getResponse().getStatus()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            event = new ReturnBikeFailedEvent(ReturnBikeFailedEvent.EState.CONFLICT, (BikeConflictError) error.getBodyAs(BikeConflictError.class));
                            break;
                    }
                }
                app.getBus().post(event);
                handleGlobalError(error, app.getResources().getString(R.string.error_return_bike_failed));
            }
        });
    }

    public List<Poi> getPois(boolean forceUpdate) {
        if ((pois != null && !forceUpdate) || !loadingManager.addLoading(DataLoad.POIS)) {
            return pois;
        }

        ApiService apiService = app.getApiService();
        MyLocation myLoc = app.getMyLocationManager().getLastKnownMyLocation();
        apiService.getPois(apiKey, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<List<Poi>>() {
            @Override
            public void success(List<Poi> pois, Response response) {
                loadingManager.removeLoading(DataLoad.POIS);
                DataManager.this.pois = pois;
                app.getBus().post(new PoisAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.POIS);
                app.getBus().post(new PoisFailedEvent());
                handleGlobalError(error, app.getResources().getString(R.string.error_get_pois_failed));
            }
        });

        return null;
    }

    public void reportIssue(final int bikeID, IssueReport issueReport) {
        ApiService apiService = app.getApiService();
        apiService.reportIssue(apiKey, bikeID, issueReport, new Callback<Object>() {
            @Override
            public void success(Object unused, Response response) {
                getBikeIssues(bikeID, true); //forced update bike issues
                getBorrowedBike(true); //forced update bike issues
            }

            @Override
            public void failure(RetrofitError error) {
                handleGlobalError(error, app.getResources().getString(R.string.error_report_issue));
            }
        });

    }

    public boolean customLoadDirections() {
        return (loadingManager.addLoading(DataLoad.CUSTOM_LOAD_DIRECTIONS));
    }

    public void customLoadDirectionsFinished(boolean success) {
        loadingManager.removeLoading(DataLoad.CUSTOM_LOAD_DIRECTIONS);
        if (!success) {
            app.getBus().post(new MessageEvent(app.getResources().getString(R.string.error_directions)));
        }
    }

    public Account getAccount() {

        if (account != null || !loadingManager.addLoading(DataLoad.ACCOUNT)) {
            return account;
        }

        ApiService apiService = app.getApiService();
        apiService.getAccount(apiKey, new Callback<Account>() {
            @Override
            public void success(Account account, Response response) {
                loadingManager.removeLoading(DataLoad.ACCOUNT);
                DataManager.this.account = account;
                app.getBus().post(new AccountAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.ACCOUNT);
                handleGlobalError(error, app.getResources().getString(R.string.error_get_account_failed));
            }
        });

        return null;
    }

    public Boundaries getBoundaries() {
        if (boundaries != null || !loadingManager.addLoading(DataLoad.BOUNDARIES)) {
            return boundaries;
        }

        ApiService apiService = app.getApiService();
        apiService.getBoundaries(apiKey, new Callback<Boundaries>() {
            @Override
            public void success(Boundaries boundaries, Response response) {
                loadingManager.removeLoading(DataLoad.BOUNDARIES);
                DataManager.this.boundaries = boundaries;
                app.getBus().post(new BoundariesAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.BOUNDARIES);
                handleGlobalError(error, app.getResources().getString(R.string.error_get_bike_failed));
            }
        });

        return null;
    }

    public List<Issue> getBikeIssues(final int bikeId, boolean forceUpdate) {
        if ((!forceUpdate && bikeIssuesMap.containsKey(bikeId))
                || !loadingManager.addLoading(DataLoad.BIKE_ISSUES)) {
            return bikeIssuesMap.get(bikeId);
        }

        if(bikeIssuesMap.containsKey(bikeId))
        {
            bikeIssuesMap.remove(bikeId);
        }

        ApiService apiService = app.getApiService();
        apiService.getBikeIssues(apiKey, bikeId, new Callback<List<Issue>>() {
            @Override
            public void success(List<Issue> bikeIssues, Response response) {
                loadingManager.removeLoading(DataLoad.BIKE_ISSUES);
                DataManager.this.bikeIssuesMap.put(bikeId, bikeIssues);
                app.getBus().post(new BikeIssuesAvailableEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.BIKE_ISSUES);
                app.getBus().post(new BikeIssueFailedEvent());
                handleGlobalError(error, app.getResources().getString(R.string.error_get_bike_failed));
            }
        });

        return null;
    }

    private void handleGlobalError(RetrofitError error, String title) {
        Log.e(TAG, "global error " + error.toString());

        if (error.getResponse() == null) { // This is a bug in retrofit when handling incorrect authentication
            if (error.getCause() != null && error.getCause().getMessage() != null && error.getCause().getMessage().contains("No authentication challenges found")) { // 401
                app.getBus().post(new MessageEvent(title));
                app.getBus().post(new AuthorizationRequiredEvent());
                return;
            }
            if (error.getKind() == RetrofitError.Kind.NETWORK) {
                app.getBus().post(new MessageEvent(title + " " + app.getResources().getString(R.string.error_network)));
            } else {
                app.getBus().post(new MessageEvent(title));
            }
            return;
        }

        MessageError msgErr;
        try {
            msgErr = (MessageError) error.getBodyAs(MessageError.class);
        } catch (Exception e) {
            msgErr = new MessageError();
        }
        if (msgErr.message == null || msgErr.message.isEmpty()) {
            app.getBus().post(new MessageEvent(title));
        } else {
            app.getBus().post(new MessageEvent(msgErr.message));
        }

        switch (error.getResponse().getStatus()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                app.getBus().post(new AuthorizationRequiredEvent());
                break;
            case 426: //Upgrade required
                app.getBus().post(new IncompatibleApiEvent());
                break;
        }
        // TODO: Handle 5xx errors
    }

    private enum DataLoad {
        LOGIN,
        PASSWORD_RECOVERY,
        BORROWED_BIKE,
        BIKES,
        BIKE_ISSUES,
        BORROW_BIKE,
        RETURN_BIKE,
        POIS,
        CUSTOM_LOAD_DIRECTIONS,
        ACCOUNT,
        BOUNDARIES,
        DEFAULT_VALUES
    }

    private class LoadingManager {

        private Set<DataLoad> loading = new HashSet<>(DataLoad.values().length);

        boolean addLoading(DataLoad dataLoad) {
            if (loading.isEmpty()) {
                app.getBus().post(new DataLoadingStarted());
            }
            return loading.add(dataLoad);
        }

        boolean removeLoading(DataLoad dataLoad) {
            boolean ret = loading.remove(dataLoad);
            if (loading.isEmpty()) {
                app.getBus().post(new DataLoadingFinished());
            }
            return ret;
        }

    }
}
