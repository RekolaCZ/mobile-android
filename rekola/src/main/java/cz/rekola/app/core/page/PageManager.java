package cz.rekola.app.core.page;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Stack;

import cz.rekola.app.R;
import cz.rekola.app.fragment.base.BaseMainFragment;
import cz.rekola.app.fragment.natural.AboutFragment;
import cz.rekola.app.fragment.natural.AddIssueFragment;
import cz.rekola.app.fragment.natural.BikeDetailFragment;
import cz.rekola.app.fragment.natural.BorrowFragment;
import cz.rekola.app.fragment.natural.MapFragment;
import cz.rekola.app.fragment.natural.ProfileFragment;
import cz.rekola.app.fragment.natural.ReturnFragment;
import cz.rekola.app.fragment.natural.ReturnMapFragment;
import cz.rekola.app.fragment.natural.SpinnerListFragment;
import cz.rekola.app.fragment.web.ReturnWebFragment;

/**
 * Works like own fragment manager
 */

public class PageManager {

    private static String TAG = "PageManager";
    private EPageState state = EPageState.MAP;

    /**
     * Last used root state for back (up) calls.
     * This is the only contextual info here.
     */
    // private EPageState rootState = EPageState.MAP;

    /**
     * Fragment cache.
     */
    private HashMap<EPageState, Fragment> cache = new HashMap<>();

    private Stack<EPageState> prevStates = new Stack<>();

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
        BIKE_DETAIL(true, true, true, null, BikeDetailFragment.class),
        ADD_ISSUE(false, true, true, R.string.add_issue_title, AddIssueFragment.class),
        SPINNER_LIST(false, false, true, null, SpinnerListFragment.class);

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
    private Fragment setState(EPageState newState, Context context, FragmentManager
            fragmentManager, ActionBar actionBar, boolean backPressed) {
        if (this.state == newState)
            return null;

        actionBar.setHomeButtonEnabled(newState.upState);
        actionBar.setDisplayHomeAsUpEnabled(newState.upState);


        if (newState.upState && !backPressed) {
            prevStates.push(state);
        } else if (!newState.upState) {
            prevStates.clear();
        }

        actionBar.setDisplayShowCustomEnabled(newState.customActionBarViewEnabled);
        setCustomActionBar(context, actionBar, newState);

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

        if (newState == EPageState.BIKE_DETAIL) {
            ColorDrawable transparentColor = getColor(context, R.color.transparent);
            actionBar.setBackgroundDrawable(transparentColor);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow_pink);
        } else {
            ColorDrawable primaryColor = getColor(context, R.color.colorPrimary);
            actionBar.setBackgroundDrawable(primaryColor);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow_white);
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

    public Fragment setNextState(EPageState newState, Context context, FragmentManager fragmentManager,
                                 ActionBar actionBar) {
        return setState(newState, context, fragmentManager, actionBar, false);
    }

    public void setPrevState(Context context, FragmentManager fragmentManager,
                             ActionBar actionBar) {
        setState(prevStates.pop(), context, fragmentManager, actionBar, true);
    }

    //if action is active, than has another "active" icon
    private void setIconsHighlights(EPageState activeState, ActionBar actionBar) {
        View customView = actionBar.getCustomView();

        //if custom view is not default (can be bike detail))
        if (customView.findViewById(R.id.custom_action_bar_default) == null)
            return;

        ImageView imgLockIcon = (ImageView) customView.findViewById(R.id.img_action_lock);
        ImageView imgMapIcon = (ImageView) customView.findViewById(R.id.img_action_map);
        ImageView imgProfileIcon = (ImageView) customView.findViewById(R.id.img_action_profile);

        if (activeState == EPageState.BORROW || activeState == EPageState.RETURN)
            imgLockIcon.setImageResource(R.drawable.actionbar_ic_lock_active);
        else
            imgLockIcon.setImageResource(R.drawable.actionbar_ic_lock);

        if (activeState == EPageState.MAP)
            imgMapIcon.setImageResource(R.drawable.actionbar_ic_map_active);
        else
            imgMapIcon.setImageResource(R.drawable.actionbar_ic_map);

        if (activeState == EPageState.PROFILE)
            imgProfileIcon.setImageResource(R.drawable.actionbar_ic_profile_active);
        else
            imgProfileIcon.setImageResource(R.drawable.actionbar_ic_profile);

    }

    //set up correct custom action view, because there are two different
    private void setCustomActionBar(Context context, ActionBar actionBar, EPageState pageState) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newCustomActionBarView = null;
        View oldCustomActionBarView = actionBar.getCustomView();

        if (oldCustomActionBarView == null && pageState == EPageState.BIKE_DETAIL)
            newCustomActionBarView = inflater.inflate(R.layout.custom_action_bar_bike_detail, null);
        else if (actionBar.getCustomView() == null)
            newCustomActionBarView = inflater.inflate(R.layout.custom_action_bar, null);
        else if (pageState == EPageState.BIKE_DETAIL && oldCustomActionBarView.findViewById(R.id.custom_action_bike_detail) == null)
            newCustomActionBarView = inflater.inflate(R.layout.custom_action_bar_bike_detail, null);
        else if (oldCustomActionBarView.findViewById(R.id.custom_action_bar_default) == null)
            newCustomActionBarView = inflater.inflate(R.layout.custom_action_bar, null);

        if (newCustomActionBarView != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(newCustomActionBarView);
        }
    }

    public boolean hasPrevState() {
        return !prevStates.empty();
    }

    private ColorDrawable getColor(Context context, int colorID) {
        return new ColorDrawable(context.getResources().getColor(colorID));
    }

}
