package cz.rekola.app.fragment.natural;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.IssueUpdate;
import cz.rekola.app.core.adapter.BikeDetailAdapter;
import cz.rekola.app.core.model.BikeDetailItem;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Info about bike for user (there is info screen for serviceman)like name, problems, last returned, ...
 */
public class BikeDetailFragment extends BaseMainFragment {

    private static String ARG_BIKE_ID = "BIKE_ID";

    private int mBikeID;

    @InjectView(R.id.rvBikeDetail)
    RecyclerView rvBikeDetail;

    public static BikeDetailFragment newInstance(int bikeID) {
        BikeDetailFragment fragment = new BikeDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BIKE_ID, bikeID);
        fragment.setArguments(args);
        return fragment;
    }

    public BikeDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mBikeID = getArguments().getInt(ARG_BIKE_ID);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike_detail, container, false);
        ButterKnife.inject(this, view);

        RecyclerView rvBikeDetail = (RecyclerView) view.findViewById(R.id.rvBikeDetail);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvBikeDetail.setLayoutManager(layoutManager);

        BikeDetailAdapter adapter = new BikeDetailAdapter(getBikeDetailListItem());
        rvBikeDetail.setAdapter(adapter);
        return view;
    }

    private List<BikeDetailItem> getBikeDetailListItem() {
        List<BikeDetailItem> bikeDetailItemList = new ArrayList<>();
        BikeDetailItem basicInfo = BikeDetailItem.getBasicInfoInstance("bikeIconUrl",
                "bikeType", "bikeName", true, true, "description");
        BikeDetailItem separator = BikeDetailItem.getSeparatorInstance();
        BikeDetailItem recentlyReturned = BikeDetailItem.getRecentlyReturnedInstance(new Date(0),
                "recentPlaceDescription");

        Button.OnClickListener equipmentsDetailListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

        Button.OnClickListener addIssueListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPageController().requestAddIssue();
            }
        };

        BikeDetailItem equipments = BikeDetailItem.getEquipmentInstance(null, equipmentsDetailListener);
        BikeDetailItem issueHeader = BikeDetailItem.getIssueHeaderInstance(addIssueListener);
        BikeDetailItem issueTitle = BikeDetailItem.getIssueTitleInstance("issueTitle");

        IssueUpdate issueUpdate = new IssueUpdate();
        issueUpdate.author = "User Name";
        issueUpdate.description = "dsf dsf ds fds dsf dfs dfs fds fsd fsd f dsf ds f dsf dsfsdfds" +
                " dsfsdf dsfsd fds fsd f dsfds  dsfsdfsdf";
        issueUpdate.issuedAt = new Date(0);

        BikeDetailItem issueItem = BikeDetailItem.getIssueItemInstance(issueUpdate);

        bikeDetailItemList.add(basicInfo);
        bikeDetailItemList.add(separator);
        bikeDetailItemList.add(recentlyReturned);
        bikeDetailItemList.add(separator);
        bikeDetailItemList.add(equipments);
        bikeDetailItemList.add(separator);
        bikeDetailItemList.add(issueHeader);
        bikeDetailItemList.add(issueTitle);
        bikeDetailItemList.add(issueItem);
        bikeDetailItemList.add(issueItem);

        return bikeDetailItemList;
    }


}
