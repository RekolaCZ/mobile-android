package cz.rekola.app.fragment.natural;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

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
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.api.model.error.MessageError;
import cz.rekola.app.api.model.map.Poi;
import cz.rekola.app.api.requestmodel.ReturningBike;
import cz.rekola.app.api.requestmodel.ReturningLocation;
import cz.rekola.app.core.Constants;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.core.bus.dataAvailable.PoisAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.ReturnBikeEvent;
import cz.rekola.app.core.bus.dataFailed.PoisFailedEvent;
import cz.rekola.app.core.bus.dataFailed.ReturnBikeFailedEvent;
import cz.rekola.app.core.data.MyBikeWrapper;
import cz.rekola.app.core.loc.MyLocation;
import cz.rekola.app.core.loc.MyLocationListener;
import cz.rekola.app.fragment.base.BaseMainFragment;

public class ReturnMapFragment extends BaseMainFragment implements /*GoogleMap.OnMyLocationButtonClickListener,*/ MyLocationListener {

    @InjectView(R.id.txt_note)
    EditText mTxtNote;
    @InjectView(R.id.view_map)
    MapView mViewMap;

    private GoogleMap mGoogleMap;

    private MapLocationUpdater mapLocUpdater = new MapLocationUpdater();


    private PoiManager pois = new PoiManager();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_return_map, container, false);
        ButterKnife.inject(this, rootView);

        mViewMap.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mGoogleMap = mViewMap.getMap();
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        centerMapOnMyLocation(false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!getApp().getDataManager().isOperational())
            return;

        pois.init();

        if (getApp().getDataManager().getPois(true) != null) {
            setupMap();
        }

        mTxtNote.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mTxtNote.setSingleLine(true);
        mTxtNote.setLines(2);
        mTxtNote.setHorizontallyScrolling(false);
        mTxtNote.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    @OnClick(R.id.btn_return_bike)
    public void returnBikeOnClick() {
        LatLng center = mGoogleMap.getCameraPosition().target;
        MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike(false);
        if (myBike == null || myBike.bike == null || myBike.bike.bikeCode == null || myBike.bike.bikeCode.length() == 0) {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_unknown_borrowed_bike_code)));
            return;
        }

        getAct().hideKeyboard();

        MyLocation location = getApp().getMyLocationManager().getLastKnownMyLocation();

        ReturningLocation returningLocation = new ReturningLocation();
        returningLocation.lat = center.latitude;
        returningLocation.lng = center.longitude;
        returningLocation.sensorLat = location.lat;
        returningLocation.sensorLng = location.lng;
        returningLocation.sensorAccuracy = location.acc;
        returningLocation.note = mTxtNote.getText().toString();

        ReturningBike returningBike = new ReturningBike(returningLocation);

        getApp().getDataManager().returnBike(myBike.bike.id, returningBike);
    }

    @OnClick(R.id.btn_center_map)
    public void centerMapOnClick() {
        centerMapOnMyLocation(true);
    }

    @Override
    public void onResume() {
        mViewMap.onResume();
        super.onResume();
        getApp().getMyLocationManager().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getApp().getMyLocationManager().unregister(this);
        mViewMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mViewMap != null)
            mViewMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mViewMap.onLowMemory();
    }


    @Subscribe
    public void bikeReturned(ReturnBikeEvent event) {
        getPageController().requestWebBikeReturned(event.successUrl);
    }

    @Subscribe
    public void bikeReturnFailed(ReturnBikeFailedEvent event) {
        if (event.error != null && event.error instanceof MessageError) {
            getApp().getBus().post(new MessageEvent(((MessageError) event.error).message));
        } else {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_return_bike_failed)));
        }
    }

    @Subscribe
    public void poisAvailableEvent(PoisAvailableEvent event) {
        setupMap();

    }

    @Subscribe
    public void poisFailedEvent(PoisFailedEvent event) {
    }

    @Override
    public void onMyLocationChanged(MyLocation myLocation) {
        // TODO: This may cause location update just before a user clicks on the return button!
        // => Bike will be returned to a different position.
        mapLocUpdater.updateMapPosition(myLocation);
    }

    @Override
    public void onMyLocationError() {
    }

    private void centerMapOnMyLocation(boolean animate) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng(), Constants.DEFAULT_RETURN_MAP_ZOOM_LEVEL);
        if (animate)
            mGoogleMap.animateCamera(cameraUpdate);
        else
            mGoogleMap.moveCamera(cameraUpdate);
    }

    private void setupMap() {
        List<Poi> poisList = getApp().getDataManager().getPois(false);
        if (pois == null)
            return;

        pois.updateMap(poisList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private class PoiManager implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

        void init() {
            mGoogleMap.setOnMarkerClickListener(this);
            mGoogleMap.setOnInfoWindowClickListener(this);
        }

        void updateMap(List<Poi> pois) {
            mGoogleMap.clear();

            for (Poi poi : pois) {
                BitmapDescriptor bmp = POIS.getBmpFromType(poi.type);
                if (bmp == null)
                    continue;
                mGoogleMap.addMarker(new MarkerOptions()
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
            // TODO: Put bay name into the note field if not present
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(marker.getPosition());
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    private enum POIS {
        BAY("bay", R.drawable.ic_pin_parking_normal),
        GRAVE("grave", R.drawable.ic_pin_grave_normal),
        NO_PARKING("noparking", R.drawable.ic_pin_noparking_normal);

        static BitmapDescriptor getBmpFromType(String type) {
            for (POIS p : POIS.values()) {
                if (p.type.equals(type)) {
                    return p.bmp;
                }
            }
            return null;
        }

        POIS(String type, int resourceId) {
            this.type = type;
            bmp = BitmapDescriptorFactory.fromResource(resourceId);
        }

        BitmapDescriptor bmp;
        String type;
    }

    private class MapLocationUpdater {
        private int posUpdatesCount = 1;
        private float bestAcc = 100000;

        void updateMapPosition(MyLocation myLocation) {
            if (posUpdatesCount <= 0)
                return;

            posUpdatesCount--;

            float acc = myLocation.acc == null ? bestAcc : myLocation.acc;
            if (acc >= bestAcc)
                return;

            int zoomLevel = 15;
            if (acc <= 100)
                zoomLevel = 16;
            if (acc <= 40)
                zoomLevel = 17;

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation.getLatLng(), zoomLevel);
            mGoogleMap.animateCamera(cameraUpdate);

            bestAcc = acc;
        }
    }
}
