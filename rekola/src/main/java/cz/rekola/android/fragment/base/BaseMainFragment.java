package cz.rekola.android.fragment.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import cz.rekola.android.activity.MainActivity;
import cz.rekola.android.core.page.PageController;
import cz.rekola.android.core.RekolaApp;

public abstract class BaseMainFragment extends Fragment {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getApp().getBus().register(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getApp().getBus().unregister(this);
	}

	public PageController getPageController() {
		return (MainActivity) getActivity();
	}

	public RekolaApp getApp() {
		return (RekolaApp) getActivity().getApplication();
	}

}
