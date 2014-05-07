package cz.rekola.android.activity;

import android.app.Activity;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.data.MyBikeWrapper;
import cz.rekola.android.core.page.PageController;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.page.PageManager;

public class MainActivity extends Activity implements PageController {

	@InjectView(R.id.fragment_container)
	FrameLayout vFragmentContainer;

	private PageManager pageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		ButterKnife.inject(this);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // TODO: May produce NPE
		actionBar.setHomeButtonEnabled(false); // TODO: How to add up button?

		pageManager = new PageManager();

		MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike();
		if (myBike != null)
			pageManager.setState(myBike.isBorrowed() ? PageManager.EPageState.RETURN : PageManager.EPageState.BORROW, getFragmentManager(), getActionBar());
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
				pageManager.setState(PageManager.EPageState.BORROW, getFragmentManager(), getActionBar());
				break;
			case R.id.action_return:
				pageManager.setState(PageManager.EPageState.RETURN, getFragmentManager(), getActionBar());
				break;
			case R.id.action_map:
				pageManager.setState(PageManager.EPageState.MAP, getFragmentManager(), getActionBar());
				break;
			case R.id.action_profile:
				pageManager.setState(PageManager.EPageState.PROFILE, getFragmentManager(), getActionBar());
				break;
			case R.id.action_about:
				pageManager.setState(PageManager.EPageState.ABOUT, getFragmentManager(), getActionBar());
				break;
			case android.R.id.home:
				pageManager.setUpState(getFragmentManager(), getActionBar());
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		invalidateOptionsMenu();
		return true;
	}

	/*@Override
	public boolean onNavigateUp() {
		return false;
	}*/

	@Override
	public void requestMap() {
		pageManager.setState(PageManager.EPageState.MAP, getFragmentManager(), getActionBar());
		invalidateOptionsMenu();
	}

	@Override
	public void requestReturnBike() {
		pageManager.setState(PageManager.EPageState.RETURN, getFragmentManager(), getActionBar());
		invalidateOptionsMenu();
	}

	@Override
	public void requestReturnMap() {
		pageManager.setState(PageManager.EPageState.RETURN_MAP, getFragmentManager(), getActionBar());
		invalidateOptionsMenu();
	}

	@Override
	public void requestWebBikeDetail() {
		pageManager.setState(PageManager.EPageState.WEB_BIKE_DETAIL, getFragmentManager(), getActionBar());
		invalidateOptionsMenu();
	}

	@Override
	public void requestWebBikeReturned() {
		pageManager.setState(PageManager.EPageState.WEB_RETURN, getFragmentManager(), getActionBar());
		invalidateOptionsMenu();
	}

	private RekolaApp getApp() {
		return (RekolaApp) getApplication();
	}
}
