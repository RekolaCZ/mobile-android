package cz.rekola.app.core.map.Cluster;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import cz.rekola.app.api.model.bike.Bike;

/**
 * Bike item for grouping bikes on Google Map
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {26. 6. 2015}
 */
public class BikeClusterItem implements ClusterItem {
    public static final String TAG = BikeClusterItem.class.getName();

    private final LatLng mPosition;
    private final Bike mBike;
    private boolean isSelected;  //because of different color for last selected bike

    /**
     * Piccaso use weak reference for every target, so it need to get reference, otherwise it
     * // would be garbage collected
     */
    private PicassoMarker mPicassoMarker;

    public BikeClusterItem(Bike bike) {
        mBike = bike;
        mPosition = new LatLng(bike.location.lat, bike.location.lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public Bike getBike() {
        return mBike;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BikeClusterItem that = (BikeClusterItem) o;

        return !(mPosition != null ? !mPosition.equals(that.mPosition) : that.mPosition != null);
    }

    public boolean isSelectedBike() {
        return isSelected;
    }

    public void setIsSelectedBike(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setPicassoMarker(PicassoMarker picassoMarker) {
        mPicassoMarker = picassoMarker;
    }

    @Override
    public int hashCode() {
        return mPosition != null ? mPosition.hashCode() : 0;
    }
}
