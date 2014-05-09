package cz.rekola.android.fragment.natural;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.model.Poi;
import cz.rekola.android.api.model.error.MessageError;
import cz.rekola.android.api.requestmodel.ReturningBike;
import cz.rekola.android.api.requestmodel.ReturningLocation;
import cz.rekola.android.core.bus.ErrorMessageEvent;
import cz.rekola.android.core.bus.PoisAvailableEvent;
import cz.rekola.android.core.bus.PoisFailedEvent;
import cz.rekola.android.core.bus.ReturnBikeEvent;
import cz.rekola.android.core.bus.ReturnBikeFailedEvent;
import cz.rekola.android.core.data.MyBikeWrapper;
import cz.rekola.android.core.loc.MyLocation;
import cz.rekola.android.core.loc.MyLocationListener;
import cz.rekola.android.fragment.base.BaseMainFragment;

public class ReturnMapFragment extends BaseMainFragment implements /*GoogleMap.OnMyLocationButtonClickListener,*/ MyLocationListener {

	MapView vMap;
	GoogleMap map;

	private MapLocationUpdater mapLocUpdater = new MapLocationUpdater();

	@InjectView(R.id.note)
	EditText vNote;
	@InjectView(R.id.bike_returned)
	Button vReturned;

	private PoiManager pois = new PoiManager();

	@Override
	public void onResume() {
		vMap.onResume();
		super.onResume();
		getApp().getMyLocationManager().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getApp().getMyLocationManager().unregister(this);
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
		View rootView = inflater.inflate(R.layout.fragment_return_map, container, false);
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
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng(), 15);
		map.moveCamera(cameraUpdate);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		vReturned.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LatLng center = map.getCameraPosition().target;
				MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike();
				if (myBike == null || myBike.bike == null || myBike.bike.bikeCode == null ||  myBike.bike.bikeCode.length() == 0) {
					getApp().getBus().post(new ErrorMessageEvent(getResources().getString(R.string.error_unknown_borrowed_bike_code)));
					return;
				}
				// TODO: May throw NumberFormatException!
				getApp().getDataManager().returnBike(Integer.parseInt(myBike.bike.bikeCode),
						new ReturningBike(new ReturningLocation(center.latitude, center.longitude, vNote.getText().toString())));
			}
		});

		pois.init();

		if (getApp().getDataManager().getPois(true) != null) {
			setupMap();
		}
	}

	@Subscribe
	public void bikeReturned(ReturnBikeEvent event) {
		getPageController().requestWebBikeReturned(event.successUrl);
	}

	@Subscribe
	public void bikeReturnFailed(ReturnBikeFailedEvent event) {
		if (event.error != null && event.error instanceof MessageError) {
			getApp().getBus().post(new ErrorMessageEvent(((MessageError)event.error).message));
		} else {
			getApp().getBus().post(new ErrorMessageEvent(getResources().getString(R.string.error_return_bike_failed)));
		}
	}

	@Subscribe
	public void poisAvailableEvent(PoisAvailableEvent event) {
		setupMap();

	}

	@Subscribe
	public void poisFailedEvent(PoisFailedEvent event) {
	}

	/*@Override
	public boolean onMyLocationButtonClick() {
		if (bikeMarker != null) {
			setupMap();
		}

		return false;
	}*/

	/*private void setupMap() {
		map.clear();
		bikeMarker = map.addMarker(new MarkerOptions()
				.position(getApp().getMyLocationManager().getLastKnownLatLng())
				.title("TODO: Bike name")
				.draggable(true));
	}*/

	@Override
	public void onMyLocationChanged(MyLocation myLocation) {
		// TODO: This may cause location update just before a user clicks on the return button!
		// => Bike will be returned to a different position.
		mapLocUpdater.updateMapPosition(myLocation);
	}

	@Override
	public void onMyLocationError() {
	}

	private void setupMap() {
		List<Poi> poisList = getApp().getDataManager().getPois(false);
		if (pois == null)
			return;

		pois.updateMap(poisList);
	}

	private class PoiManager implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

		void init() {
			map.setOnMarkerClickListener(this);
			map.setOnInfoWindowClickListener(this);
		}

		void updateMap(List<Poi> pois) {
			map.clear();

			for (Poi poi : pois) {
				BitmapDescriptor bmp = POIS.getBmpFromType(poi.type);
				if (bmp == null)
					continue;
				map.addMarker(new MarkerOptions()
						.position(new LatLng(poi.lat, poi.lng))
						.title(poi.description)
						.icon(bmp));
			}
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			marker.showInfoWindow();
			return true;
		}

		@Override
		public void onInfoWindowClick(Marker marker) {
			// TODO: Do not move to graves positions!
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(marker.getPosition());
			map.animateCamera(cameraUpdate);
		}
	}

	private enum POIS {
		BAY("bay", R.drawable.ic_pin_normal),
		GRAVE("grave", R.drawable.ic_pin_focused_pressed);

		static BitmapDescriptor getBmpFromType(String type) {
			for (POIS p : POIS.values()) {
				if (p.type.equals(type)) {
					return p.bmp;
				}
			}
			return null;
		}

		private POIS(String type, int resourceId) {
			this.type = type;
			bmp = BitmapDescriptorFactory.fromResource(resourceId);
		}

		BitmapDescriptor bmp;
		String type;
	}

	private class MapLocationUpdater {
		private int posUpdatesCount = 2;
		private float bestAcc = 1000;

		void updateMapPosition(MyLocation myLocation) {
			if (posUpdatesCount <= 0)
				return;

			float acc = myLocation.acc == null ? bestAcc : myLocation.acc;
			if (acc > bestAcc)
				return;

			int zoomLevel = 15;
			if (acc <= 100)
				zoomLevel = 16;
			if (acc <= 40)
				zoomLevel = 17;

			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation.getLatLng(), zoomLevel);
			map.animateCamera(cameraUpdate);

			bestAcc = acc;
			posUpdatesCount--;
		}
	}
}
