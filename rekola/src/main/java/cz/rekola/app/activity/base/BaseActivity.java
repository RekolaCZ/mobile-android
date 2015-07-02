package cz.rekola.app.activity.base;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import cz.config.HockeyConfig;
import cz.rekola.app.core.RekolaApp;

/** Base Activity, all new Activity should extend this Activity
 * Created on 7.5.2015 by tomas.krabac@ackee.cz
 */

public class BaseActivity extends ActionBarActivity {
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

    protected RekolaApp getApp() {
        return (RekolaApp) getApplication();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}