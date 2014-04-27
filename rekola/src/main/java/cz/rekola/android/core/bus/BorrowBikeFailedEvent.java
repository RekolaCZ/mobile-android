package cz.rekola.android.core.bus;

import cz.rekola.android.api.model.error.BaseError;

/**
 * Failed to borrow a bike.
 */
public class BorrowBikeFailedEvent extends BaseErrorEvent {

	public EState state;

	public BorrowBikeFailedEvent(EState state, BaseError error) {
		super(error);
		this.state = state;
	}

	public enum EState {UNKNOWN, WRONG_CODE, FORBIDDEN, CONFLICT}

}
