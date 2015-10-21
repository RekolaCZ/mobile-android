package cz.rekola.app.fragment.base;

import android.app.Fragment;

import cz.rekola.app.activity.MainActivity;
import cz.rekola.app.core.RekolaApp;
import cz.rekola.app.core.page.PageController;

/**
 * Base fragment, all other fragment should extend this fragment
 */

public abstract class BaseMainFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        getApp().getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getApp().getBus().unregister(this);
    }

    public PageController getPageController() {
        return (MainActivity) getActivity();
    }

    public MainActivity getAct() {
        return (MainActivity) getActivity();
    }

    public RekolaApp getApp() {
        return (RekolaApp) getActivity().getApplication();
    }

}
