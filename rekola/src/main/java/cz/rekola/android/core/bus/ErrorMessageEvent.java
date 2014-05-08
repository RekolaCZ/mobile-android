package cz.rekola.android.core.bus;

/**
 * Created by palo on 08/05/14.
 */
public class ErrorMessageEvent {

	public final String message;

	public ErrorMessageEvent(String message) {
		this.message = message;
	}
}
