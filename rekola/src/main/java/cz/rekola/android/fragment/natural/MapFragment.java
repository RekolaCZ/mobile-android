package cz.rekola.android.fragment.natural;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.core.Constants;
import cz.rekola.android.core.bus.BikesAvailableEvent;
import cz.rekola.android.core.bus.BikesFailedEvent;
import cz.rekola.android.core.loc.MyLocation;
import cz.rekola.android.core.loc.MyLocationListener;
import cz.rekola.android.core.map.DirectionManager;
import cz.rekola.android.core.map.DirectionParams;
import cz.rekola.android.fragment.base.BaseMainFragment;

public class MapFragment extends BaseMainFragment implements MyLocationListener {

    MapView vMap;
    GoogleMap map;

	@InjectView(R.id.map_overlay)
	LinearLayout vOverlay;

	@InjectView(R.id.map_overlay_area)
	LinearLayout vOverlayArea;

	@InjectView(R.id.map_overlay_close)
	ImageView vClose;

	@InjectView(R.id.map_overlay_name)
	TextView vName;

	@InjectView(R.id.map_overlay_street)
	TextView vStreet;

	@InjectView(R.id.map_overlay_note)
	TextView vNote;

	@InjectView(R.id.map_overlay_description)
	TextView vDescription;

	@InjectView(R.id.map_overlay_route)
	ImageView vRoute;

	@InjectView(R.id.map_overlay_bike_detail)
	ImageView vBikeDetail;

	private MarkerManager markers = new MarkerManager();
	private OverlayManager overlay = new OverlayManager();
	private DirectionManager directionManager;
	private Timer timer;

    @Override
    public void onResume() {
        vMap.onResume();
        super.onResume();
		getApp().getMyLocationManager().register(this);

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				getApp().getDataManager().getBikes(true); // Force update bikes.
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
		directionManager = new DirectionManager(map);

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng(), 13);
        map.moveCamera(cameraUpdate);

        return rootView;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		markers.init();
		overlay.init();

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

		markers.updateMap(bikes);
	}

	/*private void setDirections(Bike bike) {
		DirectionParams params = new DirectionParams(
				bike.id, // Path to this bike
				getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng(),
				new LatLng(bike.location.lat, bike.location.lng),
				DirectionParams.MODE_WALKING,
				getResources().getColor(R.color.pink_1),
				getResources().getDimension(R.dimen.map_direction_path_size));

		directionManager.loadDirections(params);
	}

	private void clearDirections() {
		directionManager.clearDirections();
	}*/

	private class MarkerManager implements GoogleMap.OnMarkerClickListener {

		private Map<Marker, Bike> markerMap = new HashMap<>();

		private BitmapDescriptor markerNormalBitmap;
		private BitmapDescriptor markerFocusedBitmap;

		private Marker lastMarker = null;
		private Bike lastBike = null;

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
				overlay.hide();
				directionManager.hideDirections();
			} else {
				lastMarker = newMarker;
				overlay.show(lastBike);
				lastMarker.setIcon(markerFocusedBitmap);
				lastMarker.showInfoWindow(); // Force to top
				directionManager.addDirectionsIfAvailable(lastBike.id);
			}
		}

		void notifyOverlayClose() {
			lastBike = null;
			if (lastMarker != null) {
				lastMarker.setIcon(markerNormalBitmap);
				lastMarker = null;
			}
			overlay.hide();
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

			directionManager.loadDirections(lastBike.id, params);
		}

		void notifyBikeDetailPressed() {
			if (lastBike != null)
				getPageController().requestWebBikeDetail(lastBike.id);
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
				overlay.hide();
				directionManager.hideDirections();
				return true;
			}

			marker.setIcon(markerFocusedBitmap);

			lastBike = markerMap.get(marker);
			lastMarker = marker;

			if (lastBike != null) {
				overlay.show(lastBike);
				directionManager.addDirectionsIfAvailable(lastBike.id);
			}

			return false;
		}
	}

	private class OverlayManager {

		void init() {
			vClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					markers.notifyOverlayClose();
				}
			});

			vRoute.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					markers.notifyRoutePressed();
				}
			});

			vBikeDetail.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					markers.notifyBikeDetailPressed();
				}
			});
		}

		public void show(Bike bike) {
			vName.setText(bike.name + ", " + bike.location.distance);
			vStreet.setText(bike.location.address);
			vNote.setText(bike.location.note);
			vDescription.setText(bike.description);
			vOverlay.setVisibility(View.VISIBLE);

			// Hack-adjust the map controls
			int height = vOverlayArea.getHeight();
			if (height == 0) {
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						int height = vOverlayArea.getHeight();
						if (height != 0)
							map.setPadding(0, 0, 0, height);
					}
				}, 100);
			} else {
				map.setPadding(0, 0, 0, height);
			}
		}

		public void hide() {
			vOverlay.setVisibility(View.GONE);
			map.setPadding(0, 0, 0, 0);
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
