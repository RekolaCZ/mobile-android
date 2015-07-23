package cz.rekola.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Bike;

public class BikeOverlayView extends RelativeLayout {

    @InjectView(R.id.img_bike)
    ImageView mImgBike;
    @InjectView(R.id.txt_bike_name)
    TextView mTxtBikeName;
    @InjectView(R.id.txt_distance)
    TextView mTxtDistance;
    @InjectView(R.id.txt_inoperational)
    TextView mTxtInoperational;
    @InjectView(R.id.txt_operational_with_issues)
    TextView mTxtOperationalWithIssues;
    @InjectView(R.id.txt_note)
    TextView mTxtNote;
    @InjectView(R.id.txt_description)
    TextView mTxtDescription;
    @InjectView(R.id.layout_bike_detail)
    LinearLayout mLayoutBikeDetail;
    @InjectView(R.id.overlay_map_area)
    LinearLayout mOverlayMapArea;
    @InjectView(R.id.btn_route)
    ImageView mbtnRoute;
    @InjectView(R.id.btn_center_map)
    ImageView mbtnCenterMap;

    private BikeOverlayListener callbacks;

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

        mbtnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbacks.onRoutePressed();
            }
        });

        mLayoutBikeDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbacks.onBikeDetailPressed();
            }
        });

        mbtnCenterMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCenterMapPressed();
            }
        });
    }

    public void init(BikeOverlayListener callbacks) {
        this.callbacks = callbacks;
    }

    public void show(Bike bike) {
        mTxtBikeName.setText(bike.name);
        mTxtDistance.setText(bike.location.distance);
        mTxtNote.setText(bike.location.note);
        mTxtDescription.setText(bike.description);

        int inoperationalVisibility = bike.operational ? GONE : VISIBLE;
        mTxtInoperational.setVisibility(inoperationalVisibility);

        int operationalWithIssues = bike.operational && bike.issues.size() > 0 ? VISIBLE : GONE;
        mTxtOperationalWithIssues.setVisibility(operationalWithIssues);

        Glide.with(getContext()).load(bike.iconUrl).into(mImgBike);

        mOverlayMapArea.setVisibility(VISIBLE);
        mbtnRoute.setVisibility(VISIBLE);

        this.animate().translationY(0);
    }

    public void hide() {
        if (mOverlayMapArea.getVisibility() == GONE)
            return;

        this.animate().translationY(getHeightAnimation());
    }

    public interface BikeOverlayListener {
        public void onClose();

        public void onRoutePressed();

        public void onBikeDetailPressed();

        public void onCenterMapPressed();
    }

    /**
     * calculate correct height to show/hide this overlay
     *
     * @return height
     */
    private float getHeightAnimation() {
        return mOverlayMapArea.getMeasuredHeight()
                + mbtnRoute.getMeasuredHeight()
                + getResources().getDimension(R.dimen.bike_overlay_negative_margin);
    }
}
