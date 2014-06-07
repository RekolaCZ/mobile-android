package cz.rekola.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;

public class LoadingOverlay extends FrameLayout {

	@InjectView(R.id.loading_message)
	TextView vMessage;

	public LoadingOverlay(Context context) {
		super(context);
	}

	public LoadingOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoadingOverlay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this, this);
	}

	public void show() {
		vMessage.setText(getResources().getString(R.string.login_connecting));
		setVisibility(View.VISIBLE);
	}

	public void hide() {
		setVisibility(View.GONE);
	}

	public void setProgress() {
		vMessage.setText(getResources().getString(R.string.login_connecting_progress));
	}
}
