package cz.rekola.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.requestmodel.Credentials;
import cz.rekola.android.api.requestmodel.RecoverPassword;
import cz.rekola.android.core.Constants;
import cz.rekola.android.core.RekolaApp;
import cz.rekola.android.core.bus.AuthorizationRequiredEvent;
import cz.rekola.android.core.bus.BorrowedBikeAvailableEvent;
import cz.rekola.android.core.bus.BorrowedBikeFailedEvent;
import cz.rekola.android.core.bus.MessageEvent;
import cz.rekola.android.core.bus.IncompatibleApiEvent;
import cz.rekola.android.core.bus.LoginAvailableEvent;
import cz.rekola.android.core.bus.LoginFailedEvent;
import cz.rekola.android.core.bus.PasswordRecoveryEvent;
import cz.rekola.android.core.bus.PasswordRecoveryFailed;
import cz.rekola.android.view.MessageBarView;

public class LoginActivity extends Activity {

	@InjectView(R.id.username)
	EditText vUsername;
	@InjectView(R.id.password)
	EditText vPassword;
	@InjectView(R.id.login)
	Button vLogin;
	@InjectView(R.id.recover_password)
	TextView vRecoverPassword;
	@InjectView(R.id.registration)
	TextView vRegistration;
	@InjectView(R.id.loading_overlay)
	FrameLayout vLoading;

	@InjectView(R.id.reset_overlay)
	FrameLayout vResetOverlay;
	@InjectView(R.id.reset_username)
	EditText vResetUsername;
	@InjectView(R.id.reset)
	Button vReset;
	@InjectView(R.id.reset_recall)
	TextView vRecall;

	@InjectView(R.id.error_bar)
	MessageBarView errorBar;

	private ViewHelper viewHelper = new ViewHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.inject(this);

		vLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				viewHelper.saveCredentials();
				if (!viewHelper.canLogin()) {
					getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_missing_credentials)));
					return;
				}

				login();
			}
		});

		vRegistration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BROWSER_REGISTRATION_URL));
				startActivity(browserIntent);
			}
		});

		vRecoverPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (vResetUsername.getText().length() == 0) {
					vResetUsername.setText(vUsername.getText());
				}
				vResetOverlay.setVisibility(View.VISIBLE);
			}
		});

		vRecall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideKeyboard();
				vResetOverlay.setVisibility(View.GONE);
			}
		});

		vReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!viewHelper.canReset()) {
					getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_missing_username)));
					return;
				}

				reset();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		getApp().getBus().register(this);
		getApp().getBus().register(errorBar);
		vUsername.setText(getApp().getPreferencesManager().getUsername());
		vPassword.setText(getApp().getPreferencesManager().getPassword());

		if (viewHelper.canLogin()) {
			login();
		} else {
			vLoading.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getApp().getBus().unregister(this);
		getApp().getBus().unregister(errorBar);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Subscribe
	public void loginAvailable(LoginAvailableEvent event) {
		if (getApp().getDataManager().getBorrowedBike() != null) {
			startMainActivity();
		}
	}

	@Subscribe
	public void loginFailed(LoginFailedEvent event) {
		vLoading.setVisibility(View.GONE);
	}

	@Subscribe
	public void passwordRecoveryEvent(PasswordRecoveryEvent event) {
		getApp().getBus().post(new MessageEvent(MessageEvent.MessageType.SUCCESS, getResources().getString(R.string.success_password_recovery)));
		vResetOverlay.setVisibility(View.GONE);
	}

	@Subscribe
	public void passwordRecoveryFailed(PasswordRecoveryFailed event) {
	}

	@Subscribe
	public void isBorrowedBikeAvailable(BorrowedBikeAvailableEvent event) {
		startMainActivity();
	}

	@Subscribe
	public void isBorrowedBikeFailed(BorrowedBikeFailedEvent event) {
		vLoading.setVisibility(View.GONE);
	}

	@Subscribe
	public void onAuthorizationRequired(AuthorizationRequiredEvent event) {
	}

	@Subscribe
	public void onIncompatibleApi(IncompatibleApiEvent event) {
		getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_old_app_version)));
	}

	public void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
	}

	private void login() {
		hideKeyboard();
		errorBar.hide();
		vLoading.setVisibility(View.VISIBLE);
		getApp().getDataManager().login(viewHelper.getCredentials());
	}

	private void reset() {
		hideKeyboard();
		getApp().getDataManager().recoverPassword(new RecoverPassword(vResetUsername.getText().toString()));
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

		private boolean canReset() {
			return (vResetUsername.getText().length() > 0 && vResetUsername.getText().toString().contains("@"));
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
