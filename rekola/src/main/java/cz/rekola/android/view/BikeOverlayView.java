package cz.rekola.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.android.R;
import cz.rekola.android.api.model.Bike;
import cz.rekola.android.core.anim.MyAnimator;

public class BikeOverlayView extends LinearLayout {

	@InjectView(R.id.map_overlay_area)
	LinearLayout vOverlayArea;

	@InjectView(R.id.map_overlay_close)
	ImageView vClose;

	@InjectView(R.id.map_overlay_name)
	TextView vName;

	@InjectView(R.id.map_overlay_street)
	TextView vStreet;

	@InjectView(R.id.map_overlay_note)
	TextView vNote;

	@InjectView(R.id.map_overlay_description)
	TextView vDescription;

	@InjectView(R.id.map_overlay_route)
	ImageView vRoute;

	@InjectView(R.id.map_overlay_bike_detail)
	LinearLayout vBikeDetail;

	private BikeOverlayListener callbacks;
	private int height;

	public BikeOverlayView(Context context) {
		super(context);
	}

	public BikeOverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BikeOverlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this, this);

		vClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callbacks.onClose();
			}
		});

		vRoute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callbacks.onRoutePressed();
			}
		});

		vBikeDetail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callbacks.onBikeDetailPressed();
			}
		});
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		callbacks.onHeightChanged(vOverlayArea.getMeasuredHeight());
	}

	public void init(BikeOverlayListener callbacks) {
		this.callbacks = callbacks;
	}

	public void show(Bike bike) {
		vName.setText(bike.name + ", " + bike.location.distance);
		vStreet.setText(bike.location.address);
		vNote.setText(bike.location.note);
		vDescription.setText(bike.description);

		MyAnimator.showSlideUp(this);
		callbacks.onHeightChanged(vOverlayArea.getMeasuredHeight());
	}

	public void hide() {
		MyAnimator.hideSlideDown(this);
		callbacks.onHeightChanged(0);
	}

	public interface BikeOverlayListener {
		public void onClose();
		public void onRoutePressed();
		public void onBikeDetailPressed();
		public void onHeightChanged(int height);
	}
}
