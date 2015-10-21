package cz.rekola.app.core;

import android.app.AlarmManager;

public class Constants {

    public static final String API_VERSION = "1.0.0";

    public static final long MAP_PERIODIC_UPDATE_MS = 2 * 60 * 1000L; // 1 * 30 * 1000L;

    public static final String HEADER_KEY_TOKEN = "X-Api-Key";
    //public static final String HEADER_KEY_USER_AGENT = "client-agent";
    public static final String HEADER_KEY_API_VERSION = "x-api-version";
    public static final String HEADER_KEY_ACCEPT_LANGUAGE = "accept-language";
    public static final String HEADER_KEY_OS = "client-os";

    public static final String HEADER_VALUE_USER_AGENT = "rekola/%s (Model %s; Android %s; Locale %s; Resolution %sx%s; Dpi %s)";

    public static final int DEFAULT_MAP_ZOOM_LEVEL = 15;
    public static final int MAX_CLUSTERING_ZOOM_LEVEL = 19;

    public static final String BROWSER_REGISTRATION_URL = "https://www.rekola.cz/registrace";

    //if there is more equipments, it will add three points (symbol for more = ...)
    public static final int MAX_COUNT_OF_VISIBLE_EQUIPMENTS = 4;
    public static final String ACKEE_WEB = "http://ackee.cz";

    public static final long CHECK_IF_BIKE_IS_RETURNED_TIME = AlarmManager.INTERVAL_HOUR * 3;
}
