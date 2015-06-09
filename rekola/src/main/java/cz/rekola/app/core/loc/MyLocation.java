package cz.rekola.app.core.loc;

import com.google.android.gms.maps.model.LatLng;

public class MyLocation {

    public final Float acc;
    public final Double lat;
    public final Double lng;

    MyLocation(float acc, double lat, double lng) {
        this.acc = acc;
        this.lat = lat;
        this.lng = lng;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

}
