package cz.rekola.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.activity.base.BaseActivity;
import cz.rekola.app.api.requestmodel.Credentials;
import cz.rekola.app.api.requestmodel.RecoverPassword;
import cz.rekola.app.core.Constants;
import cz.rekola.app.core.anim.MyAnimator;
import cz.rekola.app.core.bus.AuthorizationRequiredEvent;
import cz.rekola.app.core.bus.IncompatibleApiEvent;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.core.bus.PasswordRecoveryEvent;
import cz.rekola.app.core.bus.dataAvailable.BorrowedBikeAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.LoginAvailableEvent;
import cz.rekola.app.core.bus.dataFailed.BorrowedBikeFailedEvent;
import cz.rekola.app.core.bus.dataFailed.LoginFailedEvent;
import cz.rekola.app.core.bus.dataFailed.PasswordRecoveryFailed;
import cz.rekola.app.view.LoadingOverlay;
import cz.rekola.app.view.MessageBarView;

/**
 * Activity is used, when user is login or if he need need recover password
 * <p/>
 * first is showed login activity, if user want recover password, mOverlayReset overlay old view
 * if user recover his password mOverlayReset will hide
 */

public class LoginActivity extends BaseActivity {

    public static final String EXTRA_MESSAGE = "extra_message";
    @InjectView(R.id.txt_user_name)
    EditText mTxtUserName;
    @InjectView(R.id.txt_password)
    EditText mTxtPassword;
    @InjectView(R.id.btn_login)
    EditText mTxtResetUserName;
    @InjectView(R.id.btn_reset_password)
    FrameLayout mOverlayReset;
    @InjectView(R.id.txt_loading_message)
    LoadingOverlay mOverlayLoading;
    @InjectView(R.id.layout_error_bar)
    MessageBarView mLayoutErrorBar;

    private ViewHelper viewHelper = new ViewHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getApp().getBus().register(this);
        getApp().getBus().register(mLayoutErrorBar);
        mTxtUserName.setText(getApp().getPreferencesManager().getUsername());
        mTxtPassword.setText(getApp().getPreferencesManager().getPassword());

        if (viewHelper.canLogin()) {
            login();
        } else {
            mOverlayLoading.hide();
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRA_MESSAGE)) {
            mLayoutErrorBar.onMessage(new MessageEvent(getIntent().getExtras().getString(EXTRA_MESSAGE)));
            getIntent().removeExtra(EXTRA_MESSAGE);
        }
    }

    @OnClick(R.id.btn_login)
    public void loginOnClick() {
        viewHelper.saveCredentials();
        if (!viewHelper.canLogin()) {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_missing_credentials)));
            return;
        }

        login();
    }

    @OnClick(R.id.btn_registration)
    public void registrationOnClick() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BROWSER_REGISTRATION_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.btn_recover_password)
    public void recoverPasswordOnClick() {
        if (mTxtResetUserName.getText().length() == 0) {
            mTxtResetUserName.setText(mTxtResetUserName.getText());
        }

        MyAnimator.showSlideUp(mOverlayReset);
    }

    @OnClick(R.id.btn_reset_recall)
    public void resetRecallOnClick() {
        hideKeyboard();
        MyAnimator.hideSlideDown(mOverlayReset);
    }

    @OnClick(R.id.btn_reset_password)
    public void resetPasswordOnClick() {
        if (!viewHelper.canReset()) {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_missing_username)));
            return;
        }

        recoverPassword();
    }

    @Override
    public void onPause() {
        super.onPause();
        getApp().getBus().unregister(this);
        getApp().getBus().unregister(mLayoutErrorBar);
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
        getApp().getDataManager().getBorrowedBike(true);
        mOverlayLoading.setProgress();
    }

    @Subscribe
    public void loginFailed(LoginFailedEvent event) {
        mOverlayLoading.hide();
    }

    @Subscribe
    public void passwordRecoveryEvent(PasswordRecoveryEvent event) {
        getApp().getBus().post(new MessageEvent(MessageEvent.MessageType.SUCCESS, getResources().getString(R.string.success_password_recovery)));
        MyAnimator.hideSlideDown(mOverlayReset);
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
        mOverlayLoading.hide();
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
        mLayoutErrorBar.hide();
        mOverlayLoading.show();
        getApp().getDataManager().login(viewHelper.getCredentials());
    }

    private void recoverPassword() {
        hideKeyboard();
        getApp().getDataManager().recoverPassword(new RecoverPassword(mTxtResetUserName.getText().toString()));
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private class ViewHelper {

        private boolean canLogin() {
            return (mTxtUserName.getText().length() > 0 && mTxtPassword.getText().length() > 0);
        }

        private boolean canReset() {
            return (mTxtResetUserName.getText().length() > 0
                    && mTxtResetUserName.getText().toString().contains("@"));
        }

        private Credentials getCredentials() {
            return new Credentials(mTxtUserName.getText().toString(), mTxtPassword.getText().toString());
        }

        private void saveCredentials() {
            getApp().getPreferencesManager().setUsername(mTxtUserName.getText().toString());
            getApp().getPreferencesManager().setPassword(mTxtPassword.getText().toString());
        }
    }
}
