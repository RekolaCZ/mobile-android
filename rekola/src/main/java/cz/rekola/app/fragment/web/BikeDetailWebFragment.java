package cz.rekola.app.fragment.web;

import cz.rekola.app.R;
import cz.rekola.app.fragment.base.BaseWebFragment;

/**
 * Screen to bike detail as webview (used to serviceman)
 */

public class BikeDetailWebFragment extends BaseWebFragment {

    private int id;

    public void init(int id) {
        this.id = id;
    }

    @Override
    public String getStartUrl() {
        return String.format(getAct()
                .getResources()
                .getString(R.string.rekola_bike_detail_url), id);
    }

}
