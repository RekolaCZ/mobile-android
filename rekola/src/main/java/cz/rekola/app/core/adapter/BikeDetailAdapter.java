package cz.rekola.app.core.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import cz.rekola.app.R;
import cz.rekola.app.Utils.DateUtils;
import cz.rekola.app.api.model.bike.IssueUpdate;
import cz.rekola.app.core.model.BikeDetailItem;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {24. 6. 2015}
 */
public class BikeDetailAdapter extends RecyclerView.Adapter<BikeDetailAdapter.ViewHolder> {
    public static final String TAG = BikeDetailAdapter.class.getName();

    private final List<BikeDetailItem> mData;

    public BikeDetailAdapter(List<BikeDetailItem> data) {
        mData = data;
    }

    @Override
    public BikeDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeView) {
        View view = null;

        switch (typeView) {
            case BikeDetailItem.TYPE_SEPARATOR:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                                .bike_detail_separator, viewGroup,
                        false);
                break;
            case BikeDetailItem.TYPE_BASIC_INFO:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bike_detail_basic_info, viewGroup,
                        false);
                break;
            case BikeDetailItem.TYPE_RECENTLY_RETURNED:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bike_detail_recently_returned, viewGroup,
                        false);
                break;
            case BikeDetailItem.TYPE_EQUIPMENT:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bike_detail_equipment, viewGroup,
                        false);
                break;
            case BikeDetailItem.TYPE_ISSUE_HEADER:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bike_detail_issues_header, viewGroup,
                        false);
                break;
            case BikeDetailItem.TYPE_ISSUE_TITLE:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bike_detail_issues_title, viewGroup,
                        false);
                break;
            case BikeDetailItem.TYPE_ISSUE_ITEM:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bike_detail_issues_item, viewGroup,
                        false);
                break;
            default:
                Log.e(TAG, "unknown typeView " + typeView);
        }

        return new ViewHolder(view, typeView);
    }

    @Override
    public void onBindViewHolder(BikeDetailAdapter.ViewHolder viewHolder, final int position) {
        switch (viewHolder.typeView) {
            case BikeDetailItem.TYPE_SEPARATOR:
                break;
            case BikeDetailItem.TYPE_BASIC_INFO:
                onBindBasicInfo(viewHolder, position);
                break;
            case BikeDetailItem.TYPE_RECENTLY_RETURNED:
                onBindRecentlyReturned(viewHolder, position);
                break;
            case BikeDetailItem.TYPE_EQUIPMENT:
                onBindEquipment(viewHolder, position);
                break;
            case BikeDetailItem.TYPE_ISSUE_HEADER:
                onBindIssueHeader(viewHolder, position);
                break;
            case BikeDetailItem.TYPE_ISSUE_TITLE:
                onBindIssueTitle(viewHolder, position);
                break;
            case BikeDetailItem.TYPE_ISSUE_ITEM:
                onBindIssueItem(viewHolder, position);
                break;
            default:
                Log.e(TAG, "unknown typeView " + viewHolder.typeView);
        }
    }

    private void onBindBasicInfo(BikeDetailAdapter.ViewHolder viewHolder, final int position) {
        //TODO load image into ivBikeIcon
        //TODO hide or show icons operationalWithIssues, inOperational
        viewHolder.mTxtBikeType.setText(mData.get(position).getBikeType());
        viewHolder.mTxtBikeName.setText(mData.get(position).getBikeName());
        viewHolder.mTxtBikeDescription.setText(mData.get(position).getBikeDescription());
    }

    private void onBindRecentlyReturned(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        Date recentlyReturnedDate = mData.get(position).getRecentlyReturned();

        viewHolder.mTxtRecReturnedDate.setText(DateUtils.getDate(recentlyReturnedDate));
        viewHolder.mTxtRecReturnedTime.setText(DateUtils.getTime(recentlyReturnedDate));
        viewHolder.mTxtRecReturnedDescription.setText(mData.get(position).getRecentPlaceDescription());
    }

    private void onBindEquipment(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
//TODO add equipment
        viewHolder.mBtnEquipmentsDetail.setOnClickListener(mData.get(position)
                .getEquipmentsDetailListener());
    }

    private void onBindIssueHeader(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        viewHolder.mBtnAddIssue.setOnClickListener(mData.get(position).getAddIssueListener());
    }

    private void onBindIssueTitle(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        viewHolder.mTxtIssueTitle.setText(mData.get(position).getIssueTitle());
    }

    private void onBindIssueItem(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        IssueUpdate issueUpdate = mData.get(position).getIssueUpdate();

        viewHolder.mTxtUserName.setText(issueUpdate.author);
        viewHolder.mTxtIssueDescription.setText(issueUpdate.description);

        Date issueAtDate = issueUpdate.issuedAt;

        viewHolder.mTxtIssueDate.setText(DateUtils.getDate(issueAtDate));
        viewHolder.mTxtIssueTime.setText(DateUtils.getTime(issueAtDate));
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //TYPE_BASIC_INFO
        ImageView mImgBike;
        TextView mTxtBikeType;
        TextView mTxtBikeName;
        TextView mTxtBikeDescription;

        //TYPE_RECENTLY_RETURNED
        TextView mTxtRecReturnedDate;
        TextView mTxtRecReturnedTime;
        TextView mTxtRecReturnedDescription;

        //TYPE_EQUIPMENT
        LinearLayout mLlEquipments;
        Button mBtnEquipmentsDetail;

        //TYPE_ISSUE_HEADER
        Button mBtnAddIssue;

        //TYPE_ISSUE_TITLE
        TextView mTxtIssueTitle;

        //TYPE_ISSUE_ITEM
        TextView mTxtUserName;
        TextView mTxtIssueDate;
        TextView mTxtIssueTime;
        TextView mTxtIssueDescription;

        public final int typeView;

        public ViewHolder(View itemView, int typeView) {
            super(itemView);

            this.typeView = typeView;

            switch (typeView) {
                case BikeDetailItem.TYPE_SEPARATOR:
                    break;
                case BikeDetailItem.TYPE_BASIC_INFO:
                    mImgBike = (ImageView) itemView.findViewById(R.id.img_bike);
                    mTxtBikeType = (TextView) itemView.findViewById(R.id.txt_bike_type);
                    mTxtBikeName = (TextView) itemView.findViewById(R.id.txt_bike_name);
                    mTxtBikeDescription = (TextView) itemView.findViewById(R.id.txt_bike_description);
                    break;
                case BikeDetailItem.TYPE_RECENTLY_RETURNED:
                    mTxtRecReturnedDate = (TextView) itemView.findViewById(R.id.txt_rec_returned_date);
                    mTxtRecReturnedTime = (TextView) itemView.findViewById(R.id.txt_rec_returned_time);
                    mTxtRecReturnedDescription = (TextView) itemView.findViewById(R.id.txt_rec_returned_description);
                    break;
                case BikeDetailItem.TYPE_EQUIPMENT:
                    mLlEquipments = (LinearLayout) itemView.findViewById(R.id.ll_equipments);
                    mBtnEquipmentsDetail = (Button) itemView.findViewById(R.id.btn_equipments_detail);
                    break;
                case BikeDetailItem.TYPE_ISSUE_HEADER:
                    mBtnAddIssue = (Button) itemView.findViewById(R.id.btn_add_issue);
                    break;
                case BikeDetailItem.TYPE_ISSUE_TITLE:
                    mTxtIssueTitle = (TextView) itemView.findViewById(R.id.txt_issue_title);
                    break;
                case BikeDetailItem.TYPE_ISSUE_ITEM:
                    mTxtUserName = (TextView) itemView.findViewById(R.id.txt_user_name);
                    mTxtIssueDate = (TextView) itemView.findViewById(R.id.txt_issue_date);
                    mTxtIssueTime = (TextView) itemView.findViewById(R.id.txt_issue_time);
                    mTxtIssueDescription = (TextView) itemView.findViewById(R.id.txt_issue_description);
                    break;
                default:
                    Log.e(TAG, "unknown typeView " + typeView);
            }
        }
    }
}
