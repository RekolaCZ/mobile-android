package cz.rekola.app.core.page;

import java.util.ArrayList;

import cz.rekola.app.api.model.bike.Bike;
import cz.rekola.app.core.interfaces.SetIssueItemInterface;

public interface PageController {

    public void requestMap();

    public void requestReturnBike();

    public void requestReturnMap();

    public void requestAbout();

    public void requestBikeDetail(int bikeID);

    public void requestSpinnerList(ArrayList<String> listItems, SetIssueItemInterface setIssueItemInterface);

    public void requestWebBikeReturned(String successUrl);

    public void requestAddIssue(int bikeID);

    public void requestPrevState();

}
