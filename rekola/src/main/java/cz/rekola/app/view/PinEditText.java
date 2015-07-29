package cz.rekola.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import cz.rekola.app.R;

/**
 * Edit text with point in left right corner (used in CodeView)
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {14. 7. 2015}
 **/
public class PinEditText extends EditText {
    public static final String TAG = PinEditText.class.getName();

    private Paint mPaint;
    private boolean mPointVisibility = true;

    public PinEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.grey8));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPointVisibility) {
            int radius = 2;

            final float densityMultiplier = getContext().getResources().getDisplayMetrics().density;
            final float scaledRadius = radius * densityMultiplier;

            canvas.drawCircle(getWidth() - scaledRadius, getHeight() - scaledRadius, scaledRadius, mPaint);
        }

        super.onDraw(canvas);
    }

    /**
     * use custom InputConnection because of bug with not retrieving del key on some devices
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        super.onCreateInputConnection(outAttrs);

        /**
         * http://stackoverflow.com/questions/5419766/how-to-capture-soft-keyboard-input-in-a-view
         * The false second argument puts the BaseInputConnection into "dummy" mode,
         * which is also required in order for the raw key events to be sent to your view.
         */
        return new CustomInputConnection(this, false);
    }

    /**
     * draw point in right bottom corner
     *
     * @param pointVisibility point is visible/invisible
     */
    public void setPointVisibility(boolean pointVisibility) {
        mPointVisibility = pointVisibility;
    }
}



