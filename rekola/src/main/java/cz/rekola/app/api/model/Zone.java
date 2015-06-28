package cz.rekola.app.api.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {28. 6. 2015}
 **/
public class Zone {

    private static String POINTS_SEPARATOR = ";";
    private static String COORDS_SEPARATOR = ",";

    public String name;
    public String coords;

    public List<LatLng> getCoords() {

        //"coords": "14.4156831,50.0663613;14.4156831,50.0663613
        List<LatLng> parsedPoints = new ArrayList<>();
        String[] points = coords.split(POINTS_SEPARATOR);
        for (int i = 0; i < points.length; i++) {
            String[] coords = points[i].split(COORDS_SEPARATOR);
            double lat = Double.valueOf(coords[0]);
            double lng = Double.valueOf(coords[1]);
            parsedPoints.add(new LatLng(lat, lng));
        }
        return parsedPoints;
    }
}
