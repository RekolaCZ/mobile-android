package cz.rekola.android.fragment;

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
import cz.rekola.android.view.ApiWebView;

public class ProfileFragment extends BaseMainFragment {

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

		//vWeb.setData(Constants.WEBAPI_PROFILE_URL);
		vWeb.loadUrl(Constants.WEBAPI_PROFILE_URL, extraHeaderMap);
	}

}
