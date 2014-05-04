package cz.rekola.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.rekola.android.R;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.core.loc.MyLocation;
import cz.rekola.android.core.loc.MyLocationListener;

public class MapFragment extends BaseMainFragment implements GoogleMap.OnInfoWindowClickListener {

    MapView vMap;
    GoogleMap map;

	private Map<Marker, Bike> markerMap = new HashMap<>();

    @Override
    public void onResume() {
        vMap.onResume();
        super.onResume();
    }

	@Override
	public void onPause() {
		super.onPause();
		vMap.onPause();
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        vMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        vMap.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        // Gets the MapView from the XML layout and creates it
        vMap = (MapView) rootView.findViewById(R.id.mapView);
        vMap.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = vMap.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.setOnInfoWindowClickListener(this);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng(), 12);
        map.moveCamera(cameraUpdate);

        return rootView;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getApp().getDataManager().getBikes() != null) {
			setupMap();
		}
	}

	@Subscribe
	public void bikesAvailable(BikesAvailableEvent event) {
		setupMap();
	}

	@Subscribe
	public void bikesFailed(BikesFailedEvent event) {

	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Bike bike = markerMap.get(marker);
		if (bike == null)
			return;
		//getAct().startBikeDetail(bike);
	}

	private void setupMap() {
		List<Bike> bikes = getApp().getDataManager().getBikes();
		if (bikes == null)
			return;

		map.clear();
		markerMap.clear();
		for (Bike bike : bikes) {
			Marker marker = map.addMarker(new MarkerOptions()
					.position(new LatLng(bike.location.lat, bike.location.lng))
					.title(bike.name)
					.snippet(bike.location.address));
			markerMap.put(marker, bike);
		}
	}
}
