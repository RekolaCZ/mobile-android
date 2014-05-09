package cz.rekola.android.core.page;

public interface PageController {

	public void requestMap();
	public void requestReturnBike();
	public void requestReturnMap();
	public void requestWebBikeDetail(int id);
	public void requestWebBikeReturned(String successUrl);

}
