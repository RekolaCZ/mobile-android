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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Bike;
import cz.rekola.app.api.model.map.Boundaries;
import cz.rekola.app.core.Constants;
import cz.rekola.app.core.bus.dataAvailable.BikesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.BoundariesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.ReturnBikeEvent;
import cz.rekola.app.core.bus.dataFailed.BikesFailedEvent;
import cz.rekola.app.core.loc.MyLocation;
import cz.rekola.app.core.loc.MyLocationListener;
import cz.rekola.app.core.map.Cluster.BikeClusterItem;
import cz.rekola.app.core.map.Cluster.BikeRenderer;
import cz.rekola.app.core.map.DirectionManager;
import cz.rekola.app.core.map.DirectionParams;
import cz.rekola.app.core.map.ZonesManager;
import cz.rekola.app.fragment.base.BaseMainFragment;
import cz.rekola.app.view.BikeOverlayView;

public class MapFragment extends BaseMainFragment implements MyLocationListener, BikeOverlayView.BikeOverlayListener {

    @InjectView(R.id.overlay_map)
    BikeOverlayView mOverlayMap;
    @InjectView(R.id.view_map)
    MapView mViewMap;

    private GoogleMap mGooglemap;
    private MapManager mapManager = new MapManager();
    private Timer timer;
    private View mMapFragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mMapFragmentView != null) { //some hack because of problem with google map
            return mMapFragmentView;
        }

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.inject(this, rootView);

        mViewMap.onCreate(savedInstanceState);

        mGooglemap = mViewMap.getMap();
        mGooglemap.getUiSettings().setMyLocationButtonEnabled(false);
        mGooglemap.getUiSettings().setZoomControlsEnabled(false);
        mGooglemap.setMyLocationEnabled(true);
        mGooglemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGooglemap.setInfoWindowAdapter(new MapWindowAdapter(getActivity())); // Adapter creating invisible info windows to force the marker to move to front.

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        centerMapOnMyLocation(false);

        mMapFragmentView = rootView;
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!getApp().getDataManager().isOperational())
            return;

        mOverlayMap.init(this);
        mapManager.init();

        if (getApp().getDataManager().getBikes(false) != null) {
            setupMap();
        }

        setZones();
    }


    @Override
    public void onResume() {
        mViewMap.onResume();
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

    @Subscribe
    public void bikesAvailable(BikesAvailableEvent event) {
        setupMap();
    }

    @Subscribe
    public void bikesFailed(BikesFailedEvent event) {

    }

    @Subscribe
    public void bikeReturned(ReturnBikeEvent event) {
        getApp().getDataManager().getBikes(true); // Force update bikes.
    }

    @Subscribe
    public void boundariesAvaible(BoundariesAvailableEvent event) {
        setZones();
    }

    @Override
    public void onMyLocationChanged(MyLocation myLocation) {
    }

    @Override
    public void onMyLocationError() {
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
    public void onCenterMapPressed() {
        centerMapOnMyLocation(true);
    }

    @Override
    public void onHeightChanged(final int height) {
        // Map padding is not correctly updated when attached to overlay size changed event.. This is a hack-fix.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGooglemap.setPadding(0, 0, 0, height);
            }
        }, 100);
    }

    private void centerMapOnMyLocation(boolean animate) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(getApp()
                        .getMyLocationManager().getLastKnownMyLocation().getLatLng(),
                Constants.DEFAULT_BIKES_MAP_ZOOM_LEVEL);
        if (animate)
            mGooglemap.animateCamera(cameraUpdate);
        else
            mGooglemap.moveCamera(cameraUpdate);
    }

    private void setupMap() {
        List<Bike> bikes = getApp().getDataManager().getBikes(false);
        if (bikes == null)
            return;

        mapManager.updateMap(bikes);
    }

    private void setZones() {
        Boundaries boundaries = getApp().getDataManager().getBoundaries();
        if (boundaries == null) {
            return;
        }

        ZonesManager.drawZones(getActivity(), mGooglemap, boundaries.zones);
    }

    private class MapManager implements ClusterManager.OnClusterItemClickListener<BikeClusterItem>, ClusterManager.OnClusterClickListener<BikeClusterItem>,
            DirectionManager.DirectionsLoadedListener {

        private ClusterManager<BikeClusterItem> mClusterManager;
        private BikeRenderer mBikeRenderer;
        private BikeClusterItem lastBikeClusterItem = null;
        private DirectionManager directionManager = new DirectionManager(this); //navigation to bike

        void init() {
            mClusterManager = new ClusterManager<>(getActivity(), mGooglemap);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterClickListener(this);

            mBikeRenderer = new BikeRenderer(getActivity(), getActivity()
                    .getLayoutInflater(), mGooglemap, mClusterManager);
            mClusterManager.setRenderer(mBikeRenderer);

            // Point the map's listeners at the listeners implemented by the cluster
            // manager.
            mGooglemap.setOnCameraChangeListener(mClusterManager);
            mGooglemap.setOnMarkerClickListener(mClusterManager);
        }

        void updateMap(List<Bike> bikes) {
            //    mGooglemap.clear();
            mClusterManager.clearItems();

            lastBikeClusterItem = null;
            BikeClusterItem newBikeClusterItem = null;

            for (Bike bike : bikes) {
                BikeClusterItem bikeClusterItem = new BikeClusterItem(bike);
                mClusterManager.addItem(bikeClusterItem);

                if (lastBikeClusterItem != null && lastBikeClusterItem.getBike().id == bike.id) {
                    newBikeClusterItem = bikeClusterItem; // new marker after update
                    lastBikeClusterItem = bikeClusterItem;
                }
            }
            mClusterManager.cluster(); //will draw icons

            if (newBikeClusterItem == null) {
                mOverlayMap.hide();
                directionManager.hideDirections();
            } else {
                lastBikeClusterItem = newBikeClusterItem;
                mOverlayMap.show(lastBikeClusterItem.getBike());
                directionManager.addDirections(mGooglemap);
            }
        }

        void notifyOverlayClose() {
            lastBikeClusterItem = null;
            mOverlayMap.hide();
            directionManager.hideDirections();
        }

        void notifyRoutePressed() {
            if (lastBikeClusterItem == null)
                return;

            Bike lastBike = lastBikeClusterItem.getBike();
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
            if (lastBikeClusterItem != null) {
                getPageController().requestBikeDetail(lastBikeClusterItem.getBike().id);
            }
        }

        @Override
        public void onDirectionsLoaded() {
            getApp().getDataManager().customLoadDirectionsFinished(true);
            directionManager.addDirections(mGooglemap);
        }

        @Override
        public void onDirectionsError() {
            getApp().getDataManager().customLoadDirectionsFinished(false);
        }

        @Override
        public boolean onClusterItemClick(BikeClusterItem bikeClusterItem) {
            directionManager.hideDirections();
            //clicked on same the bike
            if (bikeClusterItem.equals(lastBikeClusterItem)) {
                if (bikeClusterItem.isSelectedBike()) {
                    setBikeUnselected(bikeClusterItem);
                    mOverlayMap.hide();
                } else {
                    setBikeSelected(bikeClusterItem);
                    mOverlayMap.show(bikeClusterItem.getBike());
                }
                return true;
            }
            //else deselect last bike and select new bike
            else {
                if (lastBikeClusterItem != null) {
                    setBikeUnselected(lastBikeClusterItem);
                }
                setBikeSelected(bikeClusterItem);
            }

            lastBikeClusterItem = bikeClusterItem;
            mOverlayMap.show(lastBikeClusterItem.getBike());

            return false;
        }

        @Override
        public boolean onClusterClick(Cluster<BikeClusterItem> cluster) {
            mGooglemap.moveCamera(CameraUpdateFactory.newLatLng(cluster.getPosition()));
            mGooglemap.animateCamera(CameraUpdateFactory.zoomIn());
            return true;
        }

        private void setBikeSelected(BikeClusterItem bikeClusterItem) {
            bikeClusterItem.setIsSelectedBike(true);
            mBikeRenderer.setIconSelected(bikeClusterItem);
        }

        private void setBikeUnselected(BikeClusterItem bikeClusterItem) {
            bikeClusterItem.setIsSelectedBike(false);
            mBikeRenderer.setIconUnselected(bikeClusterItem);
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
            return ((Activity) context).getLayoutInflater().inflate(R.layout.map_invisible_info, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
