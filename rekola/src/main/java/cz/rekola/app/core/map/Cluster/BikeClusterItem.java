package cz.rekola.app.core.map.Cluster;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import cz.rekola.app.api.model.Bike;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {26. 6. 2015}
 */
public class BikeClusterItem implements ClusterItem {
    public static final String TAG = BikeClusterItem.class.getName();

    private final LatLng mPosition;
    private final Bike mBike;

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

    @Override
    public int hashCode() {
        return mPosition != null ? mPosition.hashCode() : 0;
    }
}
