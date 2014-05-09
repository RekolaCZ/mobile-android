package cz.rekola.android.core.loc;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import java.util.HashSet;
import java.util.Set;

/**
 * Caches current location. Uses only location updates from maps and/or other apps.
 */
public class MyLocationManager implements com.google.android.gms.location.LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MyLocationListener {

	private static final MyLocation DEFAULT_MY_LOCATION = new MyLocation(Float.MAX_VALUE, 50.079167, 14.428414); // Location used when there is no location update available.

	private LocationClient locationClient;
	private LocationRequest locationRequest;

	private MyLocation myLocation = DEFAULT_MY_LOCATION;

	private Set<MyLocationListener> listeners;

	public MyLocationManager(Context context) {
		listeners = new HashSet<MyLocationListener>();
		locationClient = new LocationClient(context, this, this);
		register(this); // Self registration for the first location update.
	}

	public void terminate() {
		locationClient.disconnect();
	}

	public synchronized void register(MyLocationListener listener) {
		if (listeners.contains(listener))
			return;

		if (listeners.isEmpty()) {
			locationClient.connect();
		}

		listeners.add(listener);
	}

	public synchronized void unregister(MyLocationListener listener) {
		if (!listeners.contains(listener))
			return;

		listeners.remove(listener);

		if (listeners.isEmpty()) {
			locationClient.disconnect();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null)
			return;

		float acc = location.hasAccuracy() ? location.getAccuracy() : Float.MAX_VALUE;
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		myLocation = new MyLocation(acc, lat, lng);

		synchronized (this) {
			for (MyLocationListener listener : listeners) {
				listener.onMyLocationChanged(myLocation);
			}
		}
	}

	public MyLocation getLastKnownMyLocation() {
		return myLocation;
	}

	@Override
	public void onConnected(Bundle bundle) {
		locationRequest = LocationRequest.create();
		locationRequest.setInterval(100);
		locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	@Override
	public void onDisconnected() {
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
