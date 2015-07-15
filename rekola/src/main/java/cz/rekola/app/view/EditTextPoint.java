package cz.rekola.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import cz.rekola.app.R;

/**
 * Edit text with point in left right corner (used in CodeView)
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {14. 7. 2015}
 **/
public class EditTextPoint extends EditText {
    public static final String TAG = EditTextPoint.class.getName();

    private Paint mPaint;

    public EditTextPoint(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.grey8));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int radius = 2;

        final float densityMultiplier = getContext().getResources().getDisplayMetrics().density;
        final float scaledRadius = radius * densityMultiplier;

        canvas.drawCircle(getWidth() - scaledRadius, getHeight() - scaledRadius, scaledRadius, mPaint);
        super.onDraw(canvas);
    }
}

