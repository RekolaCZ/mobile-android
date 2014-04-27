package cz.rekola.android.core.bus;

import cz.rekola.android.api.model.error.BaseError;

/**
 * Created by palo on 27/04/14.
 */
public class BikeBorrowFailedEvent extends BaseErrorEvent {

	public EState state;

	public BikeBorrowFailedEvent(EState state, BaseError error) {
		super(error);
		this.state = state;
	}

	public enum EState {UNKNOWN, WRONG_CODE, FORBIDDEN, CONFLICT}

}
