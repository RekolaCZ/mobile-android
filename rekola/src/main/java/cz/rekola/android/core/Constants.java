package cz.rekola.android.core;

public class Constants {

	public static final String API_VERSION = "0.0.1";

	public static final long MAP_PERIODIC_UPDATE_MS = 1 * 30 * 1000L; // TODO: 2 * 30 * 1000L;

	public static final String HEADER_KEY_TOKEN = "X-Api-Key";
	public static final String HEADER_KEY_USER_AGENT = "user-agent";

	public static final String HEADER_VALUE_USER_AGENT = "rekola/%s (%s; Android %s; Resolution %sx%s; Dpi %s)";

	public static final String REKOLA_API_URL = "http://vps.clevis.org/rekola-demo/www/api";

	public static final String WEBAPI_BIKE_DETAIL_URL = "http://www.rekola.cz/ucet/sign/password-recovery?mobile=1";
	public static final String WEBAPI_BIKE_ISSUES_URL = "http://www.rekola.cz/ucet/sign/password-recovery?mobile=1";
	public static final String WEBAPI_BIKE_RETURN_URL = "http://www.rekola.cz/ucet/sign/password-recovery?mobile=1";
	public static final String WEBAPI_BIKE_RETURNED_URL = "http://www.rekola.cz/ucet/sign/password-recovery?mobile=1";
	public static final String WEBAPI_PROFILE_URL = "http://dl.dropboxusercontent.com/u/43851739/logout.html";
	public static final String WEBAPI_ABOUT_URL = "http://www.rekola.cz/ucet/sign/password-recovery?mobile=1"; // TODO: Replace with native

	public static final String BROWSER_RECOVER_PASSWORD_URL = "http://www.rekola.cz/ucet/sign/password-recovery?mobile=1";
	public static final String BROWSER_REGISTRATION_URL = "http://www.rekola.cz/register/credentials";

}
