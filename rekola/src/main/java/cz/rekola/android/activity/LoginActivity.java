package cz.rekola.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.bus.LoginAvailableEvent;
import cz.rekola.android.core.bus.LoginFailedEvent;

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
		getApp().getBus().register(this);

		vUsername.setText(getApp().getPreferencesManager().getUsername());

		vLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getApp().getPreferencesManager().setUsername(vUsername.getText().toString());
				if (vUsername.getText().length() == 0) {
					Toast.makeText(LoginActivity.this, "Fill in username!", Toast.LENGTH_SHORT).show();
					return;
				}
				if (vPassword.getText().length() == 0) {
					Toast.makeText(LoginActivity.this, "Fill in password!", Toast.LENGTH_SHORT).show();
					return;
				}
				getApp().getDataManager().login(new Credentials(vUsername.getText().toString(), vPassword.getText().toString()));
			}
		});

		vRecoverPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(LoginActivity.this, "Go to the web!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onDestroy() {
		getApp().getBus().unregister(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Subscribe
	public void loginAvailable(LoginAvailableEvent event) {
		Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
	}

	@Subscribe
	public void loginFailed(LoginFailedEvent event) {
		Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
	}

	/*private void login() {
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
	}*/

	private RekolaApp getApp() {
		return (RekolaApp) getApplication();
	}
}
