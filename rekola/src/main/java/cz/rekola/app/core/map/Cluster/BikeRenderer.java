package cz.rekola.app.core.map.Cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import cz.rekola.app.R;
import cz.rekola.app.core.Constants;

/**
 * Render for bike icons on Google map
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {26. 6. 2015}
 */
public class BikeRenderer extends DefaultClusterRenderer<BikeClusterItem> {
    public static final String TAG = BikeRenderer.class.getName();

    private Context mContext;
    private final ImageView mImgBikeIconDefault;
    private final ImageView mImgBikeIconSelected;
    private final ImageView mImgBikeIconBroken;

    //for optimalization have 3 different icon generator for one bike + 1 cluster icon generator
    private final IconGenerator mBikeDefaultIconGenerator; //is used before bike image is loaded
    private final IconGenerator mBikeSelectedIconGenerator;
    private final IconGenerator mBikeBrokenIconGenerator;
    private final IconGenerator mClusterIconGenerator;

    private HashMap<BikeClusterItem, Marker> markerBikeItemMap = new HashMap<>();
    //Picasso use weak reference, so without reference it would be garbage collected

    private float mZoomLevel;

    public BikeRenderer(Context context, LayoutInflater layoutInflater, GoogleMap map,
                        ClusterManager<BikeClusterItem> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;

        //set one bike
        mBikeDefaultIconGenerator = new IconGenerator(mContext);
        mBikeSelectedIconGenerator = new IconGenerator(mContext);
        mBikeBrokenIconGenerator = new IconGenerator(mContext);

        mImgBikeIconDefault = setIconGenerator(mBikeDefaultIconGenerator,
                layoutInflater, R.drawable.map_bike_default);

        mImgBikeIconSelected = setIconGenerator(mBikeSelectedIconGenerator,
                layoutInflater, R.drawable.map_bike_selected);

        mImgBikeIconBroken = setIconGenerator(mBikeBrokenIconGenerator,
                layoutInflater, R.drawable.map_bike_broken);

        //set cluster
        mClusterIconGenerator = new IconGenerator(mContext);
        View bikeClusterView = layoutInflater.inflate(R.layout.map_cluster_bikes, null);

        mClusterIconGenerator.setContentView(bikeClusterView);
        mClusterIconGenerator.setBackground(null);
    }

    /**
     * Initialize IconGenerator (correct view, background)
     *
     * @param iconGenerator      generator, which will be set up
     * @param layoutInflater     for inflate correct view
     * @param backgroundResource view background
     * @return pointer to bike icon, which will be change (is loaded via api)
     */
    private ImageView setIconGenerator(IconGenerator iconGenerator, LayoutInflater layoutInflater,
                                       int backgroundResource) {
        View bikeView = layoutInflater.inflate(R.layout.map_cluster_bike, null);
        ImageView imgBikeIconBackground = (ImageView) bikeView.findViewById(R.id.img_bike_icon_background);
        imgBikeIconBackground.setImageResource(backgroundResource);

        iconGenerator.setContentView(bikeView);
        iconGenerator.setBackground(null);

        return (ImageView) bikeView.findViewById(R.id.img_bike_icon);
    }

    public void setIconSelected(BikeClusterItem bikeClusterItem) {
        if (!markerBikeItemMap.containsKey(bikeClusterItem))
            return;

        Marker marker = markerBikeItemMap.get(bikeClusterItem);
        bikeClusterItem.setIsSelectedBike(true);
        loadIconFromApi(bikeClusterItem, marker);
    }

    public void setIconUnselected(BikeClusterItem bikeClusterItem) {
        if (!markerBikeItemMap.containsKey(bikeClusterItem))
            return;

        Marker marker = markerBikeItemMap.get(bikeClusterItem);
        bikeClusterItem.setIsSelectedBike(false);
        loadIconFromApi(bikeClusterItem, marker);
    }

    public void setZoomLevel(float zoomLevel) {
        this.mZoomLevel = zoomLevel;
    }

    // Draw a single bike.
    @Override
    protected void onBeforeClusterItemRendered(BikeClusterItem bikeClusterItem, MarkerOptions markerOptions) {
        IconGenerator iconGenerator = getCorrectedIconGenerator(bikeClusterItem);

        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    private IconGenerator getCorrectedIconGenerator(BikeClusterItem bikeClusterItem) {
        if (bikeClusterItem.isSelectedBike())
            return mBikeSelectedIconGenerator;
        else if (!bikeClusterItem.getBike().operational) //bike is broken
            return mBikeBrokenIconGenerator;
        else
            return mBikeDefaultIconGenerator;
    }

    private ImageView getCorrectedIconImage(BikeClusterItem bikeClusterItem) {
        if (bikeClusterItem.isSelectedBike())
            return mImgBikeIconSelected;
        else if (!bikeClusterItem.getBike().operational) //bike is broken
            return mImgBikeIconBroken;
        else
            return mImgBikeIconDefault;
    }


    @Override
    protected void onClusterItemRendered(BikeClusterItem bikeClusterItem, Marker marker) {
        super.onClusterItemRendered(bikeClusterItem, marker);
        markerBikeItemMap.put(bikeClusterItem, marker);
        loadIconFromApi(bikeClusterItem, marker);
    }

    private void loadIconFromApi(BikeClusterItem bikeClusterItem, Marker marker) {
        PicassoMarker picassoMarker = new PicassoMarker(marker,
                getCorrectedIconGenerator(bikeClusterItem),
                getCorrectedIconImage(bikeClusterItem));
        bikeClusterItem.setPicassoMarker(picassoMarker);

        Picasso.with(mContext)
                .load(bikeClusterItem.getBike().iconUrl)
                .into(picassoMarker);
    }

    // Draw multiple bikes
    @Override
    protected void onBeforeClusterRendered(Cluster<BikeClusterItem> cluster, MarkerOptions markerOptions) {
        //mImageView.setImageResource(person.profilePhoto);

        String bikeCount = Integer.toString(cluster.getSize());
        Bitmap icon = mClusterIconGenerator.makeIcon(bikeCount);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        for (BikeClusterItem bikeClusterItem : cluster.getItems()) {
            markerBikeItemMap.remove(bikeClusterItem);
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        Log.d("tom", "mZoomLevel " + mZoomLevel);
        if (mZoomLevel > Constants.MAX_CLUSTERING_ZOOM_LEVEL)
            return false;

        // Always render clusters.
        return cluster.getSize() > 1;
    }
}
