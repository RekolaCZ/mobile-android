package cz.rekola.android.fragment.natural;

import android.app.Activity;
import android.content.Context;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

	private MarkerManager markers = new MarkerManager();
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
		map.setInfoWindowAdapter(new MapWindowAdapter(getActivity())); // Adapter creating invisible info windows to force the marker to move to front.

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

		markers.init();
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

		markers.updateMap(bikes);
	}

	private class MarkerManager implements GoogleMap.OnMarkerClickListener {

		private Map<Marker, Bike> markerMap = new HashMap<>();

		private BitmapDescriptor markerNormalBitmap;
		private BitmapDescriptor markerFocusedBitmap;

		private Marker lastMarker = null;

		void init() {
			map.setOnMarkerClickListener(this);
			markerNormalBitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal);
			markerFocusedBitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_focused_pressed);
		}

		void updateMap(List<Bike> bikes) {
			map.clear();
			markerMap.clear();
			for (Bike bike : bikes) {
				Marker marker = map.addMarker(new MarkerOptions()
						.position(new LatLng(bike.location.lat, bike.location.lng))
						.alpha(0.7f)
						.title(bike.name)
						.icon(markerNormalBitmap));
				markerMap.put(marker, bike);
			}
		}

		void deselect() {
			if (lastMarker != null) {
				lastMarker.setIcon(markerNormalBitmap);
				overlay.hide();
			}
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			deselect();

			if (marker.equals(lastMarker)) {
				lastMarker = null;
				return true;
			}

			marker.setIcon(markerFocusedBitmap);

			Bike bike = markerMap.get(marker);
			if (bike != null) {
				overlay.show(bike);
			}

			lastMarker = marker;

			return false;
		}
	}

	private class OverlayManager {

		void init() {
			vClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					markers.deselect();
				}
			});
		}

		void show(Bike bike) {
			vNameAndStreet.setText(bike.name + ", " + bike.location.address);
			vNote.setText(bike.location.note);
			vDescription.setText(bike.description);
			vOverlay.setVisibility(View.VISIBLE);
		}

		void hide() {
			vOverlay.setVisibility(View.GONE);
		}
	}

	/**
	 * Hack to move the selected marker to the front.
	 * Info window must be displayed to move to front, so we crate an empty info window.
	 */
	public class MapWindowAdapter implements GoogleMap.InfoWindowAdapter {
		private Context context = null;

		public MapWindowAdapter(Context context) {
			this.context = context;
		}

		// Hack to prevent info window from displaying: use a 0dp/0dp frame
		@Override
		public View getInfoWindow(Marker marker) {
			View v = ((Activity) context).getLayoutInflater().inflate(R.layout.map_invisible_info, null);
			return v;
		}

		@Override
		public View getInfoContents(Marker marker) {
			return null;
		}
	}
}
