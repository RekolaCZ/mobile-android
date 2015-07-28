package cz.rekola.app.activity.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import cz.config.HockeyConfig;
import cz.rekola.app.R;
import cz.rekola.app.core.RekolaApp;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/** Base Activity, all new Activity should extend this Activity
 * Created on 7.5.2015 by tomas.krabac@ackee.cz
 */

public class BaseActivity extends AppCompatActivity {
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

    //use custom fonts https://github.com/chrisjenx/Calligraphy/
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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