package cz.rekola.app.core.map.Cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import cz.rekola.app.R;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {26. 6. 2015}
 */
public class BikeRenderer extends DefaultClusterRenderer<BikeClusterItem> {
    public static final String TAG = BikeRenderer.class.getName();

    private Context mContext;
    private final View mBikeView;
    private final View mBikeClusterView;
    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;

    public BikeRenderer(Context context, LayoutInflater layoutInflater, GoogleMap map,
                        ClusterManager<BikeClusterItem>
                                clusterManager) {
        super(context, map, clusterManager);
        mContext = context;

        mIconGenerator = new IconGenerator(mContext);
        mClusterIconGenerator = new IconGenerator(mContext);

        mBikeView = layoutInflater.inflate(R.layout.map_cluster_bike, null);
        mBikeClusterView = layoutInflater.inflate(R.layout.map_cluster_bikes, null);

        mIconGenerator.setContentView(mBikeView);
        mIconGenerator.setBackground(null);

        mClusterIconGenerator.setContentView(mBikeClusterView);
        mClusterIconGenerator.setBackground(null);
    }

    // Draw a single bike.
    @Override
    protected void onBeforeClusterItemRendered(BikeClusterItem bikeClusterItem, MarkerOptions markerOptions) {
        //mImageView.setImageResource(person.profilePhoto);
        Bitmap icon = mIconGenerator.makeIcon();
        String title = bikeClusterItem.getBike().name;
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(title);
    }

    // Draw multiple bikes
    @Override
    protected void onBeforeClusterRendered(Cluster<BikeClusterItem> cluster, MarkerOptions markerOptions) {
        //mImageView.setImageResource(person.profilePhoto);

        String bikeCount = Integer.toString(cluster.getSize());
        Bitmap icon = mClusterIconGenerator.makeIcon(bikeCount);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(bikeCount);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
}
