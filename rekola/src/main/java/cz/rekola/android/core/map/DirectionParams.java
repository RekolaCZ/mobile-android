package cz.rekola.android.core.map;

import com.google.android.gms.maps.model.LatLng;

public class DirectionParams {

	public final static String MODE_DRIVING = "driving";
	public final static String MODE_WALKING = "walking";

	/**
	 * Unique id defining this path.
	 * Start/end may change, however the path might be the same.
	 */
	public final int id;

	public final LatLng start;
	public final LatLng end;
	public final String mode;

	public final int color;
	public final float size;

	public DirectionParams(int id, LatLng start, LatLng end, String mode, int color, float size) {
		this.id = id;
		this.start = start;
		this.end = end;
		this.mode = mode;
		this.color = color;
		this.size = size;
	}

}
