package cz.rekola.android.core;

public class Constants {

	public static final String API_VERSION = "0.0.1";

	public static final long MAP_PERIODIC_UPDATE_MS = 2 * 60 * 1000L; // 1 * 30 * 1000L;

	public static final String HEADER_KEY_TOKEN = "X-Api-Key";
	public static final String HEADER_KEY_USER_AGENT = "client-agent";

	public static final String HEADER_VALUE_USER_AGENT = "rekola/%s (Model %s; Android %s; Locale %s; Resolution %sx%s; Dpi %s)";

	public static final String REKOLA_API_URL = "http://vps.clevis.org/rekola-demo/www/api";

	public static final String WEBAPI_BIKE_DETAIL_URL = REKOLA_API_URL + "/bikes/%s/info-webview";
	public static final String WEBAPI_BIKE_ISSUES_URL = REKOLA_API_URL + "/bikes/%s/issues-webview";
	public static final String WEBAPI_BIKE_RETURNED_URL = REKOLA_API_URL + "/bikes/%s/status-webview";
	public static final String WEBAPI_PROFILE_URL = REKOLA_API_URL + "/accounts/mine/profile-webview"; //"http://dl.dropboxusercontent.com/u/43851739/logout.html";

	public static final String BROWSER_REGISTRATION_URL = "http://www.rekola.cz/register/credentials";

}
