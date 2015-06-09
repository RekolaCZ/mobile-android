package cz.rekola.app.core;

public class Constants {

    public static final String API_VERSION = "1.0.0";

    public static final long MAP_PERIODIC_UPDATE_MS = 2 * 60 * 1000L; // 1 * 30 * 1000L;

    public static final String HEADER_KEY_TOKEN = "X-Api-Key";
    //public static final String HEADER_KEY_USER_AGENT = "client-agent";
    public static final String HEADER_KEY_API_VERSION = "x-api-version";
    public static final String HEADER_KEY_ACCEPT_LANGUAGE = "accept-language";
    public static final String HEADER_KEY_OS = "client-os";

    public static final String HEADER_VALUE_USER_AGENT = "rekola/%s (Model %s; Android %s; Locale %s; Resolution %sx%s; Dpi %s)";

    public static final String REKOLA_API_URL = "https://moje.rekola.cz/api";

    public static final String WEBAPI_BIKE_DETAIL_URL = REKOLA_API_URL + "/bikes/%s/info-webview";
    public static final String WEBAPI_BIKE_ISSUES_URL = REKOLA_API_URL + "/bikes/%s/issues-webview";
    public static final String WEBAPI_BIKE_RETURNED_URL = REKOLA_API_URL + "/bikes/%s/status-webview";
    public static final String WEBAPI_PROFILE_URL = REKOLA_API_URL + "/accounts/mine/profile-webview";

    public static final String BROWSER_REGISTRATION_URL = "http://www.rekola.cz/register/credentials";

}
