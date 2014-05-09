package cz.rekola.android.core.bus;

/**
 * Bike was returned.
 */
public class ReturnBikeEvent {

	public final String successUrl;

	public ReturnBikeEvent(String successUrl) {
		this.successUrl = successUrl;
	}

}
