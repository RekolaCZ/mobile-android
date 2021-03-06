package cz.rekola.app.fragment.natural;


import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Bike;
import cz.rekola.app.api.model.bike.Equipment;
import cz.rekola.app.api.model.bike.Issue;
import cz.rekola.app.api.model.bike.IssueUpdate;
import cz.rekola.app.core.adapter.BikeDetailAdapter;
import cz.rekola.app.core.adapter.EquipmentAdapter;
import cz.rekola.app.core.bus.dataAvailable.BikeIssuesAvailableEvent;
import cz.rekola.app.core.bus.dataAvailable.BikesAvailableEvent;
import cz.rekola.app.core.model.BikeDetailItem;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Info about bike for user (there is info screen for serviceman) like name, problems, last returned, ...
 */
public class BikeDetailFragment extends BaseMainFragment {
    public static final String TAG = BikeDetailAdapter.class.getName();

    private int mBikeID = Bike.UNDEFINED_BIKE;
    private List<BikeDetailItem> mBikeDetailItemList;
    private int mOverallYScroll = 0; //to change toolbar visibility according to scrolling position
    int mColorPrimaryDark;
    int mColorGrey;

    @InjectView(R.id.rvBikeDetail)
    RecyclerView mRvBikeDetail;
    @InjectView(R.id.bike_detail_toolbar_green)
    FrameLayout mBikeDetailToolbarGreen;
    @InjectView(R.id.txt_bike_name_toolbar)
    TextView mTxtBikeNameToolbar;

    public void init(int bikeID) {
        if (mBikeID != bikeID && mBikeID != Bike.UNDEFINED_BIKE) {
            mBikeID = bikeID;
            mOverallYScroll = 0;
            getApp().getDataManager().getBikeIssues(mBikeID, true); //forced update
            fillData();
        }

        mBikeID = bikeID;
    }

    public BikeDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike_detail, container, false);

        mColorPrimaryDark = getAct().getResources().getColor(R.color.colorPrimaryDark);
        mColorGrey = getAct().getResources().getColor(R.color.grey_transparent);

        ButterKnife.inject(this, view);

        mRvBikeDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mOverallYScroll = mOverallYScroll + dy;

                float transparentRatio = (float) mOverallYScroll / mBikeDetailToolbarGreen
                        .getHeight();

                if (transparentRatio > 1) {
                    transparentRatio = 1;
                }

                mBikeDetailToolbarGreen.setAlpha(transparentRatio);
                setStatusBar(transparentRatio);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvBikeDetail.setLayoutManager(layoutManager);

        mBikeDetailItemList = new ArrayList<>();

        BikeDetailAdapter adapter = new BikeDetailAdapter(mBikeDetailItemList, getActivity());
        mRvBikeDetail.setAdapter(adapter);
        fillData();

        return view;
    }

    @Subscribe
    public void isBikeDetailAvailable(BikesAvailableEvent event) {
        fillData();
    }

    @Subscribe
    public void isBikeIssuesAvailable(BikeIssuesAvailableEvent event) {
        fillData();
    }

    private void fillData() {
        if (mBikeID == Bike.UNDEFINED_BIKE) {
            Log.e(TAG, "bike was not defined");
            return;
        }

        mBikeDetailItemList.clear();

        Bike bike = getApp().getDataManager().getBike(mBikeID);
        if (bike == null) {
            return;  // will be set later by event isBikeDetailAvailable
        }

        List<Issue> bikeIssues = getApp().getDataManager().getBikeIssues(mBikeID, false);
        if (bikeIssues == null) {
            return; // will be set later by event isBikeIssuesAvailable
        }


        mTxtBikeNameToolbar.setText(bike.name);

        setBikeInfo(bike);
        setBikeIssues(Issue.getGroupedIssues(bikeIssues));
    }

    private void setBikeInfo(Bike bike) {

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

        mRvBikeDetail.getAdapter().notifyDataSetChanged();
    }


    BikeDetailItem getBasicInfoItem(Bike bike, boolean operationalWithIssues) {
        return BikeDetailItem.getBasicInfoInstance(bike.imageUrl, bike.bikeType, bike.name, operationalWithIssues,
                bike.operational, bike.description);
    }

    BikeDetailItem getRecentlyReturnedItem(Bike bike) {
        return BikeDetailItem.getRecentlyReturnedInstance(bike.location.returnedAt, bike.location.note);
    }

    BikeDetailItem getEquipmentsItem(final Bike bike) {
        Button.OnClickListener equipmentsDetailListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEquipmentsDetail(bike.equipment);
            }
        };

        return BikeDetailItem.getEquipmentInstance(bike.equipment, equipmentsDetailListener);
    }

    BikeDetailItem getIssueHeaderItem(final Bike bike) {
        Button.OnClickListener addIssueListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPageController().requestAddIssue(bike.id, true);
            }
        };

        boolean hasIssues = bike.issues.size() > 0;
        return BikeDetailItem.getIssueHeaderInstance(addIssueListener, hasIssues);
    }

    private void setBikeIssues(List<Issue> bikeIssues) {
        for (Issue issue : bikeIssues) {
            BikeDetailItem issueTitle = BikeDetailItem.getIssueTitleInstance(issue.title);
            mBikeDetailItemList.add(issueTitle);
            setBikeIssueUpdates(issue.updates);
        }

        mRvBikeDetail.getAdapter().notifyDataSetChanged();
    }

    private void setBikeIssueUpdates(List<IssueUpdate> bikeIssueUpdates) {
        for (IssueUpdate issueUpdate : bikeIssueUpdates) {
            BikeDetailItem issueItem = BikeDetailItem.getIssueItemInstance(issueUpdate);
            mBikeDetailItemList.add(issueItem);
        }
    }

    private void showEquipmentsDetail(List<Equipment> equipmentList) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_equipments, null);

        ListView lvEquipments = (ListView) dialogLayout.findViewById(R.id.lv_equipments);
        lvEquipments.setAdapter(new EquipmentAdapter(getActivity(), equipmentList));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogLayout);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.show();
        alertDialog.setCanceledOnTouchOutside(true);

        lvEquipments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alertDialog.dismiss();
            }
        });

        dialogLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void setStatusBar(float transparentRatio) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        Window window = getAct().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        int from = mColorGrey;
        int to = mColorPrimaryDark;

        window.setStatusBarColor(blendColors(from, to, transparentRatio));
    }

    private int blendColors(int from, int to, float transparentRatio) {
        final float inverseRatio = 1f - transparentRatio;
        final float a = Color.alpha(to) * transparentRatio + Color.alpha(from) * inverseRatio;
        final float r = Color.red(to) * transparentRatio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * transparentRatio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * transparentRatio + Color.blue(from) * inverseRatio;

        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }
}
