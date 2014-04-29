package cz.rekola.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;

public class ProfileFragment extends BaseMainFragment {

	@InjectView(R.id.logout)
	Button vLogout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		vLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getApp().getPreferencesManager().setPassword("");
				getActivity().finish();
			}
		});
	}

}
