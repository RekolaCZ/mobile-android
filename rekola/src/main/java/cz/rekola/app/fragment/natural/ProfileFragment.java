package cz.rekola.app.fragment.natural;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.Utils.DateUtils;
import cz.rekola.app.activity.MainActivity;
import cz.rekola.app.api.model.user.Account;
import cz.rekola.app.core.bus.BikesAvailableEvent;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseMainFragment {

    @InjectView(R.id.txt_user_name)
    TextView mTxtUserName;
    @InjectView(R.id.txt_membership_end_date)
    TextView mTxtMembershipEndDate;
    @InjectView(R.id.txt_email)
    TextView mTxtEmail;
    @InjectView(R.id.txt_address)
    TextView mTxtAddress;
    @InjectView(R.id.txt_phone)
    TextView mTxtPhone;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        if (getApp().getDataManager().getAccount() != null) {
            setupAccount();
        }
    }

    @OnClick(R.id.btn_about)
    public void startAboutFragment() {
        getPageController().requestAbout();
    }

    @OnClick(R.id.btn_logout)
    public void logout() {
        getApp().getPreferencesManager().setPassword(null);
        getApp().getDataManager().logout();
        ((MainActivity) getActivity()).startLoginActivity(null);
        getActivity().finish();
    }

    @Subscribe
    public void accountAvailable(BikesAvailableEvent event) {
        setupAccount();
    }


    private void setupAccount() {
        Account account = getApp().getDataManager().getAccount();
        if (account == null)
            return;

        mTxtUserName.setText(account.name);
        mTxtMembershipEndDate.setText(DateUtils.getDate(account.membershipEnd));
        mTxtEmail.setText(account.email);
        mTxtAddress.setText(account.address);
        mTxtPhone.setText(account.phone);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
