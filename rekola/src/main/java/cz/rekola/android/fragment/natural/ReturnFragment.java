package cz.rekola.android.fragment.natural;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.data.MyBikeWrapper;
import cz.rekola.android.fragment.base.BaseMainFragment;

public class ReturnFragment extends BaseMainFragment {

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
	}

	private void populateData() {
		MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike();
		if (myBike == null || !myBike.isBorrowed()) {
			setData("No bike borrowed.", "");
			return;
		}

		if (myBike.bike != null) {
			setData(String.format(getResources().getString(R.string.return_bike_name), myBike.bike.name), myBike.bike.lockCode);
			return;
		}

		if (myBike.lockCode != null) {
			setData(String.format(getResources().getString(R.string.return_bike_name), myBike.lockCode.bike.name), myBike.lockCode.lockCode);
			return;
		}
	}

	private void setData(String name, String lockCode) {
		vBikeName.setText(name);
		vLockCode.setText(lockCode);
	}
}
