package cz.rekola.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;

/**
 * Text view for one bike code number with simulated cursor
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {13. 8. 2015}
 **/
public class NumberView extends FrameLayout {
    public static final String TAG = NumberView.class.getName();

    @InjectView(R.id.txt_number)
    PointTextView mTxtNumber;

    @InjectView(R.id.img_cursor)
    ImageView mImgCursor;

    private Animation cursorAnimation;


    public NumberView(Context context) {
        super(context);
    }

    public NumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this, this);
        cursorAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cursor_flashing);
    }

    public PointTextView getPointTextView() {
        return mTxtNumber;
    }

    public void showCursor() {
        mImgCursor.setVisibility(VISIBLE);
        mImgCursor.startAnimation(cursorAnimation);
    }


    public void hideCursor() {
        mImgCursor.setVisibility(INVISIBLE);
        mImgCursor.clearAnimation();
    }


}
