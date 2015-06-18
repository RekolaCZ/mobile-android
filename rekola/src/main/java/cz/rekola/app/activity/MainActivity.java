package cz.rekola.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.activity.base.BaseActivity;
import cz.rekola.app.core.RekolaApp;
import cz.rekola.app.core.bus.AuthorizationRequiredEvent;
import cz.rekola.app.core.bus.DataLoadingFinished;
import cz.rekola.app.core.bus.DataLoadingStarted;
import cz.rekola.app.core.bus.IncompatibleApiEvent;
import cz.rekola.app.core.bus.ProgressDataLoading;
import cz.rekola.app.core.data.MyBikeWrapper;
import cz.rekola.app.core.page.PageController;
import cz.rekola.app.core.page.PageManager;
import cz.rekola.app.fragment.web.BikeDetailWebFragment;
import cz.rekola.app.fragment.web.ReturnWebFragment;
import cz.rekola.app.view.MessageBarView;

public class MainActivity extends BaseActivity implements PageController {

    @InjectView(R.id.progress)
    ProgressBar progressBar;

    @InjectView(R.id.error_bar)
    MessageBarView errorBar;

    private PageManager pageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // App was restarted and lost all data manager context. (Save instance state is not used yet, if ever..)
        if (!getApp().getDataManager().isOperational()) {
            startLoginActivity(null); // Relogin to update the context
            return;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setOwnActionBar();
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);

        pageManager = new PageManager();

        MyBikeWrapper myBike = getMyBike();
        if (myBike != null) {
            pageManager.setState(myBike.isBorrowed() ? PageManager.EPageState.RETURN : PageManager.EPageState.BORROW, getFragmentManager(), getSupportActionBar(), getResources());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getApp().getBus().register(this);
        getApp().getBus().register(errorBar);
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        getApp().getBus().unregister(this);
        getApp().getBus().unregister(errorBar);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Create options menu might be called even if the activity is terminating.
        if (!getApp().getDataManager().isOperational())
            return super.onCreateOptionsMenu(menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void setOwnActionBar() {
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_action_bar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);
    }

    public void onCustomOptionsItemSelected(View v) {
        switch (v.getId()) {
            case R.id.action_lock:
                MyBikeWrapper myBike = getMyBike();
                pageManager.setState(myBike != null && myBike.isBorrowed() ? PageManager.EPageState.RETURN : PageManager.EPageState.BORROW,
                        getFragmentManager(), getSupportActionBar(), getResources());

                break;
            case R.id.action_map:
                pageManager.setState(PageManager.EPageState.MAP, getFragmentManager(), getSupportActionBar(), getResources());
                break;
            case R.id.action_profile:
                pageManager.setState(PageManager.EPageState.PROFILE, getFragmentManager(), getSupportActionBar(), getResources());
                break;
            case android.R.id.home:
                pageManager.setUpState(getFragmentManager(), getSupportActionBar(), getResources());
                break;
            default:
                Log.e(TAG, "unknown options menu item " + v.getId() + " " + v.toString());
        }
    }

    @Override
    public void onBackPressed() {
        if (!pageManager.setBackState(getFragmentManager(), getSupportActionBar(), getResources())) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // This might be a cause for the stack mess..
            startActivity(intent);
        }
        invalidateOptionsMenu();
    }

    @Subscribe
    public void dataLoadingStarted(DataLoadingStarted event) {
        setProgressBarIndeterminateVisibility(true);
    }

    @Subscribe
    public void dataLoadingFinished(DataLoadingFinished event) {
        setProgressBarIndeterminateVisibility(false);
    }

    @Subscribe
    public void dataLoadingProgress(ProgressDataLoading event) {
        if (event.progress == 100) {
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (event.progress == 0) {
            progressBar.setVisibility(View.VISIBLE);
            return;
        }
        progressBar.setProgress(event.progress);
    }

    @Subscribe
    public void onAuthorizationRequired(AuthorizationRequiredEvent event) {
        startLoginActivity(getResources().getString(R.string.error_session_expired));
    }

    @Subscribe
    public void onIncompatibleApi(IncompatibleApiEvent event) {
        startLoginActivity(getResources().getString(R.string.error_old_app_version));
    }

    @Override
    public void requestMap() {
        pageManager.setState(PageManager.EPageState.MAP, getFragmentManager(), getSupportActionBar(), getResources());
        invalidateOptionsMenu();
    }

    @Override
    public void requestReturnBike() {
        pageManager.setState(PageManager.EPageState.RETURN, getFragmentManager(), getSupportActionBar(), getResources());
        invalidateOptionsMenu();
    }

    @Override
    public void requestReturnMap() {
        pageManager.setState(PageManager.EPageState.RETURN_MAP, getFragmentManager(), getSupportActionBar(), getResources());
        invalidateOptionsMenu();
    }

    @Override
    public void requestAbout() {
        pageManager.setState(PageManager.EPageState.ABOUT, getFragmentManager(), getSupportActionBar(), getResources());
        invalidateOptionsMenu();
    }

    @Override
    public void requestWebBikeDetail(int id, boolean issues) {
        Fragment fragment = pageManager.setState(PageManager.EPageState.WEB_BIKE_DETAIL, getFragmentManager(), getSupportActionBar(), getResources());
        if (fragment != null && fragment instanceof BikeDetailWebFragment)
            ((BikeDetailWebFragment) fragment).init(id, issues);
        invalidateOptionsMenu();
    }

    @Override
    public void requestWebBikeReturned(String successUrl) {
        Fragment fragment = pageManager.setState(PageManager.EPageState.WEB_RETURN, getFragmentManager(), getSupportActionBar(), getResources());
        if (fragment != null && fragment instanceof ReturnWebFragment)
            ((ReturnWebFragment) fragment).init(successUrl);
        invalidateOptionsMenu();
    }

    private MyBikeWrapper getMyBike() {
        return getApp().getDataManager().getBorrowedBike(false);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    public void startLoginActivity(String message) {
        getApp().resetDataManager();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        if (message != null) {
            intent.putExtra(LoginActivity.EXTRA_MESSAGE, message);
        }
        startActivity(intent);
        finish();
    }

    private RekolaApp getApp() {
        return (RekolaApp) getApplication();
    }
}
