package cz.rekola.app.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.otto.Bus;

import java.util.Map;

import cz.rekola.app.R;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.core.bus.ProgressDataLoading;
import cz.rekola.app.webapi.WebApiHandler;

public class ApiWebView extends WebView {

	public ApiWebView(Context context) {
		super(context);
	}

	public ApiWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ApiWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ApiWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
		super(context, attrs, defStyle, privateBrowsing);
	}

	public void init(final Bus bus, WebApiHandler handler, String startUrl, Map<String, String> additionalHttpHeaders) {
		bus.post(new ProgressDataLoading(0)); // Show progress
		final WebApiHandler apiHandler = handler;
		getSettings().setJavaScriptEnabled(true);
		setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (progress > 0 && progress < 100)
					bus.post(new ProgressDataLoading(progress));
			}
		});
		setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				bus.post(new MessageEvent(getResources().getString(R.string.error_web_load_failed)));
				loadData("<html>" + getResources().getString(R.string.error_web_load_failed) +"</head>", "text/html", "UTF-8");
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Uri uri = Uri.parse(url);
				return apiHandler.onWebApiEvent(url);
			}

			@Override
			public void onPageFinished(WebView view, java.lang.String url) {
				bus.post(new ProgressDataLoading(100)); // Hide progress
			}
		});

		loadUrl(startUrl, additionalHttpHeaders);
	}
}
