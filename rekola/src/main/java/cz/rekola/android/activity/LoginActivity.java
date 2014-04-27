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
import cz.rekola.android.core.bus.IsBorrowedBikeAvailableEvent;
import cz.rekola.android.core.bus.IsBorrowedBikeFailedEvent;
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

	private ViewHelper viewHelper = new ViewHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.inject(this);
		getApp().getBus().register(this);

		vUsername.setText(getApp().getPreferencesManager().getUsername());
		vPassword.setText(getApp().getPreferencesManager().getPassword());

		vLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				viewHelper.saveCredentials();
				if (!viewHelper.canLogin()) {
					Toast.makeText(LoginActivity.this, "Fill in the username and password!", Toast.LENGTH_SHORT).show();
					return;
				}

				getApp().getDataManager().login(viewHelper.getCredentials());
			}
		});

		vRecoverPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(LoginActivity.this, "Go to the web!", Toast.LENGTH_SHORT).show();
			}
		});

		if (viewHelper.canLogin()) {
			getApp().getDataManager().login(viewHelper.getCredentials());
		}
	}

	@Override
	public void onDestroy() {
		getApp().getBus().unregister(this);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Subscribe
	public void loginAvailable(LoginAvailableEvent event) {
		Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
		if (getApp().getDataManager().isBorrowedBike() != null) {
			startMainActivity();
		}
	}

	@Subscribe
	public void loginFailed(LoginFailedEvent event) {
		Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
	}

	@Subscribe
	public void isBorrowedBikeAvailable(IsBorrowedBikeAvailableEvent event) {
		Toast.makeText(LoginActivity.this, "Is bike borrowed known", Toast.LENGTH_SHORT).show();
		startMainActivity();
	}

	@Subscribe
	public void isBorrowedBikeFailed(IsBorrowedBikeFailedEvent event) {
		Toast.makeText(LoginActivity.this, "Bike borrowed error", Toast.LENGTH_SHORT).show();
	}

	private void startMainActivity() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
	}

	private RekolaApp getApp() {
		return (RekolaApp) getApplication();
	}

	private class ViewHelper {

		private boolean canLogin() {
			return (vUsername.getText().length() > 0 && vPassword.getText().length() > 0);
		}

		private Credentials getCredentials() {
			return new Credentials(vUsername.getText().toString(), vPassword.getText().toString());
		}

		private void saveCredentials() {
			getApp().getPreferencesManager().setUsername(vUsername.getText().toString());
			getApp().getPreferencesManager().setPassword(vPassword.getText().toString());
		}
	}
}
