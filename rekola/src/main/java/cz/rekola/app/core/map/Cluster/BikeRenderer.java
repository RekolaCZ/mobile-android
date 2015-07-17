package cz.rekola.app.core.map.Cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.HashSet;

import cz.rekola.app.R;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {26. 6. 2015}
 */
public class BikeRenderer extends DefaultClusterRenderer<BikeClusterItem> {
    public static final String TAG = BikeRenderer.class.getName();

    private Context mContext;
    private final View mBikeView;
    private final ImageView mImgBikeIcon;
    private final ImageView mImgBikeIconBackground;
    private final View mBikeClusterView;
    private final IconGenerator mDefaultIconGenerator; //is used before bike image is loaded
    private final IconGenerator mBikeIconGenerator;
    private final IconGenerator mClusterIconGenerator;
    private HashMap<BikeClusterItem, Marker> markerBikeItemMap = new HashMap<>();
    //Picasso use weak reference, so without reference it would be garbage collected
    private HashSet<PicassoMarker> mPicassoMarkersSet = new HashSet<>();

    public BikeRenderer(Context context, LayoutInflater layoutInflater, GoogleMap map,
                        ClusterManager<BikeClusterItem> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;

        mDefaultIconGenerator = new IconGenerator(mContext);
        mBikeIconGenerator = new IconGenerator(mContext);
        mClusterIconGenerator = new IconGenerator(mContext);

        mBikeView = layoutInflater.inflate(R.layout.map_cluster_bike, null);
        mBikeClusterView = layoutInflater.inflate(R.layout.map_cluster_bikes, null);
        mImgBikeIcon = (ImageView) mBikeView.findViewById(R.id.img_bike_icon);
        mImgBikeIconBackground = (ImageView) mBikeView.findViewById(R.id.img_bike_icon_background);

        mDefaultIconGenerator.setContentView(layoutInflater.inflate(R.layout.map_cluster_bike, null));
        mDefaultIconGenerator.setBackground(null);

        mBikeIconGenerator.setContentView(mBikeView);
        mBikeIconGenerator.setBackground(null);

        mClusterIconGenerator.setContentView(mBikeClusterView);
        mClusterIconGenerator.setBackground(null);
    }

    public void setIconSelected(BikeClusterItem bikeClusterItem) {
        if (!markerBikeItemMap.containsKey(bikeClusterItem))
            return;

        Marker marker = markerBikeItemMap.get(bikeClusterItem);
        marker.setIcon(getIconBikeIsSelected());
    }

    public void setIconUnselected(BikeClusterItem bikeClusterItem) {
        if (!markerBikeItemMap.containsKey(bikeClusterItem))
            return;

        Marker marker = markerBikeItemMap.get(bikeClusterItem);

        if (!bikeClusterItem.getBike().operational) //bike is broken
            marker.setIcon(getIconBikeIsBroken());
        else
            marker.setIcon(getIconBikeDefault());
    }

    // Draw a single bike.
    @Override
    protected void onBeforeClusterItemRendered(BikeClusterItem bikeClusterItem, MarkerOptions markerOptions) {

        if (bikeClusterItem.isSelectedBike())
            setOneBikeBackGround(R.drawable.map_bike_selected);
        else if (!bikeClusterItem.getBike().operational) //bike is broken
            setOneBikeBackGround(R.drawable.map_bike_broken);
        else
            setOneBikeBackGround(R.drawable.map_bike_default);

        Bitmap icon = mDefaultIconGenerator.makeIcon();
        String title = bikeClusterItem.getBike().name;
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(title);
    }

    private BitmapDescriptor getIconBikeIsSelected() {
        setOneBikeBackGround(R.drawable.map_bike_selected);
        return BitmapDescriptorFactory.fromBitmap(mBikeIconGenerator.makeIcon());
    }

    private BitmapDescriptor getIconBikeIsBroken() {
        setOneBikeBackGround(R.drawable.map_bike_broken);
        return BitmapDescriptorFactory.fromBitmap(mBikeIconGenerator.makeIcon());
    }

    private BitmapDescriptor getIconBikeDefault() {
        setOneBikeBackGround(R.drawable.map_bike_default);
        return BitmapDescriptorFactory.fromBitmap(mBikeIconGenerator.makeIcon());
    }

    @Override
    protected void onClusterItemRendered(BikeClusterItem bikeClusterItem, Marker marker) {
        super.onClusterItemRendered(bikeClusterItem, marker);
        markerBikeItemMap.put(bikeClusterItem, marker);

        String bikeType = bikeClusterItem.getBike().bikeType;
        String url;

        switch (bikeType) {
            case "scooter":
                url = "https://dl.dropboxusercontent.com/u/34660596/Ackee/Rekola/ic_scooter.png";
                break;
            case "lady":
                url = "https://dl.dropboxusercontent.com/u/34660596/Ackee/Rekola/ic_bike1.png";
                break;
            default:
                url = "https://dl.dropboxusercontent.com/u/34660596/Ackee/Rekola/ic_bike2.png";
        }

        PicassoMarker picassoMarker = new PicassoMarker(marker, mBikeIconGenerator, mImgBikeIcon);
        mPicassoMarkersSet.add(picassoMarker);
        Picasso.with(mContext).load(url).into(picassoMarker);

     /*
        Picasso.with(mContext).load(bikeClusterItem.getBike().iconUrl)
          .into(new PicassoMarker(marker, mBikeIconGenerator, mImgBikeIcon)); //TODO waiting for api*/
    }

    private void setOneBikeBackGround(int resId) {
        mImgBikeIconBackground.setImageResource(resId);
    }

    // Draw multiple bikes
    @Override
    protected void onBeforeClusterRendered(Cluster<BikeClusterItem> cluster, MarkerOptions markerOptions) {
        //mImageView.setImageResource(person.profilePhoto);

        String bikeCount = Integer.toString(cluster.getSize());
        Bitmap icon = mClusterIconGenerator.makeIcon(bikeCount);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(bikeCount);

        for (BikeClusterItem bikeClusterItem : cluster.getItems()) {
            markerBikeItemMap.remove(bikeClusterItem);
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
}
