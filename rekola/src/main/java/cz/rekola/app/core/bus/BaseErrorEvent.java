package cz.rekola.app.core.bus;

import cz.rekola.app.api.model.error.BaseError;

/**
 * Created by palo on 27/04/14.
 */
public abstract class BaseErrorEvent {

    public final BaseError error;

    public BaseErrorEvent(BaseError error) {
        this.error = error;
    }

}
