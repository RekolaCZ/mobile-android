package cz.rekola.android.core.page;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import cz.rekola.android.R;
import cz.rekola.android.fragment.BaseMainFragment;
import cz.rekola.android.fragment.BorrowFragment;
import cz.rekola.android.fragment.MapFragment;
import cz.rekola.android.fragment.PlaceholderFragment;
import cz.rekola.android.fragment.ProfileFragment;
import cz.rekola.android.fragment.ReturnFragment;

public class PageManager {

	private final int containerId;

	private EPageState state = EPageState.MAP;
	private Boolean isBorrowedBike;

	public enum EPageState {
		//						BORROW, RETURN, MAP, OVERFLOW, PROFILE, ABOUT, BaseMainFragment
		BORROW	(new boolean[]	{false, false, true, true, true, true}, R.id.action_borrow, BorrowFragment.class ),
		RETURN	(new boolean[]	{false, false, true, true, true, true}, R.id.action_return, ReturnFragment.class),
		MAP		(new boolean[]	{true, true, false, true, true, true}, R.id.action_map, MapFragment.class),
		PROFILE	(new boolean[]	{true, true, true, true, false, true}, R.id.action_profile, ProfileFragment.class),
		ABOUT	(new boolean[]	{true, true, true, true, true, false}, R.id.action_about, PlaceholderFragment.class );

		EPageState(boolean[] actionAllowed, int resourceId, Class fragment) {
			this.actionAllowed = actionAllowed;
			this.resourceId = resourceId;
			this.fragment = fragment;
		}

		final boolean[] actionAllowed;
		final int resourceId;
		final Class<BaseMainFragment> fragment;
	}

	public PageManager(int fragmentContainerId) {
		this.containerId = fragmentContainerId;
	}

	public void setIsBorrowedBike(Boolean isBorrowedBike) {
		this.isBorrowedBike = isBorrowedBike;
	}

	public void setState(EPageState newState, FragmentManager fragmentManager) {
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

		fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
		this.state = newState;
	}

	public void setupOptionsMenu(Menu menu) {
		OptionsMenuConfig menuConfig = new OptionsMenuConfig();
		menuConfig.setupMenu(menu);

		if (isBorrowedBike != null) {
			if (isBorrowedBike) {
				menu.findItem(EPageState.BORROW.resourceId).setVisible(false);
			} else {
				menu.findItem(EPageState.RETURN.resourceId).setVisible(false);
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
