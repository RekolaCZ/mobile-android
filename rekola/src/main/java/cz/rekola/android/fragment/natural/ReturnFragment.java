package cz.rekola.android.fragment.natural;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.Constants;
import cz.rekola.android.core.data.MyBikeWrapper;
import cz.rekola.android.fragment.base.BaseMainFragment;
import cz.rekola.android.view.ApiWebView;
import cz.rekola.android.webapi.WebApiHandler;

public class ReturnFragment extends BaseMainFragment implements WebApiHandler {

	@InjectView(R.id.return_bike_web)
	ApiWebView vWeb;

	@InjectView(R.id.bike_name)
	TextView vBikeName;
	@InjectView(R.id.lock_code)
	TextView vLockCode;
	@InjectView(R.id.return_bike)
	Button vReturn;
	@InjectView(R.id.return_bike_detail)
	Button vDetail;
	@InjectView(R.id.return_bike_issue)
	Button vIssue;

	private int bikeId = -1; // Invalid bike id

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_return, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		populateData();

		vReturn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getPageController().requestReturnMap();
			}
		});

		vDetail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getPageController().requestWebBikeDetail(bikeId, false);
			}
		});

		vIssue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getPageController().requestWebBikeDetail(bikeId, true);
			}
		});

		// TODO: Handle api key expiration!
		Map extraHeaderMap = new HashMap<String, String>();
		extraHeaderMap.put(Constants.HEADER_KEY_TOKEN, getApp().getDataManager().getToken().apiKey);

		vWeb.init(getApp().getBus(), this, String.format(Constants.WEBAPI_BIKE_RETURNED_URL, bikeId), extraHeaderMap);
	}

	@Override
	public boolean onWebApiEvent(String paramUrl) {
		return false;
	}

	private void populateData() {
		MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike();
		if (myBike == null || !myBike.isBorrowed()) {
			setData(getResources().getString(R.string.error_no_bike_borrowed), "");
			return;
		}

		if (myBike.bike != null) {
			bikeId = myBike.bike.id;
			setData(String.format(getResources().getString(R.string.return_bike_name), myBike.bike.name), myBike.bike.lockCode);
			return;
		}

		if (myBike.lockCode != null) {
			bikeId = myBike.lockCode.bike.id;
			setData(String.format(getResources().getString(R.string.return_bike_name), myBike.lockCode.bike.name), myBike.lockCode.lockCode);
			return;
		}
	}

	private void setData(String name, String lockCode) {
		vBikeName.setText(name);
		vLockCode.setText(lockCode == null ? null : lockCode.replace("", " ").trim());
	}

}
