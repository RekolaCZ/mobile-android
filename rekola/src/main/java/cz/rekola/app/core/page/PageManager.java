package cz.rekola.app.core.page;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Stack;

import cz.rekola.app.R;
import cz.rekola.app.activity.MainActivity;
import cz.rekola.app.fragment.base.BaseMainFragment;
import cz.rekola.app.fragment.natural.AboutFragment;
import cz.rekola.app.fragment.natural.AddIssueFragment;
import cz.rekola.app.fragment.natural.BikeDetailFragment;
import cz.rekola.app.fragment.natural.BorrowFragment;
import cz.rekola.app.fragment.natural.BorrowFragmentKeyboard;
import cz.rekola.app.fragment.natural.MapFragment;
import cz.rekola.app.fragment.natural.ProfileFragment;
import cz.rekola.app.fragment.natural.ReturnFragment;
import cz.rekola.app.fragment.natural.ReturnMapFragment;
import cz.rekola.app.fragment.natural.SpinnerListFragment;
import cz.rekola.app.fragment.web.BikeDetailWebFragment;
import cz.rekola.app.fragment.web.ReturnWebFragment;
import cz.rekola.app.utils.KeyboardUtils;

/**
 * Works like own fragment manager
 */

public class PageManager {

    private static String TAG = "PageManager";
    private EPageState state = EPageState.MAP;
    private static final int mAnimationIn = android.R.animator.fade_in;
    private static final int mAnimationOut = android.R.animator.fade_out;

    /**
     * Fragment cache.
     */
    private HashMap<EPageState, Fragment> cache = new HashMap<>();

    private Stack<EPageState> prevStates = new Stack<>();

    public enum EPageState {
        //      Visible action bar
        //      Visible Tab menu,
        //      Use cache,
        //      Up state,
        //      Title ID,
        //      BaseMainFragment
        BORROW(false, true, true, false, null, BorrowFragment.class),
        BORROW_KEYBOARD(false, true, true, true, null, BorrowFragmentKeyboard.class),
        RETURN(false, true, true, false, null, ReturnFragment.class),
        MAP(false, true, true, false, null, MapFragment.class),
        PROFILE(false, true, false, false, null, ProfileFragment.class),
        RETURN_MAP(true, false, true, true, R.string.returnmap_title, ReturnMapFragment.class),
        ABOUT(true, false, true, true, R.string.about_title, AboutFragment.class),
        WEB_RETURN(false, true, false, false, null, ReturnWebFragment.class),
        BIKE_DETAIL(false, false, true, true, null, BikeDetailFragment.class),
        WEB_BIKE_DETAIL(true, false, false, true, null, BikeDetailWebFragment.class),
        ADD_ISSUE(true, false, true, true, R.string.add_issue_title, AddIssueFragment.class),
        SPINNER_LIST(false, false, false, true, null, SpinnerListFragment.class);

        EPageState(boolean actionBarVisible, boolean tabMenuVisible, boolean useCache,
                   boolean upState, Integer titleId, Class fragment) {
            this.actionBarVisible = actionBarVisible;
            this.tabMenuVisible = tabMenuVisible;
            this.useCache = useCache;
            this.upState = upState;
            this.titleId = titleId;
            this.fragment = fragment;
        }

        final boolean actionBarVisible;
        final boolean tabMenuVisible;
        final boolean useCache;
        final boolean upState;
        final Integer titleId;
        final Class<BaseMainFragment> fragment;
    }

    /**
     * Returns newly created or cached fragment for further configuration.
     *
     * @param newState
     * @param activity
     * @param fragmentManager
     * @param actionBar       @return Newly created fragment
     */
    private Fragment setState(EPageState newState, MainActivity activity, FragmentManager
            fragmentManager, ActionBar actionBar, boolean backPressed) {
        if (this.state == newState)
            return null;

        setActionBar(activity, actionBar, newState, backPressed);
        setStatusBar(activity, newState);

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
                    .setCustomAnimations(mAnimationIn, mAnimationOut)
                    .show(fragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(mAnimationIn, mAnimationOut)
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
        if (oldFrag != null) {
            if (this.state.useCache) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(mAnimationIn, mAnimationOut)
                        .hide(oldFrag)
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(mAnimationIn, mAnimationOut)
                        .remove(oldFrag)
                        .commit();
                cache.remove(this.state);
            }
        }

        if (newState == EPageState.WEB_BIKE_DETAIL) {
            ColorDrawable transparentColor = getColor(activity, R.color.transparent);
            actionBar.setBackgroundDrawable(transparentColor);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow_pink);
        } else {
            ColorDrawable primaryColor = getColor(activity, R.color.colorPrimary);
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
        setIconsHighlights(newState, activity.findViewById(R.id.tab_menu));
        KeyboardUtils.hideKeyboard(activity);

        return fragment;
    }

    public Fragment setNextState(EPageState newState, MainActivity activity, FragmentManager fragmentManager,
                                 ActionBar actionBar) {
        return setState(newState, activity, fragmentManager, actionBar, false);
    }

    public void setPrevState(MainActivity activity, FragmentManager fragmentManager,
                             ActionBar actionBar) {
        setState(prevStates.pop(), activity, fragmentManager, actionBar, true);
    }

    private void setActionBar(MainActivity activity, ActionBar actionBar, EPageState newState,
                              boolean backPressed) {

        activity.setTabMenuVisibility(newState.tabMenuVisible);

        if (!newState.actionBarVisible) {
            actionBar.hide();
        } else {
            actionBar.show();
            actionBar.setElevation(0); //remove shadow in Lollipop
        }

        actionBar.setHomeButtonEnabled(newState.upState);
        actionBar.setDisplayHomeAsUpEnabled(newState.upState);


        if (newState.upState && !backPressed) {
            prevStates.push(state);
        } else if (!newState.upState) {
            prevStates.clear();
        }

        if (newState.titleId == null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setTitle("");
        } else {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(newState.titleId);
        }

    }

    private void setStatusBar(Activity activity, EPageState newState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            int color;
            if (newState == EPageState.SPINNER_LIST) {
                color = R.color.dark_pink4;
            } else if (newState == EPageState.BIKE_DETAIL) {
                color = R.color.transparent;
            } else {
                color = R.color.dark_green;
            }

            window.setStatusBarColor(activity.getResources().getColor(color));
        }
    }

    //if action is active, than has another "active" icon
    private void setIconsHighlights(EPageState activeState, View tabMenu) {

        ImageView imgLockIcon = (ImageView) tabMenu.findViewById(R.id.img_action_lock);
        ImageView imgMapIcon = (ImageView) tabMenu.findViewById(R.id.img_action_map);
        ImageView imgProfileIcon = (ImageView) tabMenu.findViewById(R.id.img_action_profile);

        if (activeState == EPageState.BORROW
                || activeState == EPageState.BORROW_KEYBOARD
                || activeState == EPageState.RETURN)
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

    public boolean hasPrevState() {
        return !prevStates.empty();
    }

    private ColorDrawable getColor(Context context, int colorID) {
        return new ColorDrawable(context.getResources().getColor(colorID));
    }
}
