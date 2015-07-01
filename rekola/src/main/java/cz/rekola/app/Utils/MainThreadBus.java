package cz.rekola.app.utils;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Extend otto event bus to use if event is need to run in main thread
 * http://stackoverflow.com/questions/15431768/how-to-send-event-from-service-to-activity-with-otto-event-bus
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {1. 7. 2015}
 **/
public class MainThreadBus extends Bus {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainThreadBus.super.post(event);
                }
            });
        }
    }
}
