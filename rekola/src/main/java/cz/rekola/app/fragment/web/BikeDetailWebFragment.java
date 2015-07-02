package cz.rekola.app.fragment.web;

import cz.rekola.app.core.Constants;
import cz.rekola.app.fragment.base.BaseWebFragment;

/**
 * Screen to bike detail as webview (used to serviceman)
 */

public class BikeDetailWebFragment extends BaseWebFragment {

    private int id;
    private boolean issues;

    public void init(int id, boolean issues) {
        this.id = id;
        this.issues = issues;
    }

    @Override
    public String getStartUrl() {
        return issues ? String.format(Constants.WEBAPI_BIKE_ISSUES_URL, id) : String.format(Constants.WEBAPI_BIKE_DETAIL_URL, id);
    }

}
