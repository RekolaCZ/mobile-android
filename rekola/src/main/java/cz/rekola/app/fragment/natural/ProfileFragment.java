package cz.rekola.app.fragment.natural;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.activity.MainActivity;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseMainFragment {

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @OnClick(R.id.btnAbout)
    public void startAboutFragment() {
        getPageController().requestAbout();
    }

    @OnClick(R.id.tvLogout)
    public void logout() {
        getApp().getPreferencesManager().setPassword(null);
        getApp().getDataManager().logout();
        ((MainActivity) getActivity()).startLoginActivity(null);
        getActivity().finish();
    }


}
