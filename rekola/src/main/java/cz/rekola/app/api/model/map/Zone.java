package cz.rekola.app.api.model.map;

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

        //"coords": "14.480123900000002,50.1057677;14.479856200000002,50.1062329;...
        List<LatLng> parsedPoints = new ArrayList<>();
        String[] points = coords.split(POINTS_SEPARATOR);
        for (int i = 0; i < points.length; i++) {
            String[] coords = points[i].split(COORDS_SEPARATOR);
            double lng = Double.valueOf(coords[0]);
            double lat = Double.valueOf(coords[1]);
            parsedPoints.add(new LatLng(lat, lng));
        }
        return parsedPoints;
    }
}
