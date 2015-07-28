package cz.rekola.app.activity;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import cz.rekola.app.utils.KeyboardUtils;
import cz.rekola.app.utils.SoftKeyboard;
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

    @InjectView(R.id.txt_register)
    TextView mTxtRegister;
    @InjectView(R.id.txt_user_name)
    EditText mTxtUserName;
    @InjectView(R.id.txt_password)
    EditText mTxtPassword;
    @InjectView(R.id.txt_reset_user_name)
    EditText mTxtResetUserName;
    @InjectView(R.id.overlay_reset)
    FrameLayout mOverlayReset;
    @InjectView(R.id.overlay_loading)
    LoadingOverlay mOverlayLoading;
    @InjectView(R.id.layout_error_bar)
    MessageBarView mLayoutErrorBar;
    @InjectView(R.id.img_rekola_logo)
    ImageView mImgRekolaLogo;
    @InjectView(R.id.root_layout)
    FrameLayout mRootLayout;
    @InjectView(R.id.ll_bottom)
    LinearLayout mLlBottom;
    @InjectView(R.id.view_dummy)
    View mViewDummy;


    private ViewHelper viewHelper = new ViewHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        attachKeyboardListeners();
        initKeyboardsCallBacks();
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
            showElements();
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRA_MESSAGE)) {
            mLayoutErrorBar.onMessage(new MessageEvent(getIntent().getExtras().getString(EXTRA_MESSAGE)));
            getIntent().removeExtra(EXTRA_MESSAGE);
        }
    }

    @OnClick(R.id.txt_register)
    public void registerOnClick() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BROWSER_REGISTRATION_URL));
        startActivity(browserIntent);
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

    @OnClick(R.id.btn_recover_password)
    public void recoverPasswordOnClick() {
        if (mTxtResetUserName.getText().length() == 0) {
            mTxtResetUserName.setText(mTxtUserName.getText());
        }

        hideElements();
        MyAnimator.showSlideUp(mOverlayReset);
    }

    @OnClick(R.id.btn_reset_recall)
    public void resetRecallOnClick() {
        hideResetView();
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
    public void onBackPressed() {

        if (mOverlayReset.getVisibility() == View.VISIBLE) {
            showElements();
            hideResetView();
        } else {
            super.onBackPressed();
        }
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
        hideResetView();
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

    private void initKeyboardsCallBacks() {
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        SoftKeyboard softKeyboard = new SoftKeyboard(mRootLayout, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {

            @Override
            public void onSoftKeyboardHide() {

                if (isLoginScreenVisible()) {
                    showElements();
                }
            }

            @Override
            public void onSoftKeyboardShow() {

                hideElements();
            }
        });
    }

    private void showElements() {
        mTxtRegister.setVisibility(View.VISIBLE);
        mImgRekolaLogo.setVisibility(View.VISIBLE);
        mLlBottom.setVisibility(View.VISIBLE);

        mViewDummy.setVisibility(View.GONE);
    }

    private void hideElements() {
        mTxtRegister.setVisibility(View.GONE);
        mImgRekolaLogo.setVisibility(View.GONE);
        mLlBottom.setVisibility(View.GONE);

        //hack, this view must be visible, if keyboard is visible,
        // it caused bad scrolling
        mViewDummy.setVisibility(View.VISIBLE);
    }

    private boolean isLoginScreenVisible() {
        return mOverlayLoading.getVisibility() != View.VISIBLE
                && mOverlayReset.getVisibility() != View.VISIBLE;
    }

    private void login() {
        KeyboardUtils.hideKeyboard(this);
        mLayoutErrorBar.hide();
        hideElements();
        mOverlayLoading.show();
        getApp().getDataManager().login(viewHelper.getCredentials());
    }

    private void recoverPassword() {
        KeyboardUtils.hideKeyboard(this);
        getApp().getDataManager().recoverPassword(new RecoverPassword(mTxtResetUserName.getText().toString()));
    }

    private void hideResetView() {
        showElements();
        KeyboardUtils.hideKeyboard(this);
        MyAnimator.hideSlideDown(mOverlayReset);
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
