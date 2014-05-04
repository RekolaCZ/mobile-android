package cz.rekola.android.fragment.web;

import cz.rekola.android.core.Constants;
import cz.rekola.android.fragment.base.BaseWebFragment;

public class BikeDetailWebFragment extends BaseWebFragment {

	@Override
	public String getStartUrl() {
		return Constants.WEBAPI_BIKE_DETAIL_URL;
	}

}
