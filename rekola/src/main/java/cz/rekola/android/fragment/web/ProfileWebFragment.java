package cz.rekola.android.fragment.web;

import cz.rekola.android.core.Constants;
import cz.rekola.android.fragment.base.BaseWebFragment;
import cz.rekola.android.webapi.WebApiConstants;

public class ProfileWebFragment extends BaseWebFragment {

	@Override
	public String getStartUrl() {
		return Constants.WEBAPI_PROFILE_URL;
	}

	@Override
	public boolean onWebApiEvent(String paramUrl) {
		if (paramUrl != null && paramUrl.endsWith(WebApiConstants.PARAM_LOGOUT)) {
			getApp().getPreferencesManager().setPassword(null);
			getActivity().finish();
			return true;
		}
		return false;
	}

}
