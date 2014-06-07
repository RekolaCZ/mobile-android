package cz.rekola.app.core.bus;

/**
 * Bike was returned.
 */
public class ReturnBikeEvent {

	public final String successUrl;

	public ReturnBikeEvent(String successUrl) {
		this.successUrl = successUrl;
	}

}
