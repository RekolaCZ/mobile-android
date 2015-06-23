package cz.rekola.app.core.page;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;

import cz.rekola.app.R;
import cz.rekola.app.fragment.base.BaseMainFragment;
import cz.rekola.app.fragment.natural.AboutFragment;
import cz.rekola.app.fragment.natural.BikeDetailFragment;
import cz.rekola.app.fragment.natural.BorrowFragment;
import cz.rekola.app.fragment.natural.MapFragment;
import cz.rekola.app.fragment.natural.ProfileFragment;
import cz.rekola.app.fragment.natural.ReturnFragment;
import cz.rekola.app.fragment.natural.ReturnMapFragment;
import cz.rekola.app.fragment.web.ReturnWebFragment;

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
        //      Custom action bar view enabled,
        //      Use cache,
        //      Up state,
        //      Title ID,
        //      BaseMainFragment
        BORROW(true, true, false, null, BorrowFragment.class),
        RETURN(true, true, false, null, ReturnFragment.class),
        MAP(true, true, false, null, MapFragment.class),
        PROFILE(true, false, false, null, ProfileFragment.class),
        RETURN_MAP(false, true, true, R.string.returnmap_title, ReturnMapFragment.class),
        ABOUT(false, true, true, R.string.about_title, AboutFragment.class),
        WEB_RETURN(true, false, false, null, ReturnWebFragment.class),
        WEB_BIKE_DETAIL(true, false, true, null, BikeDetailFragment.class);

        EPageState(boolean customActionBarViewEnabled, boolean useCache, boolean upState, Integer titleId, Class fragment) {
            this.customActionBarViewEnabled = customActionBarViewEnabled;
            this.useCache = useCache;
            this.upState = upState;
            this.titleId = titleId;
            this.fragment = fragment;
        }

        final boolean customActionBarViewEnabled;
        final boolean useCache;
        final boolean upState;
        final Integer titleId;
        final Class<BaseMainFragment> fragment;
    }

    /**
     * Returns newly created or cached fragment for further configuration.
     *
     * @param newState
     * @param fragmentManager
     * @param actionBar
     * @return Newly created fragment
     */
    public Fragment setState(EPageState newState, FragmentManager fragmentManager, ActionBar actionBar, Resources resources) {
        if (this.state == newState)
            return null;

        actionBar.setHomeButtonEnabled(newState.upState);
        actionBar.setDisplayHomeAsUpEnabled(newState.upState);

        if (newState.upState) { // It is a root state
            rootState = state;
        }

        actionBar.setDisplayShowCustomEnabled(newState.customActionBarViewEnabled);

        if (newState.customActionBarViewEnabled)
            actionBar.getCustomView().setVisibility(View.VISIBLE);
        else
            actionBar.getCustomView().setVisibility(View.GONE);

        if (newState.titleId == null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setTitle("");
        } else {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(newState.titleId);
        }

        Fragment fragment = cache.get(newState);
        if (fragment == null) {
            try {
                fragment = newState.fragment.newInstance();
                cache.put(newState, fragment);
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }

        if (fragment == null)
            return null;

        Fragment oldFrag = cache.get(this.state);
        if (fragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .show(fragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
        if (oldFrag != null) {
            if (this.state.useCache) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .hide(oldFrag)
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .remove(oldFrag)
                        .commit();
                cache.remove(this.state);
            }
        }

        // Release unused cached Borrow/Return fragment when the other is selected.
        if (newState == EPageState.BORROW) {
            cache.remove(EPageState.RETURN);
        }
        if (newState == EPageState.RETURN) {
            cache.remove(EPageState.BORROW);
        }

        this.state = newState;
        setIconsHighlights(newState, actionBar);

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

    public boolean setBackState(FragmentManager fragmentManager, ActionBar actionBar, Resources resources) {
        if (!state.upState)
            return false;

        setUpState(fragmentManager, actionBar, resources);
        return true;
    }

    //if action is active, than has another "active" icon
    private void setIconsHighlights(EPageState activeState, ActionBar actionBar) {
        View custoView = actionBar.getCustomView();
        ImageView lockIcon = (ImageView) custoView.findViewById(R.id.action_lock);
        ImageView mapIcon = (ImageView) custoView.findViewById(R.id.action_map);
        ImageView profileIcon = (ImageView) custoView.findViewById(R.id.action_profile);

        if (activeState == EPageState.BORROW || activeState == EPageState.RETURN)
            lockIcon.setImageResource(R.drawable.actionbar_ic_lock_active);
        else
            lockIcon.setImageResource(R.drawable.actionbar_ic_lock);

        if (activeState == EPageState.MAP)
            mapIcon.setImageResource(R.drawable.actionbar_ic_map_active);
        else
            mapIcon.setImageResource(R.drawable.actionbar_ic_map);

        if (activeState == EPageState.PROFILE)
            profileIcon.setImageResource(R.drawable.actionbar_ic_profile_active);
        else
            profileIcon.setImageResource(R.drawable.actionbar_ic_profile);

    }
}
