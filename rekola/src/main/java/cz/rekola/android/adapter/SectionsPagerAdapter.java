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

/**
 * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final int TAB_NUM = 3;

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
                return new MapFragment();
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
                return context.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return context.getString(R.string.title_section2).toUpperCase(l);
            case 2:
                return context.getString(R.string.title_section3).toUpperCase(l);
        }
        return null;
    }

	public void startBikeDetail(Bike bike) {
		notifyDataSetChanged();
	}
}
