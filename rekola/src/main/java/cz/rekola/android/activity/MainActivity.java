package cz.rekola.android.activity;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.bus.AuthorizationRequiredEvent;
import cz.rekola.android.core.bus.DataLoadingFinished;
import cz.rekola.android.core.bus.IncompatibleApiEvent;
import cz.rekola.android.core.bus.ProgressDataLoading;
import cz.rekola.android.core.bus.DataLoadingStarted;
import cz.rekola.android.core.data.MyBikeWrapper;
import cz.rekola.android.core.page.PageController;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.page.PageManager;
import cz.rekola.android.fragment.web.BikeDetailWebFragment;
import cz.rekola.android.fragment.web.ReturnWebFragment;
import cz.rekola.android.view.MessageBarView;

public class MainActivity extends Activity implements PageController {

	@InjectView(R.id.fragment_container)
	FrameLayout vFragmentContainer;

	@InjectView(R.id.progress)
	ProgressBar progressBar;

	@InjectView(R.id.error_bar)
	MessageBarView errorBar;

	private PageManager pageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
		ButterKnife.inject(this);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // TODO: May produce NPE
		actionBar.setHomeButtonEnabled(false);

		pageManager = new PageManager();

		MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike();
		if (myBike != null)
			pageManager.setState(myBike.isBorrowed() ? PageManager.EPageState.RETURN : PageManager.EPageState.BORROW, getFragmentManager(), getActionBar(), getResources());
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
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		pageManager.setupOptionsMenu(menu, getApp().getDataManager().getBorrowedBike());
		return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_borrow:
				pageManager.setState(PageManager.EPageState.BORROW, getFragmentManager(), getActionBar(), getResources());
				break;
			case R.id.action_return:
				pageManager.setState(PageManager.EPageState.RETURN, getFragmentManager(), getActionBar(), getResources());
				break;
			case R.id.action_map:
				pageManager.setState(PageManager.EPageState.MAP, getFragmentManager(), getActionBar(), getResources());
				break;
			case R.id.action_profile:
				pageManager.setState(PageManager.EPageState.PROFILE, getFragmentManager(), getActionBar(), getResources());
				break;
			case R.id.action_logout:
				// TODO: Add yes/no dialog
				getApp().getPreferencesManager().setPassword(null);
				finish();
				break;
			case android.R.id.home:
				pageManager.setUpState(getFragmentManager(), getActionBar(), getResources());
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		invalidateOptionsMenu();
		return true;
	}

	@Override
	public void onBackPressed() {
		if (!pageManager.setBackState(getFragmentManager(), getActionBar(), getApp().getDataManager().getBorrowedBike(), getResources())) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		invalidateOptionsMenu();
	}

	/*@Override
	public boolean onNavigateUp() {
		return false;
	}*/

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
		finish();
	}

	@Subscribe
	public void onIncompatibleApi(IncompatibleApiEvent event) {
		finish();
	}

	@Override
	public void requestMap() {
		pageManager.setState(PageManager.EPageState.MAP, getFragmentManager(), getActionBar(), getResources());
		invalidateOptionsMenu();
	}

	@Override
	public void requestReturnBike() {
		pageManager.setState(PageManager.EPageState.RETURN, getFragmentManager(), getActionBar(), getResources());
		invalidateOptionsMenu();
	}

	@Override
	public void requestReturnMap() {
		pageManager.setState(PageManager.EPageState.RETURN_MAP, getFragmentManager(), getActionBar(), getResources());
		invalidateOptionsMenu();
	}

	@Override
	public void requestWebBikeDetail(int id, boolean issues) {
		Fragment fragment = pageManager.setState(PageManager.EPageState.WEB_BIKE_DETAIL, getFragmentManager(), getActionBar(), getResources());
		if (fragment != null && fragment instanceof BikeDetailWebFragment)
			((BikeDetailWebFragment)fragment).init(id, issues);
		invalidateOptionsMenu();
	}

	@Override
	public void requestWebBikeReturned(String successUrl) {
		Fragment fragment = pageManager.setState(PageManager.EPageState.WEB_RETURN, getFragmentManager(), getActionBar(), getResources());
		if (fragment != null && fragment instanceof ReturnWebFragment)
			((ReturnWebFragment)fragment).init(successUrl);
		invalidateOptionsMenu();
	}

	private RekolaApp getApp() {
		return (RekolaApp) getApplication();
	}
}
