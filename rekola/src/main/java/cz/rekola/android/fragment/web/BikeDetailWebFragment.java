package cz.rekola.android.fragment.web;

import cz.rekola.android.core.Constants;
import cz.rekola.android.fragment.base.BaseWebFragment;

public class BikeDetailWebFragment extends BaseWebFragment {

	private int id;
	private boolean issues;

	public void init(int id, boolean issues) {
		this.id = id;
		this.issues = issues;
	}

	@Override
	public String getStartUrl() {
		return issues ? String.format(Constants.WEBAPI_BIKE_ISSUES_URL, id) : String.format(Constants.WEBAPI_BIKE_DETAIL_URL, id);
	}

}
