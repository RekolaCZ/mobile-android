package cz.rekola.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.activity.base.BaseActivity;
import cz.rekola.app.core.bus.AuthorizationRequiredEvent;
import cz.rekola.app.core.bus.DataLoadingFinished;
import cz.rekola.app.core.bus.DataLoadingStarted;
import cz.rekola.app.core.bus.IncompatibleApiEvent;
import cz.rekola.app.core.bus.ProgressDataLoading;
import cz.rekola.app.core.data.MyBikeWrapper;
import cz.rekola.app.core.interfaces.SetIssueItemInterface;
import cz.rekola.app.core.page.PageController;
import cz.rekola.app.core.page.PageManager;
import cz.rekola.app.fragment.natural.AddIssueFragment;
import cz.rekola.app.fragment.natural.BikeDetailFragment;
import cz.rekola.app.fragment.natural.SpinnerListFragment;
import cz.rekola.app.fragment.web.ReturnWebFragment;
import cz.rekola.app.view.MessageBarView;

public class MainActivity extends BaseActivity implements PageController {

    /**
     * Activity is running when user is successful logged
     */

    @InjectView(R.id.layout_error_bar)
    MessageBarView mLayoutErrorBar;
    @InjectView(R.id.progress_circle_bar)
    CircleProgressBar mProgressCircleBar;

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
        pageManager = new PageManager();

        MyBikeWrapper myBike = getMyBike();
        if (myBike != null) {
            PageManager.EPageState pageState = myBike.isBorrowed() ? PageManager.EPageState.RETURN : PageManager.EPageState.BORROW;
            setState(pageState);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getApp().getBus().register(this);
        getApp().getBus().register(mLayoutErrorBar);
    }

    @Override
    public void onPause() {
        super.onPause();
        getApp().getBus().unregister(this);
        getApp().getBus().unregister(mLayoutErrorBar);
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

        //custom view in action bar is set up in {@link PageManager}

        return super.onCreateOptionsMenu(menu);
    }


    public void onCustomOptionsItemSelected(View v) {
        switch (v.getId()) {
            case R.id.img_action_lock:
                MyBikeWrapper myBike = getMyBike();
                PageManager.EPageState pageState = myBike != null && myBike.isBorrowed() ? PageManager.EPageState.RETURN : PageManager.EPageState.BORROW;
                setState(pageState);

                break;
            case R.id.img_action_map:
                setState(PageManager.EPageState.MAP);
                break;
            case R.id.img_action_profile:
                setState(PageManager.EPageState.PROFILE);
                break;
            case android.R.id.home:
                requestPrevState();
                break;
            default:
                Log.e(TAG, "unknown options menu item " + v.getId() + " " + v.toString());
        }
    }

    @Override
    public void onBackPressed() {
        if (pageManager.hasPrevState()) {
            pageManager.setPrevState(this, getFragmentManager(), getSupportActionBar());
        } else {
            super.onBackPressed();
        }
        invalidateOptionsMenu();
    }

    @Subscribe
    public void dataLoadingStarted(DataLoadingStarted event) {
        mProgressCircleBar.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void dataLoadingFinished(DataLoadingFinished event) {
        mProgressCircleBar.setVisibility(View.INVISIBLE);
    }

    @Subscribe
    public void dataLoadingProgress(ProgressDataLoading event) {
        if (event.progress == 100) {
            mProgressCircleBar.setShowProgressText(false);
            mProgressCircleBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (event.progress == 0) {
            mProgressCircleBar.setShowProgressText(true);
            mProgressCircleBar.setVisibility(View.VISIBLE);
            return;
        }
        mProgressCircleBar.setProgress(event.progress);
    }

    @Subscribe
    public void onAuthorizationRequired(AuthorizationRequiredEvent event) {
        startLoginActivity(getResources().getString(R.string.error_session_expired));
    }

    @Subscribe
    public void onIncompatibleApi(IncompatibleApiEvent event) {
        startLoginActivity(getResources().getString(R.string.error_old_app_version));
    }

    /**
     * methods to change fragments
     */

    @Override
    public void requestMap() {
        setState(PageManager.EPageState.MAP);
        invalidateOptionsMenu();
    }

    @Override
    public void requestReturnBike() {
        setState(PageManager.EPageState.RETURN);
        invalidateOptionsMenu();
    }

    @Override
    public void requestReturnMap() {
        setState(PageManager.EPageState.RETURN_MAP);
        invalidateOptionsMenu();
    }

    @Override
    public void requestAbout() {
        setState(PageManager.EPageState.ABOUT);
        invalidateOptionsMenu();
    }

    @Override
    public void requestBikeDetail(int bikeID) {
        Fragment fragment = setState(PageManager.EPageState.BIKE_DETAIL);
        if (fragment != null && fragment instanceof BikeDetailFragment)
            ((BikeDetailFragment) fragment).init(bikeID);
        invalidateOptionsMenu();
    }

    @Override
    public void requestSpinnerList(ArrayList<String> listItems, SetIssueItemInterface setIssueItemInterface) {
        Fragment fragment = setState(PageManager.EPageState.SPINNER_LIST);
        if (fragment != null && fragment instanceof SpinnerListFragment)
            ((SpinnerListFragment) fragment).init(listItems, setIssueItemInterface);
        invalidateOptionsMenu();
    }

    @Override
    public void requestWebBikeReturned(String successUrl) {
        Fragment fragment = setState(PageManager.EPageState.WEB_RETURN);
        if (fragment != null && fragment instanceof ReturnWebFragment)
            ((ReturnWebFragment) fragment).init(successUrl);
        invalidateOptionsMenu();
    }

    @Override
    public void requestAddIssue(int bikeID) {
        Fragment fragment = setState(PageManager.EPageState.ADD_ISSUE);
        if (fragment != null && fragment instanceof AddIssueFragment)
            ((AddIssueFragment) fragment).init(bikeID);
        invalidateOptionsMenu();
    }

    @Override
    public void requestPrevState() {
        pageManager.setPrevState(this, getFragmentManager(), getSupportActionBar());
    }

    private Fragment setState(PageManager.EPageState pageState) {
        return pageManager.setNextState(pageState, this, getFragmentManager(), getSupportActionBar());
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
}
