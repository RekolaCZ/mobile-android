package cz.rekola.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import cz.rekola.app.R;

/**
 * Textview with point in left right corner (used in CodeView)
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {14. 7. 2015}
 **/
public class PointTextView extends TextView {
    public static final String TAG = PointTextView.class.getName();

    private Paint mPaint;
    private boolean mPointVisibility = true;

    public PointTextView(Context context, AttributeSet attrs) {
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
     * draw point in right bottom corner
     *
     * @param pointVisibility point is visible/invisible
     */
    public void setPointVisibility(boolean pointVisibility) {
        mPointVisibility = pointVisibility;
    }
}



