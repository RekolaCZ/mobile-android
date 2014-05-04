package cz.rekola.android.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Map;

import cz.rekola.android.webapi.WebApiHandler;

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

	public void init(WebApiHandler handler, String startUrl, Map<String, String> additionalHttpHeaders) {
		final WebApiHandler apiHandler = handler;
		getSettings().setJavaScriptEnabled(true);
		setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
			}
		});
		setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(getContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
				// TODO: Handle api key expiration!
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Uri uri = Uri.parse(url);
				return apiHandler.onWebApiEvent(url);
			}
		});

		loadUrl(startUrl, additionalHttpHeaders);
	}
}
