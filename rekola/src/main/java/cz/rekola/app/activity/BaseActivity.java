package cz.rekola.app.activity;

import android.app.Activity;
import android.os.Bundle;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import cz.config.HockeyConfig;

/**
 * Created on 7.5.2015 by tomas.krabac@ackee.cz
 */

public class BaseActivity extends Activity {
    public static String TAG = BaseActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
    }

    private void checkForCrashes() {
        CrashManager.register(this, HockeyConfig.HOCKEYAPP_ID, new CrashManagerListener() {
            @Override
            public boolean shouldAutoUploadCrashes() {
                return HockeyConfig.HOCKEYAPP_AUTOSEND;
            }
        });
    }
}