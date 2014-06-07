package cz.rekola.app.core.page;

public interface PageController {

	public void requestMap();
	public void requestReturnBike();
	public void requestReturnMap();
	public void requestWebBikeDetail(int id, boolean issues);
	public void requestWebBikeReturned(String successUrl);

}
