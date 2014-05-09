package cz.rekola.android.fragment.web;

import cz.rekola.android.fragment.base.BaseWebFragment;

public class ReturnWebFragment extends BaseWebFragment {

	private String successUrl;

	public void init(String successUrl) {
		this.successUrl = successUrl;
	}

	@Override
	public String getStartUrl() {
		return successUrl;
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
