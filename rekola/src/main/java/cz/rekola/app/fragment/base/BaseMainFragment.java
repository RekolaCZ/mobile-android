package cz.rekola.app.fragment.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import cz.rekola.app.activity.MainActivity;
import cz.rekola.app.core.page.PageController;
import cz.rekola.app.core.RekolaApp;

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

	public MainActivity getAct() {
		return (MainActivity) getActivity();
	}

	public RekolaApp getApp() {
		return (RekolaApp) getActivity().getApplication();
	}

}
