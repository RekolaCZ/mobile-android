package cz.rekola.app.core.data;

import android.util.Log;

import java.net.HttpURLConnection;
import java.util.Date;
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
import cz.rekola.app.api.model.error.BikeConflictError;
import cz.rekola.app.api.model.error.MessageError;
import cz.rekola.app.api.model.map.Poi;
import cz.rekola.app.api.model.user.Account;
import cz.rekola.app.api.model.user.Token;
import cz.rekola.app.api.requestmodel.Credentials;
import cz.rekola.app.api.requestmodel.RecoverPassword;
import cz.rekola.app.api.requestmodel.ReturningBike;
import cz.rekola.app.core.RekolaApp;
import cz.rekola.app.core.bus.AuthorizationRequiredEvent;
import cz.rekola.app.core.bus.BikeIssueFailedEvent;
import cz.rekola.app.core.bus.BikeIssuesAvailableEvent;
import cz.rekola.app.core.bus.BikesAvailableEvent;
import cz.rekola.app.core.bus.BikesFailedEvent;
import cz.rekola.app.core.bus.BorrowedBikeAvailableEvent;
import cz.rekola.app.core.bus.BorrowedBikeFailedEvent;
import cz.rekola.app.core.bus.DataLoadingFinished;
import cz.rekola.app.core.bus.DataLoadingStarted;
import cz.rekola.app.core.bus.IncompatibleApiEvent;
import cz.rekola.app.core.bus.LockCodeEvent;
import cz.rekola.app.core.bus.LockCodeFailedEvent;
import cz.rekola.app.core.bus.LoginAvailableEvent;
import cz.rekola.app.core.bus.LoginFailedEvent;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.core.bus.PasswordRecoveryEvent;
import cz.rekola.app.core.bus.PasswordRecoveryFailed;
import cz.rekola.app.core.bus.PoisAvailableEvent;
import cz.rekola.app.core.bus.PoisFailedEvent;
import cz.rekola.app.core.bus.ReturnBikeEvent;
import cz.rekola.app.core.bus.ReturnBikeFailedEvent;
import cz.rekola.app.core.loc.MyLocation;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DataManager {

    public static final String TAG = "DataManager";

    private RekolaApp app;

    private Token token;
    private List<Bike> bikes;
    HashMap<Integer, List<Issue>> bikeIssuesMap = new HashMap<>(); //HashMap<bikeId, issues>
    private MyBikeWrapper myBike;
    private List<Poi> pois;
    private Account account;
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
        apiService.login(credentials, new Callback<Token>() {
            @Override
            public void success(Token resp, Response response) {
                loadingManager.removeLoading(DataLoad.LOGIN);
                token = resp;
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
        if (token == null || !loadingManager.addLoading(DataLoad.LOGOUT)) {
            return;
        }

        //only invalidate token, so user don't need to know about success or failure
        ApiService apiService = app.getApiService();
        apiService.logout(token.apiKey, new Callback<Object>() {
            @Override
            public void success(Object unused, Response response) {
                loadingManager.removeLoading(DataLoad.LOGOUT);
            }

            @Override
            public void failure(RetrofitError error) {
                loadingManager.removeLoading(DataLoad.LOGOUT);
                Log.e(TAG, "logout error " + error.getMessage());
            }
        });
    }

    public Token getToken() {
        return token;
    }

    public boolean isOperational() {
        return token != null;
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
        apiService.getBikes(token.apiKey, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<List<Bike>>() {
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
        apiService.getBorrowedBike(token.apiKey, new Callback<BorrowedBike>() {
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
        apiService.borrowBike(token.apiKey, bikeCode, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<LockCode>() {
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
        apiService.returnBike(token.apiKey, bikeCode, returningBike, new Callback<ReturnedBike>() {

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
        apiService.getPois(token.apiKey, myLoc.lat.toString(), myLoc.lng.toString(), new Callback<List<Poi>>() {
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

//TODO delete after api will be ready
        Account account = new Account();
        account.name = "Korben Dallas";
        account.membershipEnd = new Date(0);
        account.email = "mail@example.com";
        account.phone = "+420 555 888 777";
        account.address = "Bechyňova 274/8, Praha 6";
        return account;

/*
        if (account != null  || !loadingManager.addLoading(DataLoad.ACCOUNT)) {
            return account;
        }

        ApiService apiService = app.getApiService();
        apiService.getAccount(token.apiKey, new Callback<Account>() {
                    @Override
                    public void success(Account account, Response response) {
                        loadingManager.removeLoading(DataLoad.ACCOUNT);
                        DataManager.this.account = account;
                        app.getBus().post(new BikesAvailableEvent());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loadingManager.removeLoading(DataLoad.ACCOUNT);
                        handleGlobalError(error, app.getResources().getString(R.string.error_get_bikes_failed));
                    }
                });

        return null;
        */
    }

    public List<Issue> getBikeIssues(final int bikeId) {
        if (bikeIssuesMap.containsKey(bikeId) || !loadingManager.addLoading(DataLoad.BIKE_ISSUES)) {
            return bikeIssuesMap.get(bikeId);
        }

        ApiService apiService = app.getApiService();
        apiService.getBikeIssues(token.apiKey, bikeId, new Callback<List<Issue>>() {
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
        if (error.getResponse() == null) { // This is a bug in retrofit when handling incorrect authentication
            if (error.getCause() != null && error.getCause().getMessage() != null && error.getCause().getMessage().contains("No authentication challenges found")) { // 401
                app.getBus().post(new MessageEvent(title));
                app.getBus().post(new AuthorizationRequiredEvent());
                return;
            }
            if (error.isNetworkError()) {
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
        LOGOUT,
        PASSWORD_RECOVERY,
        BORROWED_BIKE,
        BIKES,
        BIKE_ISSUES,
        BORROW_BIKE,
        RETURN_BIKE,
        POIS,
        CUSTOM_LOAD_DIRECTIONS,
        ACCOUNT
    }

    private class LoadingManager {

        private Set<DataLoad> loading = new HashSet<DataLoad>(DataLoad.values().length);

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
