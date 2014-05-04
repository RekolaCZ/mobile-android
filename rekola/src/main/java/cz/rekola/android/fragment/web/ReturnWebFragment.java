package cz.rekola.android.fragment.web;

import cz.rekola.android.core.Constants;
import cz.rekola.android.fragment.base.BaseWebFragment;

public class ReturnWebFragment extends BaseWebFragment {

	@Override
	public String getStartUrl() {
		return Constants.WEBAPI_BIKE_RETURNED_URL;
	}

	@Override
	public boolean onWebApiEvent(String paramUrl) {
		if (paramUrl.contains("moje")) {
			getPageController().requestMap();
			return true;
		}
		return false;
	}

}
