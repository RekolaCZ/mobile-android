package cz.rekola.android.core.version;

import android.app.Application;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

import cz.rekola.android.core.Constants;

public class VersionManager {

	private String userAgent; // Using client-agent header, see Constants.
	private String webUserAgent;

	public VersionManager(Application app) {
		String brand = Build.BRAND;
		String model = Build.MODEL;
		int sdkVersion = Build.VERSION.SDK_INT;

		Locale locale = app.getResources().getConfiguration().locale;

		DisplayMetrics metrics = app.getResources().getDisplayMetrics();
		int dpi = metrics.densityDpi;
		int height = metrics.heightPixels;
		int width = metrics.widthPixels;

		String httpAgent = System.getProperty("http.agent");

		userAgent = String.format(Constants.HEADER_VALUE_USER_AGENT, Constants.API_VERSION, brand + " " + model, sdkVersion, locale, width, height, dpi);
		webUserAgent = userAgent + "; " + httpAgent;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getWebUserAgent() {
		return webUserAgent;
	}

}
