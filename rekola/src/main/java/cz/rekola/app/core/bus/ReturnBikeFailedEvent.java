package cz.rekola.app.core.bus;

import cz.rekola.app.api.model.error.BaseError;

/**
 * Failed to return the bike.
 */
public class ReturnBikeFailedEvent extends BaseErrorEvent {

	public EState state;

	public ReturnBikeFailedEvent(EState state, BaseError error) {
		super(error);
		this.state = state;
	}

	public enum EState {UNKNOWN, CONFLICT}
}
