package cz.rekola.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.List;

import cz.rekola.android.R;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.core.bus.LoginAvailableEvent;

public class MapFragment extends Fragment {

    MapView mapView;
    GoogleMap map;

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
		getApp().getBus().unregister(this);
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(50.085, 14.426), 12);
        map.animateCamera(cameraUpdate);

        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText("Mapa");
        return rootView;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getApp().getBus().register(this);
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

	private void setupMap() {
		List<Bike> bikes = getApp().getDataManager().getBikes();
		if (bikes == null)
			return;

		map.clear();
		for (Bike bike : bikes) {
			map.addMarker(new MarkerOptions()
					.position(new LatLng(bike.location.lat, bike.location.lng))
					.title(bike.name));
		}
	}

	private RekolaApp getApp() {
		return (RekolaApp) getActivity().getApplication();
	}
}
