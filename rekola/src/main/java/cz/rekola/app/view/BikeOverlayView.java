package cz.rekola.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Bike;
import cz.rekola.app.core.anim.MyAnimator;

public class BikeOverlayView extends RelativeLayout {

    @InjectView(R.id.map_overlay_area)
    LinearLayout vOverlayArea;

    @InjectView(R.id.map_overlay_name)
    TextView vName;

    @InjectView(R.id.map_overlay_distance)
    TextView vDistance;

    @InjectView(R.id.map_overlay_inoperational)
    TextView vInoperational;

    @InjectView(R.id.map_overlay_operational_with_issues)
    TextView vOperationalWithIssues;

    @InjectView(R.id.map_overlay_note)
    TextView vNote;

    @InjectView(R.id.map_overlay_description)
    TextView vDescription;

    @InjectView(R.id.map_overlay_route)
    ImageView vRoute;

    @InjectView(R.id.map_overlay_center_map)
    ImageView vCenterMap;


    @InjectView(R.id.map_overlay_bike_detail)
    LinearLayout vBikeDetail;

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

        vCenterMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCenterMapPressed();
            }
        });
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        if (callbacks != null)
            callbacks.onHeightChanged(vOverlayArea.getMeasuredHeight());
    }

    public void init(BikeOverlayListener callbacks) {
        this.callbacks = callbacks;
    }

    public void show(Bike bike) {
        vName.setText(bike.name);
        vDistance.setText(bike.location.distance);
        vNote.setText(bike.location.note);
        vDescription.setText(bike.description);

        int visibility = bike.operational ? GONE : VISIBLE;
        vInoperational.setVisibility(visibility);


        //TODO set correct visibility
        //  if(bike.operational && bike.issues != null)
        vOperationalWithIssues.setVisibility(GONE);

        vOverlayArea.setVisibility(VISIBLE);
        vRoute.setVisibility(VISIBLE);

        MyAnimator.showSlideUp(this);
        callbacks.onHeightChanged(vOverlayArea.getMeasuredHeight());
    }

    public void hide() {
        vRoute.setVisibility(GONE);
        MyAnimator.hideSlideDown(vOverlayArea);
        callbacks.onHeightChanged(0);
    }

    public interface BikeOverlayListener {
        public void onClose();

        public void onRoutePressed();

        public void onBikeDetailPressed();

        public void onCenterMapPressed();

        public void onHeightChanged(int height);
    }
}
