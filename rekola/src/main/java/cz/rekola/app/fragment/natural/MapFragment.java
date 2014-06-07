package cz.rekola.app.fragment.natural;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.Bike;
import cz.rekola.app.core.Constants;
import cz.rekola.app.core.bus.BikesAvailableEvent;
import cz.rekola.app.core.bus.BikesFailedEvent;
import cz.rekola.app.core.loc.MyLocation;
import cz.rekola.app.core.loc.MyLocationListener;
import cz.rekola.app.core.map.DirectionManager;
import cz.rekola.app.core.map.DirectionParams;
import cz.rekola.app.fragment.base.BaseMainFragment;
import cz.rekola.app.view.BikeOverlayView;

public class MapFragment extends BaseMainFragment implements MyLocationListener, BikeOverlayView.BikeOverlayListener {

    MapView vMap;
    GoogleMap map;

	@InjectView(R.id.map_overlay)
	BikeOverlayView vOverlay;

	private MapManager mapManager = new MapManager();
	private Timer timer;

	private View vView;

    @Override
    public void onResume() {
        vMap.onResume();
        super.onResume();
		getApp().getMyLocationManager().register(this);

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						getApp().getDataManager().getBikes(true); // Force update bikes.
					}
				});
			}
		}, 0, Constants.MAP_PERIODIC_UPDATE_MS); // First update right now
    }

	@Override
	public void onPause() {
		timer.cancel();
		getApp().getMyLocationManager().unregister(this);
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
		if (vView != null) {
			return vView;
		}


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
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng(), 13);
        map.moveCamera(cameraUpdate);

		vView = rootView;
        return rootView;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		if (!getApp().getDataManager().isOperational())
			return;

		vOverlay.init(this);
		mapManager.init();

		if (getApp().getDataManager().getBikes(false) != null) {
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
	public void onMyLocationChanged(MyLocation myLocation) {
	}

	@Override
	public void onMyLocationError() {
	}

	private void setupMap() {
		List<Bike> bikes = getApp().getDataManager().getBikes(false);
		if (bikes == null)
			return;

		mapManager.updateMap(bikes);
	}

	// Overlay callbacks
	@Override
	public void onClose() {
		mapManager.notifyOverlayClose();
	}

	@Override
	public void onRoutePressed() {
		mapManager.notifyRoutePressed();
	}

	@Override
	public void onBikeDetailPressed() {
		mapManager.notifyBikeDetailPressed();
	}

	@Override
	public void onHeightChanged(final int height) {
		// Map padding is not correctly updated when attached to overlay size changed event.. This is a hack-fix.
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				map.setPadding(0, 0, 0, height);
			}
		}, 100);
	}

	private class MapManager implements GoogleMap.OnMarkerClickListener, DirectionManager.DirectionsLoadedListener {

		private Map<Marker, Bike> markerMap = new HashMap<>();

		private BitmapDescriptor markerNormalBitmap;
		private BitmapDescriptor markerFocusedBitmap;

		private Marker lastMarker = null;
		private Bike lastBike = null;

		private DirectionManager directionManager = new DirectionManager(this);

		void init() {
			map.setOnMarkerClickListener(this);
			markerNormalBitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal);
			markerFocusedBitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_focused_pressed);
		}

		void updateMap(List<Bike> bikes) {
			if (lastMarker != null) {
				lastMarker.setIcon(markerNormalBitmap);
			}

			map.clear();
			markerMap.clear();
			lastMarker = null;

			Marker newMarker = null;
			for (Bike bike : bikes) {
				Marker marker = map.addMarker(new MarkerOptions()
						.position(new LatLng(bike.location.lat, bike.location.lng))
						//.alpha(0.7f)
						.title(bike.name)
						.icon(markerNormalBitmap));
				markerMap.put(marker, bike);

				if (lastBike != null && lastBike.id == bike.id) {
					newMarker = marker; // new marker after update
				}
			}

			if (newMarker == null) {
				lastBike = null;
				vOverlay.hide();
				directionManager.hideDirections();
			} else {
				lastMarker = newMarker;
				vOverlay.show(lastBike);
				lastMarker.setIcon(markerFocusedBitmap);
				lastMarker.showInfoWindow(); // Force to top
				directionManager.addDirections(map);
			}
		}

		void notifyOverlayClose() {
			lastBike = null;
			if (lastMarker != null) {
				lastMarker.setIcon(markerNormalBitmap);
				lastMarker = null;
			}
			vOverlay.hide();
			directionManager.hideDirections();
		}

		void notifyRoutePressed() {
			if (lastBike == null)
				return;

			DirectionParams params = new DirectionParams(
					getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng(),
					new LatLng(lastBike.location.lat, lastBike.location.lng),
					DirectionParams.MODE_WALKING,
					getResources().getColor(R.color.pink_1),
					getResources().getDimension(R.dimen.map_direction_path_size));

			if (getApp().getDataManager().customLoadDirections()) { // If we are not loading directions
				directionManager.loadDirections(lastBike.id, params);
			}
		}

		void notifyBikeDetailPressed() {
			if (lastBike != null) {
				getPageController().requestWebBikeDetail(lastBike.id, false);
			}
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			if (lastMarker != null) {
				lastMarker.setIcon(markerNormalBitmap);
				directionManager.hideDirections();
			}

			if (marker.equals(lastMarker)) {
				lastMarker = null;
				lastBike = null;
				vOverlay.hide();
				directionManager.hideDirections();
				return true;
			}

			marker.setIcon(markerFocusedBitmap);

			lastBike = markerMap.get(marker);
			lastMarker = marker;

			if (lastBike != null) {
				vOverlay.show(lastBike);
			}

			return false;
		}

		@Override
		public void onDirectionsLoaded() {
			getApp().getDataManager().customLoadDirectionsFinished();
			directionManager.addDirections(map);
		}

		@Override
		public void onDirectionsError() {
			getApp().getDataManager().customLoadDirectionsFinished();
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
