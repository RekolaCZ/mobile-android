package cz.rekola.app.core.loc;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches current location. Uses only location updates from maps and/or other apps.
 * change deprecated api according to
 * http://stackoverflow.com/questions/24611977/android-locationclient-class-is-deprecated-but-used-in-documentation
 */
public class MyLocationManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, MyLocationListener {

    private static final MyLocation DEFAULT_MY_LOCATION = new MyLocation(Float.MAX_VALUE, 50.079167, 14.428414); // Location used when there is no location update available.

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MyLocation mMyLocation = DEFAULT_MY_LOCATION;
    private Set<MyLocationListener> listeners;

    public MyLocationManager(Context context) {
        //try to solve java.util.HashMap$HashIterator.nextEntry with thread safe hash map
        //https://rink.hockeyapp.net/manage/apps/177043/app_versions/5/crash_reasons/35300397
        listeners = Collections.newSetFromMap(new ConcurrentHashMap<MyLocationListener, Boolean>());
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        register(this); // Self registration for the first location update.
    }

    public void terminate() {
        mGoogleApiClient.disconnect();
    }

    public synchronized void register(MyLocationListener listener) {
        if (listeners.contains(listener))
            return;

        if (listeners.isEmpty()) {
            mGoogleApiClient.connect();
        }

        listeners.add(listener);
    }

    public synchronized void unregister(MyLocationListener listener) {
        if (!listeners.contains(listener))
            return;

        listeners.remove(listener);

        if (listeners.isEmpty()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null)
            return;

        float acc = location.hasAccuracy() ? location.getAccuracy() : Float.MAX_VALUE;
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        mMyLocation = new MyLocation(acc, lat, lng);

        synchronized (this) {
            for (MyLocationListener listener : listeners) {
                listener.onMyLocationChanged(mMyLocation);
            }
        }
    }

    public MyLocation getLastKnownMyLocation() {
        return mMyLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        mLocationRequest.setInterval(1000); // Update location every second

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if(lastLocation!=null)
        {
            mMyLocation = new MyLocation(lastLocation.getAccuracy(),
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude());
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        synchronized (this) {
            for (MyLocationListener listener : listeners) {
                listener.onMyLocationError();
            }
        }
    }

    /**
     * Self registered MyLocation listener to force update the location at the beginning.
     *
     * @param myLocation
     */
    @Override
    public void onMyLocationChanged(MyLocation myLocation) {
        unregister(this);
    }

    @Override
    public void onMyLocationError() {
        unregister(this);
    }
}
