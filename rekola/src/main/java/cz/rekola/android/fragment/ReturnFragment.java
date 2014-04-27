package cz.rekola.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.model.BorrowedBike;
import cz.rekola.android.api.model.error.MessageError;
import cz.rekola.android.core.bus.BikeBorrowFailedEvent;
import cz.rekola.android.core.bus.BikeBorrowedEvent;

public class ReturnFragment extends BaseMainFragment {

	@InjectView(R.id.bike_name)
	TextView vBikeName;
	@InjectView(R.id.lock_code)
	TextView vLockCode;
	@InjectView(R.id.return_bike)
	Button vReturn;

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
			}
		});
	}

	private void populateData() {
		BorrowedBike borrowedBike= getApp().getDataManager().getBorrowedBike();
		if (borrowedBike == null) {
			vBikeName.setText("No bike borrowed.");
			return;
		}

		vBikeName.setText(borrowedBike.bike.name);
		vLockCode.setText(borrowedBike.lockCode);
	}
}
