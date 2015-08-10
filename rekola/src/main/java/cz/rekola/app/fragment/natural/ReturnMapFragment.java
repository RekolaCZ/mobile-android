package cz.rekola.app.fragment.natural;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.api.model.error.MessageError;
import cz.rekola.app.api.model.map.Poi;
import cz.rekola.app.api.requestmodel.ReturningBike;
import cz.rekola.app.api.requestmodel.ReturningLocation;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.core.bus.dataAvailable.BoundariesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.PoisAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.ReturnBikeEvent;
import cz.rekola.app.core.bus.dataFailed.PoisFailedEvent;
import cz.rekola.app.core.bus.dataFailed.ReturnBikeFailedEvent;
import cz.rekola.app.core.data.MyBikeWrapper;
import cz.rekola.app.core.loc.MyLocation;
import cz.rekola.app.fragment.base.BaseMapFragment;
import cz.rekola.app.utils.KeyboardUtils;

public class ReturnMapFragment extends BaseMapFragment {

    @InjectView(R.id.txt_note)
    EditText mTxtNote;
    @InjectView(R.id.img_bike_icon)
    ImageView mImgBikeIcon;

    private MapLocationUpdater mapLocUpdater = new MapLocationUpdater();
    private PoiManager pois = new PoiManager();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTxtNote.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mTxtNote.setSingleLine(true);
        mTxtNote.setLines(2);
        mTxtNote.setHorizontallyScrolling(false);
        mTxtNote.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    @Override
    protected int getMapViewResource() {
        return R.layout.fragment_return_map;
    }

    @Override
    protected void setUpData() {
        MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike(false);
        if (myBike != null && myBike.bike != null) { //should be in cache, but for sure
            Glide.with(getActivity()).load(myBike.bike.iconUrl).into(mImgBikeIcon);
        }

        pois.init();
        setUpPois(true);
        setZones();
    }

    @OnClick(R.id.btn_return_bike)
    public void returnBikeOnClick() {
        if (!mMapIsReady) {
            return;
        }

        if (mTxtNote.getText().toString().equals("")) {
            showDialog();
            return;
        }

        LatLng center = mGoogleMap.getCameraPosition().target;
        MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike(false);
        if (myBike == null || myBike.bike == null || myBike.bike.bikeCode == null || myBike.bike.bikeCode.length() == 0) {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_unknown_borrowed_bike_code)));
            return;
        }

        KeyboardUtils.hideKeyboard(getActivity());

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
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            centerMapOnMyLocation(true);
        }
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
        setUpPois(false);

    }

    @Subscribe
    public void poisFailedEvent(PoisFailedEvent event) {
    }

    @Subscribe
    public void boundariesAvaible(BoundariesAvailableEvent event) {
        setZones();
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

    private void showDialog() {
        String text = getString(R.string.returnmap_dialog_position);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        //  alertDialogBuilder.setTitle("Your Title");

        // set dialog message
        alertDialogBuilder
                .setMessage(text)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void setUpPois(boolean forceUpdate) {
        List<Poi> poisList = getApp().getDataManager().getPois(forceUpdate);
        if (pois == null || poisList == null)
            return;

        pois.updateMap(poisList);
    }


    private class PoiManager implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

        void init() {
            mGoogleMap.setOnMarkerClickListener(this);
            mGoogleMap.setOnInfoWindowClickListener(this);
        }

        void updateMap(List<Poi> pois) {
            //   mGoogleMap.clear();

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
        BAY("bay", R.drawable.ic_pin_parking),
        GRAVE("grave", R.drawable.ic_pin_grave),
        NO_PARKING("noparking", R.drawable.ic_pin_noparking);

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
