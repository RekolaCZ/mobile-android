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
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.page.PageManager;

public class MainActivity extends Activity {

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
		actionBar.setTitle("");
		actionBar.setHomeButtonEnabled(false); // TODO: How to add up button?

		Boolean isBorrowedBike = getApp().getDataManager().isBorrowedBike();
		pageManager = new PageManager(R.id.fragment_container);
		pageManager.setIsBorrowedBike(isBorrowedBike);
		if (isBorrowedBike != null)
			pageManager.setState(isBorrowedBike ? PageManager.EPageState.RETURN : PageManager.EPageState.BORROW, getFragmentManager());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		pageManager.setupOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_borrow:
				pageManager.setState(PageManager.EPageState.BORROW, getFragmentManager());
				break;
			case R.id.action_return:
				pageManager.setState(PageManager.EPageState.RETURN, getFragmentManager());
				break;
			case R.id.action_map:
				pageManager.setState(PageManager.EPageState.MAP, getFragmentManager());
				break;
			case R.id.action_profile:
				pageManager.setState(PageManager.EPageState.PROFILE, getFragmentManager());
				break;
			case R.id.action_about:
				pageManager.setState(PageManager.EPageState.ABOUT, getFragmentManager());
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

	public void startBikeDetail(Bike bike) {
	}

	private RekolaApp getApp() {
		return (RekolaApp) getApplication();
	}
}
