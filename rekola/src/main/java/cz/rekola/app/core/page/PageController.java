package cz.rekola.app.core.page;

public interface PageController {

    public void requestMap();

    public void requestReturnBike();

    public void requestReturnMap();

    public void requestAbout();

    public void requestBikeDetail(int bikeID);

    public void requestWebBikeReturned(String successUrl);

    public void requestAddIssue();

}
