package cz.rekola.app.fragment.natural;

import android.app.Service;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.api.model.error.MessageError;
import cz.rekola.app.core.bus.LockCodeEvent;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.core.bus.dataFailed.LockCodeFailedEvent;
import cz.rekola.app.fragment.base.BaseMainFragment;
import cz.rekola.app.utils.KeyboardUtils;
import cz.rekola.app.utils.SoftKeyboard;
import cz.rekola.app.view.CodeView;
import cz.rekola.app.view.EditTextPoint;

/**
 * Screen to borrow bike
 */

public class BorrowFragment extends BaseMainFragment {

    @InjectView(R.id.root_layout)
    ScrollView mRootLayout;
    @InjectView(R.id.txt_bike_code)
    CodeView mTxtBikeCode;
    @InjectView(R.id.txt_code_hint_keyboard_hidden)
    TextView mTxtCodeHintKeyboardHidden;
    @InjectView(R.id.txt_code_hint_keyboard_visible)
    TextView mTxtCodeHintKeyboardVisible;
    @InjectView(R.id.img_rekola_logo)
    ImageView mImgRekolaLogo;
    @InjectView(R.id.txt_borrow_info)
    TextView mTxtBorrowInfo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrow, container, false);
        ButterKnife.inject(this, view);
        initKeyboardsCallBacks();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden && !mTxtBikeCode.isCodeHintVisible()) {
            KeyboardUtils.showKeyboard(getActivity());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.btn_borrow)
    public void borrowOnClick() {
        if (mTxtBikeCode.getText().length() != getResources().getInteger(R.integer.bike_code_length)) {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_incorrect_bike_code)));
            return;
        }
        KeyboardUtils.hideKeyboard(getActivity());
        getApp().getDataManager().borrowBike(Integer.parseInt(mTxtBikeCode.getText()));
    }

    @Subscribe
    public void bikeBorrowed(LockCodeEvent event) {
        getPageController().requestReturnBike();
    }

    @Subscribe
    public void bikeBorrowFailed(LockCodeFailedEvent event) {
        if (event.error != null && event.error instanceof MessageError) {
            getApp().getBus().post(new MessageEvent(((MessageError) event.error).message));
        } else {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_borrow_bike_failed)));
        }
    }

    private void initKeyboardsCallBacks() {
        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Service
                .INPUT_METHOD_SERVICE);

        SoftKeyboard softKeyboard = new SoftKeyboard(mRootLayout, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {

            @Override
            public void onSoftKeyboardHide() {
                mImgRekolaLogo.setVisibility(View.VISIBLE);
                mTxtBorrowInfo.setVisibility(View.VISIBLE);
                mTxtCodeHintKeyboardHidden.setVisibility(View.VISIBLE);
                mTxtBikeCode.setHintVisibility(true);

                mTxtCodeHintKeyboardVisible.setVisibility(View.GONE);

            }

            @Override
            public void onSoftKeyboardShow() {
                mImgRekolaLogo.setVisibility(View.GONE);
                mTxtBorrowInfo.setVisibility(View.GONE);
                mTxtCodeHintKeyboardHidden.setVisibility(View.GONE);
                mTxtBikeCode.setHintVisibility(false);

                mTxtCodeHintKeyboardVisible.setVisibility(View.VISIBLE);
            }
        });
    }

}
