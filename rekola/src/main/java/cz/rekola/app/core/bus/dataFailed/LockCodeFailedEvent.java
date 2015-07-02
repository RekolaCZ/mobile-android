package cz.rekola.app.core.bus.dataFailed;

import cz.rekola.app.api.model.error.BaseError;
import cz.rekola.app.core.bus.BaseErrorEvent;

/**
 * Failed to borrow a bike.
 */
public class LockCodeFailedEvent extends BaseErrorEvent {

    public EState state;

    public LockCodeFailedEvent(EState state, BaseError error) {
        super(error);
        this.state = state;
    }

    public enum EState {UNKNOWN, WRONG_CODE, FORBIDDEN, CONFLICT}

}
