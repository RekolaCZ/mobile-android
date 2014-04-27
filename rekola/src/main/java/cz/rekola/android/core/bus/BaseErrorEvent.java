package cz.rekola.android.core.bus;

import cz.rekola.android.api.model.error.BaseError;

/**
 * Created by palo on 27/04/14.
 */
public abstract class BaseErrorEvent {

	public final BaseError error;

	public BaseErrorEvent(BaseError error) {
		this.error = error;
	}

}
