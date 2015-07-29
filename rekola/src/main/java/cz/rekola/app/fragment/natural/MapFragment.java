package cz.rekola.app.fragment.natural;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import cz.rekola.app.core.map.Cluster.BikeClusterItem;
import cz.rekola.app.core.map.Cluster.BikeRenderer;
import cz.rekola.app.core.map.DirectionManager;
import cz.rekola.app.core.map.DirectionParams;
import cz.rekola.app.core.map.ZonesManager;
import cz.rekola.app.fragment.base.BaseMapFragment;
import cz.rekola.app.view.BikeOverlayView;

public class MapFragment extends BaseMapFragment implements BikeOverlayView.BikeOverlayListener {
    public static final String TAG = MapFragment.class.getName();

    @InjectView(R.id.overlay_map)
    BikeOverlayView mOverlayMap;

    private MapManager mapManager = new MapManager();
    private Timer timer;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOverlayMap.init(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getApp().getMyLocationManager().register(this);
        startTimer();
    }

    @Override
    public void onPause() {
        cancelTimer();
        getApp().getMyLocationManager().unregister(this);
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden && timer != null) {
            cancelTimer();
        } else {
            startTimer();
        }
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
    public void onMyLocationChanged(MyLocation myLocation) {
    }

    @Override
    public void onMyLocationError() {
    }

    // Override BaseMapFragment

    @Override
    protected int getMapViewResource() {
        return R.layout.fragment_map;
    }


    @Override
    protected void setUpData() {
        setUpBikes();
        setUpZones();
    }

    @Override
    protected void onMapReady() {
        mapManager.init();

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mOverlayMap.isVisible())
                    mOverlayMap.hide();
            }
        });
    }

    @Subscribe
    public void bikesAvailable(BikesAvailableEvent event) {
        setUpBikes();
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
        setUpZones();
    }


    private void startTimer() {
        if (!mMapIsReady)
            return;
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

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }


    private void setUpBikes() {
        List<Bike> bikes = getApp().getDataManager().getBikes(false);
        if (bikes == null)
            return;

        mapManager.updateMap(bikes);
    }

    private void setUpZones() {
        Boundaries boundaries = getApp().getDataManager().getBoundaries();
        if (boundaries == null) {
            return;
        }

        ZonesManager.drawZones(getActivity(), mGoogleMap, boundaries.zones);
    }

    private class MapManager implements ClusterManager.OnClusterItemClickListener<BikeClusterItem>, ClusterManager.OnClusterClickListener<BikeClusterItem>,
            DirectionManager.DirectionsLoadedListener {

        private ClusterManager<BikeClusterItem> mClusterManager;
        private BikeRenderer mBikeRenderer;
        private BikeClusterItem lastBikeClusterItem = null;
        private DirectionManager directionManager = new DirectionManager(this); //navigation to bike

        void init() {
            mClusterManager = new ClusterManager<>(getActivity(), mGoogleMap);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterClickListener(this);

            mBikeRenderer = new BikeRenderer(getActivity(), getActivity()
                    .getLayoutInflater(), mGoogleMap, mClusterManager);
            mClusterManager.setRenderer(mBikeRenderer);

            // Point the map's listeners at the listeners implemented by the cluster
            // manager.
            mGoogleMap.setOnCameraChangeListener(mClusterManager);
            mGoogleMap.setOnMarkerClickListener(mClusterManager);
        }

        void updateMap(List<Bike> bikes) {
            //    mGoogleMap.clear();
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
                directionManager.addDirections(mGoogleMap);
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
                    getResources().getColor(R.color.navigation_color),
                    getResources().getDimension(R.dimen.map_direction_path_size));

            if (getApp().getDataManager().customLoadDirections()) { // If we are not loading directions
                directionManager.loadDirections(lastBike.id, params);
            }
        }

        void notifyBikeDetailPressed() {
            if (lastBikeClusterItem != null) {
                Bike bike = lastBikeClusterItem.getBike();
                getPageController().requestBikeDetail(bike.id, bike.issues.size() > 0);
            }
        }

        @Override
        public void onDirectionsLoaded() {
            getApp().getDataManager().customLoadDirectionsFinished(true);
            directionManager.addDirections(mGoogleMap);
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
            //center to cluster position + zoom in
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),
                    (float) Math.floor(mGoogleMap.getCameraPosition().zoom + 1));
            mGoogleMap.animateCamera(cameraUpdate);

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
