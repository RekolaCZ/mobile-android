package cz.rekola.app.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import cz.rekola.app.R;
import cz.rekola.app.utils.KeyboardUtils;

/**
 * View to set pin code (6 Textview + 1 EditText)
 * User write in invisible edittext, so it is faster than jumping in edittext (mTxtCodeHidden)
 * Content is shown in Textviews
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {14. 7. 2015}
 **/
public class CodeView extends FrameLayout {
    public static final String TAG = CodeView.class.getName();

    @InjectViews({R.id.txt_code_0, R.id.txt_code_1, R.id.txt_code_2, R.id.txt_code_3,
            R.id.txt_code_4, R.id.txt_code_5})
    List<NumberView> mTxtCodeList;

    @InjectView(R.id.txt_code_hidden)
    EditText mTxtCodeHidden;


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


        for (NumberView txtCode : mTxtCodeList) {
            txtCode.getPointTextView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    codeHintOnClick();
                }
            });
        }

        setEditTextListeners();

        //according to design last EditText is without point
        mTxtCodeList.get(mTxtCodeList.size() - 1).getPointTextView().setPointVisibility(false);
    }


    public void codeHintOnClick() {
        mTxtCodeHidden.requestFocus();
        KeyboardUtils.showKeyboard(getContext(), mTxtCodeHidden);
        setCursor();
    }

    public String getText() {
        StringBuilder pinCode = new StringBuilder(mTxtCodeList.size());
        for (NumberView txtCode : mTxtCodeList) {
            pinCode.append(txtCode.getPointTextView().getText());
        }

        return pinCode.toString();
    }


    private void setEditTextListeners() {
        mTxtCodeHidden.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable text) {

                //set corresponded code number
                for (int i = 0; i < text.length(); i++) {
                    mTxtCodeList.get(i).getPointTextView().setText(text.subSequence(i, i + 1));
                }

                //set rest of code numbers to empty string
                for (int i = text.length(); i < mTxtCodeList.size(); i++) {
                    mTxtCodeList.get(i).getPointTextView().setText("");
                }

                //set cursor visibility
                setCursor();
            }
        });
    }


    private void setCursor() {
        for (int i = 0; i < mTxtCodeList.size(); i++) {
            if (i == mTxtCodeHidden.getText().length()) {
                mTxtCodeList.get(i).showCursor();
            } else {
                mTxtCodeList.get(i).hideCursor();
            }
        }
    }
}
