package cz.rekola.android.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.core.bus.ErrorMessageEvent;

public class ErrorBarView extends LinearLayout {

	private static final int MESSAGE_TIMEOUT = 5000;

	@InjectView(R.id.error_text)
	TextView message;

	private final Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			setVisibility(GONE);
		}
	};

	public ErrorBarView(Context context) {
		super(context);
	}

	public ErrorBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ErrorBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void hide() {
		setVisibility(GONE);
		handler.removeCallbacks(runnable);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this, this);

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setVisibility(GONE);
				handler.removeCallbacks(runnable);
			}
		});
	}

	@Subscribe
	public void onErrorMessage(ErrorMessageEvent event) {
		message.setText(event.message);
		setVisibility(VISIBLE);
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, MESSAGE_TIMEOUT);
	}
}

