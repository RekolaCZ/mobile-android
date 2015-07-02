package cz.rekola.app.fragment.natural;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Bike;
import cz.rekola.app.api.model.bike.Issue;
import cz.rekola.app.api.model.bike.IssueUpdate;
import cz.rekola.app.core.adapter.BikeDetailAdapter;
import cz.rekola.app.core.bus.dataAvailable.BikeIssuesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.BikesAvailableEvent;
import cz.rekola.app.core.model.BikeDetailItem;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Info about bike for user (there is info screen for serviceman)like name, problems, last returned, ...f
 */
public class BikeDetailFragment extends BaseMainFragment {
    public static final String TAG = BikeDetailAdapter.class.getName();

    private static String ARG_BIKE_ID = "BIKE_ID";
    private final int UNDEFINED_BIKE = -1;
    private int mBikeID = UNDEFINED_BIKE;
    List<BikeDetailItem> mBikeDetailItemList;

    @InjectView(R.id.rvBikeDetail)
    RecyclerView rvBikeDetail;


    public void init(int bikeID) {
        mBikeID = bikeID;
    }

    public BikeDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike_detail, container, false);
        ButterKnife.inject(this, view);

        if (savedInstanceState != null) {
            mBikeID = savedInstanceState.getInt(ARG_BIKE_ID);
        }

        RecyclerView rvBikeDetail = (RecyclerView) view.findViewById(R.id.rvBikeDetail);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvBikeDetail.setLayoutManager(layoutManager);

        mBikeDetailItemList = new ArrayList<>();

        BikeDetailAdapter adapter = new BikeDetailAdapter(mBikeDetailItemList, getActivity());
        rvBikeDetail.setAdapter(adapter);

        setBikeInfo();
        setBikeIssues();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_BIKE_ID, mBikeID);
    }


    @Subscribe
    public void isBikeDetailAvailable(BikesAvailableEvent event) {
        setBikeInfo();
    }

    @Subscribe
    public void isBikeIssuesAvailable(BikeIssuesAvailableEvent event) {
        setBikeIssues();
    }

    private void setBikeInfo() {
        if (mBikeID == UNDEFINED_BIKE) {
            Log.e(TAG, "bike was not defined");
            return;
        }

        Bike bike = getApp().getDataManager().getBike(mBikeID);
        if (bike == null)
            return; // will be set later by event isBikeDetailAvailable


        boolean operationalWithIssues = bike.operational && bike.issues.size() > 0;
        BikeDetailItem basicInfo = getBasicInfoItem(bike, operationalWithIssues);
        BikeDetailItem separator = BikeDetailItem.getSeparatorInstance();
        BikeDetailItem recentlyReturned = getRecentlyReturnedItem(bike);

        BikeDetailItem equipments = getEquipmentsItem(bike);
        BikeDetailItem issueHeader = getIssueHeaderItem(bike);

        mBikeDetailItemList.add(basicInfo);
        mBikeDetailItemList.add(separator);
        mBikeDetailItemList.add(recentlyReturned);
        mBikeDetailItemList.add(separator);
        mBikeDetailItemList.add(equipments);
        mBikeDetailItemList.add(separator);
        mBikeDetailItemList.add(issueHeader);

        rvBikeDetail.getAdapter().notifyDataSetChanged();
    }


    BikeDetailItem getBasicInfoItem(Bike bike, boolean operationalWithIssues) {
        return BikeDetailItem.getBasicInfoInstance(bike.iconUrl, bike.bikeType, bike.name, operationalWithIssues,
                bike.operational, bike.description);
    }

    BikeDetailItem getRecentlyReturnedItem(Bike bike) {
        return BikeDetailItem.getRecentlyReturnedInstance(new Date(0), //TODO replace with bike.date
                bike.location.note);
    }

    BikeDetailItem getEquipmentsItem(Bike bike) {
        Button.OnClickListener equipmentsDetailListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add dialog fragment
            }
        };

        return BikeDetailItem.getEquipmentInstance(bike.equipment, equipmentsDetailListener);
    }

    BikeDetailItem getIssueHeaderItem(Bike bike) {
        Button.OnClickListener addIssueListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO solve problem with back state
                //getPageController().requestAddIssue();
            }
        };

        return BikeDetailItem.getIssueHeaderInstance(addIssueListener);
    }

    private void setBikeIssues() {
        List<Issue> bikeIssues = getApp().getDataManager().getBikeIssues(mBikeID);
        if (bikeIssues == null)
            return;

        for (Issue issue : bikeIssues) {
            BikeDetailItem issueTitle = BikeDetailItem.getIssueTitleInstance(issue.title);
            mBikeDetailItemList.add(issueTitle);
            setBikeIssueUpdates(issue.updates);
        }

        rvBikeDetail.getAdapter().notifyDataSetChanged();
    }

    private void setBikeIssueUpdates(List<IssueUpdate> bikeIssueUpdates) {
        for (IssueUpdate issueUpdate : bikeIssueUpdates) {
            BikeDetailItem issueItem = BikeDetailItem.getIssueItemInstance(issueUpdate);
            mBikeDetailItemList.add(issueItem);
        }
    }

}
