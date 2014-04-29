package cz.rekola.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.loc.MyLocation;
import cz.rekola.android.core.loc.MyLocationListener;

public class ReturnMapFragment extends BaseMainFragment implements GoogleMap.OnMyLocationButtonClickListener, MyLocationListener {

	MapView vMap;
	GoogleMap map;

	@InjectView(R.id.return_bike)
	Button vReturn;

	private Marker bikeMarker;

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
		map.setOnMyLocationButtonClickListener(this);

		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
		MapsInitializer.initialize(this.getActivity());

		// Updates the location and zoom of the MapView
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp().getMyLocationManager().getLastKnownLatLng(), 17);
		map.moveCamera(cameraUpdate);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);
		setupMap();
	}

	@Override
	public boolean onMyLocationButtonClick() {
		if (bikeMarker != null) {
			setupMap();
		}

		return false;
	}

	private void setupMap() {
		map.clear();
		bikeMarker = map.addMarker(new MarkerOptions()
				.position(getApp().getMyLocationManager().getLastKnownLatLng())
				.title("TODO: Bike name")
				.draggable(true));
	}

	@Override
	public void onMyLocationChanged(MyLocation myLocation) {
		setupMap();
	}

	@Override
	public void onMyLocationError() {

	}
}
