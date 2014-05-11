package cz.rekola.android.fragment.web;

import cz.rekola.android.fragment.base.BaseWebFragment;
import cz.rekola.android.webapi.WebApiConstants;

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
		if (paramUrl != null && paramUrl.contains(WebApiConstants.PARAM_DISMISS)) {
			getPageController().requestMap();
			return true;
		}
		return false;
	}

}
