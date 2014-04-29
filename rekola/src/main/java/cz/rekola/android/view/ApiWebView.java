package cz.rekola.android.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URL;

import cz.rekola.android.core.RekolaApp;

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

	public void setData(String url) {
		getSettings().setJavaScriptEnabled(true);
		setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
			}
		});
		setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(getContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				boolean shouldOverride = false;
				Uri uri = Uri.parse(url);

				return shouldOverride;
			}
		});

		loadUrl(url);
	}
}
