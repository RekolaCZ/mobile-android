package cz.rekola.app.core.bus;

/**
 * Created by palo on 08/05/14.
 */
public class MessageEvent {

	public final MessageType type;

	public final String message;

	public MessageEvent(String message) {
		this.type = MessageType.ERROR;
		this.message = message;
	}

	public MessageEvent(MessageType type, String message) {
		this.type = type;
		this.message = message;
	}

	public enum MessageType {
		ERROR,
		SUCCESS
	}
}
