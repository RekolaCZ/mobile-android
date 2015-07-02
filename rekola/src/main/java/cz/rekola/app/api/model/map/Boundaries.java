package cz.rekola.app.api.model.map;

import java.util.List;

/**
 * Boundaries contain zones, they are polygons only for visualisation,
 * where can user left bike, in fact there is some boxed area (for simplify),
 * but it resolve server
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {28. 6. 2015}
 **/
public class Boundaries {
    public List<Zone> zones;
}
