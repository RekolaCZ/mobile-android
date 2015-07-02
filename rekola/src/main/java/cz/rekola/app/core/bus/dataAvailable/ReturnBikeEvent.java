package cz.rekola.app.core.bus.dataAvailable;

/**
 * Bike was returned.
 */
public class ReturnBikeEvent {

    public final String successUrl;

    public ReturnBikeEvent(String successUrl) {
        this.successUrl = successUrl;
    }

}
