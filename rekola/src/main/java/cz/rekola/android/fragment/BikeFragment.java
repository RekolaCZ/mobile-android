package cz.rekola.android.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.Constants;
import cz.rekola.android.core.RekolaApp;

public class BikeFragment extends BaseMainFragment {

	@InjectView(R.id.bike_web)
	WebView vWeb;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_bike, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		vWeb.getSettings().setJavaScriptEnabled(true);
		vWeb.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
			}
		});

		vWeb.setWebViewClient(new WebViewClient() {

			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				boolean shouldOverride = false;
				Uri uri = Uri.parse(url);
				String param1 = uri.getQueryParameter("navigate_back");
				if (url.endsWith("navigate_back=true")) {
					Activity act = getActivity();
					RekolaApp app = (RekolaApp)act.getApplication();
					app.getPreferencesManager().setPassword("");
					act.finish();
					shouldOverride = true;
				}
				return shouldOverride;
			}
		});

		vWeb.loadUrl(Constants.WEBAPI_BIKE_URL);
	}


}
