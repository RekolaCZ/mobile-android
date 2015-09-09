package cz.rekola.app.fragment.natural;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.core.Constants;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Screen about app
 */
public class AboutFragment extends BaseMainFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @OnClick(R.id.img_ackee_logo)
    public void onLogoClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ACKEE_WEB));
        startActivity(browserIntent);
    }

}
