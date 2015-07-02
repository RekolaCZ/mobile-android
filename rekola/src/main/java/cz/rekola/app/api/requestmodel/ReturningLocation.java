package cz.rekola.app.api.requestmodel;

public class ReturningLocation {

    /**
     * Location of bike, which user want returned
     */
    public double lat;
    public double lng;
    public double sensorLat;
    public double sensorLng;
    public float sensorAccuracy;
    public String note;
}
