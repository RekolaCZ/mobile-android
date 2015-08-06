package cz.rekola.app.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.utils.KeyboardUtils;

/**
 * View to set pin code (6 editbox)
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {14. 7. 2015}
 **/
public class CodeView extends FrameLayout {
    public static final String TAG = CodeView.class.getName();

    @InjectViews({R.id.txt_code_0, R.id.txt_code_1, R.id.txt_code_2, R.id.txt_code_3,
            R.id.txt_code_4, R.id.txt_code_5})
    List<PinEditText> mTxtCodeList;

    @InjectView(R.id.txt_code_hint)
    TextView mTxtCodeHint;
    @InjectView(R.id.ll_code)
    LinearLayout mLlCode;

    public CodeView(Context context) {
        super(context);
    }

    public CodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this, this);

        setEditTextListeners();

        //according to design last EditText is without point
        mTxtCodeList.get(mTxtCodeList.size() - 1).setPointVisibility(false);
    }


    public void codeHintOnClick() {
        setHintVisibility(false);
        EditText firstEditText = mTxtCodeList.get(0);
        firstEditText.requestFocus();
        KeyboardUtils.showKeyboard(getContext(), firstEditText);
    }

    public boolean isCodeHintVisible() {
        return mTxtCodeHint.getVisibility() == VISIBLE;
    }

    public String getText() {
        StringBuilder pinCode = new StringBuilder(mTxtCodeList.size());
        for (EditText txtCode : mTxtCodeList) {
            pinCode.append(txtCode.getText());
        }

        return pinCode.toString();
    }

    public void setHintVisibility(boolean visible) {
        if (visible) {
            mTxtCodeHint.setVisibility(VISIBLE);
            mLlCode.setVisibility(GONE);
        } else {
            mTxtCodeHint.setVisibility(GONE);
            mLlCode.setVisibility(VISIBLE);
        }

    }

    private void setEditTextListeners() {
        for (int i = 0; i < mTxtCodeList.size(); i++) {
            EditText txtCode = mTxtCodeList.get(i);
            setTextChangedListener(txtCode, i);
            setOnKeyListener(txtCode, i);
        }
    }

    private void setTextChangedListener(final EditText txtCode, final int position) {

        txtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable text) {
                //if user wrote one number, jump to another EditText
                if (position + 1 < mTxtCodeList.size() && !text.toString().equals("")) {
                    mTxtCodeList.get(position + 1).requestFocus();
                }
            }
        });
    }

    private void setOnKeyListener(final EditText txtCode, final int position) {
        txtCode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                //if user press delete and there is no number, jump to previous EditText
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (position != 0 && txtCode.getText().toString().equals("")) {
                        mTxtCodeList.get(position - 1).requestFocus();
                    }
                }
                return false;
            }
        });


    }
}
