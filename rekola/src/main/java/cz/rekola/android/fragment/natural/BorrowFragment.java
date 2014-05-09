package cz.rekola.android.fragment.natural;

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
import cz.rekola.android.core.bus.ErrorMessageEvent;
import cz.rekola.android.core.bus.LockCodeEvent;
import cz.rekola.android.core.bus.LockCodeFailedEvent;
import cz.rekola.android.fragment.base.BaseMainFragment;

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
					getApp().getBus().post(new ErrorMessageEvent(getResources().getString(R.string.error_incorrect_bike_code)));
					return;
				}
				getApp().getDataManager().borrowBike(Integer.parseInt(vBikeCode.getText().toString()));
			}
		});
	}

	@Subscribe
	public void bikeBorrowed(LockCodeEvent event) {
		getPageController().requestReturnBike();
	}

	@Subscribe
	public void bikeBorrowFailed(LockCodeFailedEvent event) {
		if (event.error != null && event.error instanceof MessageError) {
			getApp().getBus().post(new ErrorMessageEvent(((MessageError)event.error).message));
		} else {
			getApp().getBus().post(new ErrorMessageEvent(getResources().getString(R.string.error_borrow_bike_failed)));
		}
	}

}
