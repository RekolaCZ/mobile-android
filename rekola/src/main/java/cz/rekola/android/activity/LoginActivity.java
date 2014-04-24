package cz.rekola.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.ApiService;
import cz.rekola.android.api.loader.BikesLoader;
import cz.rekola.android.api.model.Token;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.core.RekolaApp;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends Activity {

	@InjectView(R.id.username)
	EditText vUsername;
	@InjectView(R.id.password)
	EditText vPassword;
	@InjectView(R.id.login)
	Button vLogin;
	@InjectView(R.id.recover_password)
	Button vRecoverPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.inject(this);

		vLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});

		vRecoverPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ApiService apiService = getApp().getApiService();
				apiService.login(new Credentials("demo@vitekjezek.com", "heslo"), new Callback<Token>() {
					@Override
					public void success(Token resp, Response response) {
						Toast.makeText(LoginActivity.this, "Success: " + response.getStatus(), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void failure(RetrofitError error) {
						Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
					}
				});
				//BikesLoader bikesLoader = new BikesLoader();
				//bikesLoader.load(getApp());
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	private RekolaApp getApp() {
		return (RekolaApp) getApplication();
	}
}
