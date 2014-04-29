package cz.rekola.android.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.Locale;

import cz.rekola.android.R;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.fragment.BorrowFragment;
import cz.rekola.android.fragment.MapFragment;
import cz.rekola.android.fragment.PlaceholderFragment;
import cz.rekola.android.fragment.ProfileFragment;
import cz.rekola.android.fragment.ReturnFragment;
import cz.rekola.android.fragment.ReturnMapFragment;

/**
 * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final int TAB_NUM = 5;

    private Context context; // TODO: Is this ok to keep?

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch(position) {
			case 0:
				return new BorrowFragment();
			case 1:
				return new ReturnFragment();
            case 2:
                return new MapFragment();
			case 3:
				return new ReturnMapFragment();
			case 4:
				return new ProfileFragment();
            default:
                return PlaceholderFragment.newInstance(position + 1);
        }
    }

	/*@Override
	public int getItemPosition(Object object){
		return PagerAdapter.POSITION_NONE;
	}*/

    @Override
    public int getCount() {
        // Show 3 total pages.
        return TAB_NUM;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return "Borrow";
            case 1:
                return "Return";
            case 2:
                return "Map";
			case 3:
				return "Return Map";
			case 4:
				return "Profile";
			default:
				return "section " + (position + 1);
        }
    }

	public void startBikeDetail(Bike bike) {
		notifyDataSetChanged();
	}
}
