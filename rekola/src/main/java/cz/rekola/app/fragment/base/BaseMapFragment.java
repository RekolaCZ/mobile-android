package cz.rekola.app.fragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.map.Boundaries;
import cz.rekola.app.core.Constants;
import cz.rekola.app.core.loc.MyLocationListener;
import cz.rekola.app.core.map.ZonesManager;

/**
 * Base fragment for map fragments
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {23. 7. 2015}
 */

public abstract class BaseMapFragment extends BaseMainFragment implements MyLocationListener {

    @InjectView(R.id.view_map)
    protected MapView mViewMap;

    protected boolean mMapIsReady = false;
    protected GoogleMap mGoogleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(getMapViewResource(), container, false);
        ButterKnife.inject(this, rootView);
        final BaseMapFragment baseMapFragment = this;

        mViewMap.onCreate(savedInstanceState);
        mViewMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMapIsReady = true;
                mGoogleMap = googleMap;
                setUpMap();
                baseMapFragment.onMapReady();
                setUpData();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        mViewMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        if (!getApp().getDataManager().isOperational())
            return;
    }

    protected void setUpMap() {
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        centerMapOnMyLocation(false);
    }


    /**
     * get map layout to init view
     *
     * @return resource id, e.g. R.layout.fragment_map
     */
    protected abstract int getMapViewResource();

    /**
     * init map data
     */
    protected abstract void setUpData();

    /**
     * called when map is ready to use
     */
    protected void onMapReady() {

    }

    protected void centerMapOnMyLocation(boolean animate) {
        if (!mMapIsReady)
            return;

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp()
                        .getMyLocationManager().getLastKnownMyLocation().getLatLng(),
                Constants.DEFAULT_BIKES_MAP_ZOOM_LEVEL);
        if (animate)
            mGoogleMap.animateCamera(cameraUpdate);
        else
            mGoogleMap.moveCamera(cameraUpdate);
    }

    protected void setZones() {
        Boundaries boundaries = getApp().getDataManager().getBoundaries();
        if (boundaries == null) {
            return;
        }

        ZonesManager.drawZones(getActivity(), mGoogleMap, boundaries.zones);
    }
}
