package cz.rekola.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.model.error.MessageError;
import cz.rekola.android.core.bus.LockCodeEvent;
import cz.rekola.android.core.bus.LockCodeFailedEvent;

public class BorrowFragment extends BaseMainFragment {

	@InjectView(R.id.bike_code)
	EditText vBikeCode;
	@InjectView(R.id.borrow)
	Button vBorrow;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_borrow, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		vBorrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (vBikeCode.getText().length() != getResources().getInteger(R.integer.bike_code_length)) {
					Toast.makeText(getActivity(), "Incorrect bike code!", Toast.LENGTH_SHORT).show();
					return;
				}
				getApp().getDataManager().borrowBike(Integer.parseInt(vBikeCode.getText().toString()));
			}
		});
	}

	@Subscribe
	public void bikeBorrowed(LockCodeEvent event) {
		Toast.makeText(getActivity(), "Bike borrowed!", Toast.LENGTH_SHORT).show();
		getPageController().requestReturnBike();
	}

	@Subscribe
	public void bikeBorrowFailed(LockCodeFailedEvent event) {
		if (event.error != null && event.error instanceof MessageError) {
			Toast.makeText(getActivity(), ((MessageError)event.error).message, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(), "Failed to borrow the bike!", Toast.LENGTH_SHORT).show();
		}
	}

}
