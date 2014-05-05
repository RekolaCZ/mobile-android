package cz.rekola.android.fragment.web;

import cz.rekola.android.core.Constants;
import cz.rekola.android.fragment.base.BaseWebFragment;

public class ProfileWebFragment extends BaseWebFragment {

	@Override
	public String getStartUrl() {
		return Constants.WEBAPI_PROFILE_URL;
	}

	@Override
	public boolean onWebApiEvent(String paramUrl) {
		if (paramUrl.contains("log_out")) {
			getApp().getPreferencesManager().setPassword(null);
			getActivity().finish();
			return true;
		}
		return false;
	}

}
