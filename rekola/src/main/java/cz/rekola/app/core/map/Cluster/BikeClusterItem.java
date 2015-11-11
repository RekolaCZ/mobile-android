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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BikeClusterItem that = (BikeClusterItem) o;

        if (getPosition() != null ? !getPosition().equals(that.getPosition()) : that.getPosition() != null)
            return false;
        return !(getBike() != null ? !getBike().equals(that.getBike()) : that.getBike() != null);

    }

    @Override
    public int hashCode() {
        int result = getPosition() != null ? getPosition().hashCode() : 0;
        result = 31 * result + (getBike() != null ? getBike().hashCode() : 0);
        return result;
    }

    public Bike getBike() {
        return mBike;
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


}
