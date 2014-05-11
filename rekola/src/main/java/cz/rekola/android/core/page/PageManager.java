package cz.rekola.android.core.page;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.view.Menu;

import java.util.HashMap;

import cz.rekola.android.R;
import cz.rekola.android.core.data.MyBikeWrapper;
import cz.rekola.android.fragment.web.ProfileWebFragment;
import cz.rekola.android.fragment.base.BaseMainFragment;
import cz.rekola.android.fragment.web.BikeDetailWebFragment;
import cz.rekola.android.fragment.natural.BorrowFragment;
import cz.rekola.android.fragment.natural.MapFragment;
import cz.rekola.android.fragment.natural.ReturnFragment;
import cz.rekola.android.fragment.natural.ReturnMapFragment;
import cz.rekola.android.fragment.web.ReturnWebFragment;

public class PageManager {

	private EPageState state = EPageState.MAP;

	/**
	 * Last used root state for back (up) calls.
	 * This is the only contextual info here.
 	 */
	private EPageState rootState = EPageState.MAP;

	/**
	 * Fragment cache.
	 */
	private HashMap<EPageState, Fragment> cache = new HashMap<>();

	public enum EPageState {
		//								Uses menu item order.
		//				action bar link {BORROW, RETURN, MAP, PROFILE, OVERFLOW, LOGOUT}, Use cache, Up to Root State, Root state drawable, Back State, TitleId, menu item id, BaseMainFragment
		BORROW			(new boolean[]	{false, false, true, true, false, false}, false, false, R.drawable.actionbar_ic_borrow, null, null, R.id.action_borrow, BorrowFragment.class ),
		RETURN			(new boolean[]	{false, false, true, true, false, false}, true, false, R.drawable.actionbar_ic_return, null, null, R.id.action_return, ReturnFragment.class),
		MAP				(new boolean[]	{true, true, false, true, false, false}, true, false, R.drawable.actionbar_ic_map, BORROW/*or RETURN*/, null, R.id.action_map, MapFragment.class),
		PROFILE			(new boolean[]	{true, true, true, false, true, true}, false, false, null, BORROW/*or RETURN*/, null, R.id.action_profile, ProfileWebFragment.class),

		// Other states without actionbar access.
		RETURN_MAP		(new boolean[]	{false, false, true, false, false, false}, false, true, null, RETURN, R.string.returnmap_title, null, ReturnMapFragment.class ),
		WEB_RETURN		(new boolean[]	{true, true, true, true, false, false}, false, false, null, BORROW, null, null, ReturnWebFragment.class),
		WEB_BIKE_DETAIL	(new boolean[]	{true, true, true, true, false, false}, false, true, null, MAP/*or RETURN*/, R.string.webbikedetail_title, null, BikeDetailWebFragment.class);

		EPageState(boolean[] actionAllowed, boolean useCache, boolean upState, Integer rootStateDrawable, EPageState backState, Integer titleId, Integer actionResourceId, Class fragment) {
			this.actionAllowed = actionAllowed;
			this.useCache = useCache;
			this.upState = upState;
			this.rootStateDrawable = rootStateDrawable;
			this.backState = backState;
			this.titleId = titleId;
			this.actionResourceId = actionResourceId;
			this.fragment = fragment;
		}

		final boolean[] actionAllowed;
		final boolean useCache;
		final boolean upState;
		final Integer rootStateDrawable;
		final EPageState backState;
		final Integer titleId;
		final Integer actionResourceId;
		final Class<BaseMainFragment> fragment;
	}

	/**
	 * Returns newly created or cached fragment for further configuration.
	 * @param newState
	 * @param fragmentManager
	 * @param actionBar
	 * @return Newly created fragment
	 */
	public Fragment setState(EPageState newState, FragmentManager fragmentManager, ActionBar actionBar, Resources resources) {
		if (this.state == newState)
			return null;

		if (newState.rootStateDrawable != null) { // It is a root state
			rootState = newState;
		}

		/*if (newState == EPageState.MAP || newState == EPageState.BORROW || newState == EPageState.RETURN) {
			rootState = newState;
		}*/

		actionBar.setHomeButtonEnabled(newState.upState);
		actionBar.setDisplayHomeAsUpEnabled(newState.upState);
		if (newState.upState) {
			actionBar.setLogo(null);
			actionBar.setIcon(resources.getDrawable(rootState.rootStateDrawable));
		} else {
			actionBar.setLogo(R.drawable.navbar_ic_logo);
			actionBar.setIcon(null);
		}

		if (newState.titleId == null)
			actionBar.setTitle("");
		else
			actionBar.setTitle(newState.titleId);

		Fragment fragment = cache.get(newState);
		if (fragment == null) {
			try {
				fragment = newState.fragment.newInstance();
				if (newState.useCache) {
					cache.put(newState, fragment);
				}
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}

		if (fragment == null)
			return null;

		fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.fragment_container, fragment).commit();
		this.state = newState;

		return fragment;
	}

	public void setUpState(FragmentManager fragmentManager, ActionBar actionBar, Resources resources) {
		if (!state.upState)
			return;

		// Special handling of bike detail up state
		if (state == EPageState.WEB_BIKE_DETAIL && rootState == EPageState.RETURN) {
			setState(EPageState.RETURN, fragmentManager, actionBar, resources);
			return;
		}

		setState(rootState, fragmentManager, actionBar, resources);
	}

	public boolean setBackState(FragmentManager fragmentManager, ActionBar actionBar, MyBikeWrapper myBike, Resources resources) {
		if (state.backState == null)
			return false;

		// Special handling for return state instead of borrow state
		if (myBike != null && myBike.isBorrowed() && state.backState == EPageState.BORROW) {
			setState(EPageState.RETURN, fragmentManager, actionBar, resources);
			return true;
		}

		// Special handling of Bike detail when opened from RETURN state
		if (state == EPageState.WEB_BIKE_DETAIL && rootState == EPageState.RETURN) {
			setState(EPageState.RETURN, fragmentManager, actionBar, resources);
			return true;
		}

		setState(state.backState, fragmentManager, actionBar, resources);
		return true;
	}

	public void setupOptionsMenu(Menu menu, MyBikeWrapper myBike) {
		OptionsMenuConfig menuConfig = new OptionsMenuConfig();
		menuConfig.setupMenu(menu);

		// Special handling for bike detail
		if (state == EPageState.WEB_BIKE_DETAIL) {
			if (rootState == EPageState.RETURN) {
				menu.findItem(EPageState.RETURN.actionResourceId).setVisible(false);
			}
			if (rootState == EPageState.MAP) {
				menu.findItem(EPageState.MAP.actionResourceId).setVisible(false);
			}
		}

		// Special handling of borrow/return menu items
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
