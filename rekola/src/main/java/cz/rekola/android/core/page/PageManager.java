package cz.rekola.android.core.page;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.view.Menu;

import cz.rekola.android.R;
import cz.rekola.android.core.data.DataManager;
import cz.rekola.android.core.data.MyBikeWrapper;
import cz.rekola.android.fragment.BaseMainFragment;
import cz.rekola.android.fragment.BorrowFragment;
import cz.rekola.android.fragment.MapFragment;
import cz.rekola.android.fragment.PlaceholderFragment;
import cz.rekola.android.fragment.ProfileFragment;
import cz.rekola.android.fragment.ReturnFragment;
import cz.rekola.android.fragment.ReturnMapFragment;

public class PageManager {

	private final int containerId;

	private EPageState state = EPageState.MAP;

	public enum EPageState {
		//						{BORROW, RETURN, MAP, OVERFLOW, PROFILE, ABOUT}, Up State, TitleId, actionResourceId, BaseMainFragment
		BORROW	(new boolean[]	{false, false, true, true, true, true}, null, null, R.id.action_borrow, BorrowFragment.class ),
		RETURN	(new boolean[]	{false, false, true, true, true, true}, null, null, R.id.action_return, ReturnFragment.class),
		MAP		(new boolean[]	{true, true, false, true, true, true}, null, null, R.id.action_map, MapFragment.class),
		PROFILE	(new boolean[]	{true, true, true, true, false, true}, null, null, R.id.action_profile, ProfileFragment.class),
		ABOUT	(new boolean[]	{true, true, true, true, true, false}, null, null, R.id.action_about, PlaceholderFragment.class ),

		// Other states without actionbar access.
		RET_MAP	(new boolean[]	{false, false, true, false, false, false}, RETURN, R.string.page_return_map_title, null, ReturnMapFragment.class );

		EPageState(boolean[] actionAllowed, EPageState upState, Integer titleId, Integer actionResourceId, Class fragment) {
			this.actionAllowed = actionAllowed;
			this.actionResourceId = actionResourceId;
			this.upState = upState;
			this.titleId = titleId;
			this.fragment = fragment;
		}

		final boolean[] actionAllowed;
		final Integer actionResourceId;
		final EPageState upState;
		final Integer titleId;
		final Class<BaseMainFragment> fragment;
	}

	public PageManager(int fragmentContainerId) {
		this.containerId = fragmentContainerId;
	}

	public void setState(EPageState newState, FragmentManager fragmentManager, ActionBar actionBar) {
		if (this.state == newState)
			return;

		BaseMainFragment fragment = null;
		try {
			fragment = newState.fragment.newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		if (fragment == null)
			return;

		actionBar.setHomeButtonEnabled(newState.upState != null);
		actionBar.setDisplayHomeAsUpEnabled(newState.upState != null);
		if (newState.titleId == null)
			actionBar.setTitle("");
		else
			actionBar.setTitle(newState.titleId);

		fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
		this.state = newState;
	}

	public void setUpState(FragmentManager fragmentManager, ActionBar actionBar) {
		if (state.upState == null)
			return;

		setState(state.upState, fragmentManager, actionBar);
	}

	public void setupOptionsMenu(Menu menu, MyBikeWrapper myBike) {
		OptionsMenuConfig menuConfig = new OptionsMenuConfig();
		menuConfig.setupMenu(menu);

		if (myBike != null) {
			if (myBike.isBorrowed()) {
				menu.findItem(EPageState.BORROW.actionResourceId).setVisible(false);
			} else {
				menu.findItem(EPageState.RETURN.actionResourceId).setVisible(false);
			}
		}
	}

	private class OptionsMenuConfig {
		private int index = 0;
		void setupMenu(Menu menu) {
			for(int i = 0; i < menu.size(); i++) {
				menu.getItem(i).setVisible(state.actionAllowed[index]);
				index++;
				if (menu.getItem(i).hasSubMenu())
					setupMenu(menu.getItem(i).getSubMenu());
			}
		}
	}
}
