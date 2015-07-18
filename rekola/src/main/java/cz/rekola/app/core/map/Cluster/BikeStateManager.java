package cz.rekola.app.core.map.Cluster;

import android.widget.ImageView;

import com.google.maps.android.ui.IconGenerator;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {18. 7. 2015}
 **/
public class BikeStateManager {
    public static final String TAG = BikeStateManager.class.getName();

    private final IconGenerator mBikeIconGenerator;
    private final ImageView mImgBikeIcon;

    public BikeStateManager(IconGenerator bikeIconGenerator, ImageView imgBikeIcon) {
        mBikeIconGenerator = bikeIconGenerator;
        mImgBikeIcon = imgBikeIcon;
    }

    public IconGenerator getBikeIconGenerator() {
        return mBikeIconGenerator;
    }

    public ImageView getImgBikeIcon() {
        return mImgBikeIcon;
    }
}
