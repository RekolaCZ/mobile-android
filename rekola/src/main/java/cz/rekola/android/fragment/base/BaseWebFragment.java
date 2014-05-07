package cz.rekola.android.fragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.Constants;
import cz.rekola.android.fragment.base.BaseMainFragment;
import cz.rekola.android.view.ApiWebView;
import cz.rekola.android.webapi.WebApiHandler;

public abstract class BaseWebFragment extends BaseMainFragment implements WebApiHandler {

	@InjectView(R.id.profile_web)
	ApiWebView vWeb;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		// TODO: Handle api key expiration!
		Map extraHeaderMap = new HashMap<String, String>();
		extraHeaderMap.put(Constants.HEADER_KEY_TOKEN, getApp().getDataManager().getToken().apiKey);

		vWeb.init(getApp().getBus(), this, getStartUrl(), extraHeaderMap);
	}

	@Override
	public boolean onWebApiEvent(String paramUrl) {
		return false;
	}

	public abstract String getStartUrl();
}
