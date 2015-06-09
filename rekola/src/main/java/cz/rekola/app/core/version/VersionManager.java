package cz.rekola.app.core.version;

import android.app.Application;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

import cz.rekola.app.core.Constants;

public class VersionManager {

    private final String userAgent; // Using client-agent header, see Constants.
    private final String webUserAgent;
    private final String acceptVersion;
    private final String clientOs;

    public VersionManager(Application app) {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        int sdkVersion = Build.VERSION.SDK_INT;

        clientOs = "android " + sdkVersion;

        Locale locale = app.getResources().getConfiguration().locale;
        acceptVersion = locale.toString();

        DisplayMetrics metrics = app.getResources().getDisplayMetrics();
        int dpi = metrics.densityDpi;
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        String httpAgent = System.getProperty("http.agent");

        userAgent = String.format(Constants.HEADER_VALUE_USER_AGENT, Constants.API_VERSION, brand + " " + model, sdkVersion, locale, width, height, dpi);
        webUserAgent = userAgent + "; " + httpAgent;
    }

	/*public String getUserAgent() {
        return userAgent;
	}

	/*public String getWebUserAgent() {
		return webUserAgent;
	}*/

    public String getOs() {
        return clientOs;
    }

    public String getApiVersion() {
        return Constants.API_VERSION;
    }

    public String getAcceptLanguage() {
        return acceptVersion;
    }
}
