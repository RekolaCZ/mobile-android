package cz.rekola.android.core.loc;

public class MyLocation {

	public final Float acc;
	public final Double lat;
	public final Double lng;

	MyLocation(float acc, double lat, double lng) {
		this.acc = acc;
		this.lat = lat;
		this.lng = lng;
	}

}
