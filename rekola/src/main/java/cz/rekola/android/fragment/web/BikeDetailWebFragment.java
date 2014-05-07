package cz.rekola.android.fragment.web;

import cz.rekola.android.core.Constants;
import cz.rekola.android.fragment.base.BaseWebFragment;

public class BikeDetailWebFragment extends BaseWebFragment {

	private int id;

	public void init(int id) {
		this.id = id;
	}

	@Override
	public String getStartUrl() {
		return String.format(Constants.WEBAPI_BIKE_DETAIL_URL, id);
	}

}
