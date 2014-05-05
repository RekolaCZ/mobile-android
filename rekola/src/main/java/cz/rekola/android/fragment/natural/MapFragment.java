package cz.rekola.android.fragment.natural;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.fragment.base.BaseMainFragment;

public class MapFragment extends BaseMainFragment {

    MapView vMap;
    GoogleMap map;

	@InjectView(R.id.map_overlay)
	LinearLayout vOverlay;

	@InjectView(R.id.map_overlay_close)
	ImageView vClose;

	@InjectView(R.id.map_overlay_name_and_street)
	TextView vNameAndStreet;

	@InjectView(R.id.map_overlay_note)
	TextView vNote;

	@InjectView(R.id.map_overlay_description)
	TextView vDescription;

	private Map<Marker, Bike> markerMap = new HashMap<>();

	private OverlayManager overlay = new OverlayManager();

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
		ButterKnife.inject(this, view);

		overlay.init();

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
		markerMap.clear();
		for (Bike bike : bikes) {
			Marker marker = map.addMarker(new MarkerOptions()
					.position(new LatLng(bike.location.lat, bike.location.lng))
					.title(bike.name)
					.snippet(bike.location.address));
			markerMap.put(marker, bike);
		}
	}

	private class OverlayManager implements GoogleMap.OnMarkerClickListener {

		private Marker lastMarker = null;

		void init() {
			map.setOnMarkerClickListener(this);
			vClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					hide();
				}
			});
		}

		void show(Bike bike, Marker marker) {
			vNameAndStreet.setText(bike.name + ", " + bike.location.address);
			vNote.setText(bike.location.note);
			vDescription.setText(bike.description);
			vOverlay.setVisibility(View.VISIBLE);
		}

		void hide() {
			if (lastMarker != null) {
				lastMarker.setRotation(0);
				lastMarker = null;
			}

			vOverlay.setVisibility(View.GONE);
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			if (lastMarker != null) {
				lastMarker.setRotation(0);
			}
			lastMarker = marker;

			marker.setRotation(-30);
			Bike bike = markerMap.get(marker);
			if (bike == null)
				return true;
			show(bike, marker);
			return true;
		}
	}
}
