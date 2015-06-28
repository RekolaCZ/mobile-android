package cz.rekola.app.core.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

import cz.rekola.app.R;
import cz.rekola.app.api.model.map.Zone;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {28. 6. 2015}
 **/
public class ZonesManager {
    public static final String TAG = ZonesManager.class.getName();

    public static void drawTestZone(Context context, GoogleMap map) {
        List<Zone> zones = new ArrayList<>();
        Zone zone = new Zone();
        zone.name = "Praha";
        zone.coords = "50.442224,13.829746;50.700395,16.702671;48.993308,13.796787";
        zones.add(zone);
        ZonesManager.drawZones(context, map, zones);
    }

    public static void drawZones(Context context, GoogleMap map, List<Zone> zones) {
        for (Zone zone : zones) {
            map.addPolygon(getZonePolygonOptions(context, zone.getCoords()));
        }
    }


    private static PolygonOptions getZonePolygonOptions(Context context, List<LatLng>
            zonesPoints) {
        int colorPinkTransparent = context.getResources().getColor(R.color.pink_transparent);
        int colorPink = context.getResources().getColor(R.color.base_pink);

        PolygonOptions zone = new PolygonOptions();
        zone.fillColor(colorPinkTransparent);
        zone.strokeColor(colorPink);
        zone.addAll(zonesPoints);
        return zone;
    }


}
