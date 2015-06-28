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
        viewHolder.tvBikeType.setText(mData.get(position).getBikeType());
        viewHolder.tvBikeName.setText(mData.get(position).getBikeName());
        viewHolder.tvBikeDescription.setText(mData.get(position).getBikeDescription());
    }

    private void onBindRecentlyReturned(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        Date date = mData.get(position).getRecentlyReturned();

        viewHolder.tvRecReturnedDate.setText(DateUtils.getDate(date));
        viewHolder.tvRecReturnedTime.setText(DateUtils.getTime(date));
        viewHolder.tvRecReturnedDescription.setText(mData.get(position).getRecentPlaceDescription());
    }

    private void onBindEquipment(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
//TODO add equipment
        viewHolder.btn_equipments_detail.setOnClickListener(mData.get(position)
                .getEquipmentsDetailListener());
    }

    private void onBindIssueHeader(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        viewHolder.btn_add_issue.setOnClickListener(mData.get(position).getAddIssueListener());
    }

    private void onBindIssueTitle(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        viewHolder.tvIssueTitle.setText(mData.get(position).getIssueTitle());
    }

    private void onBindIssueItem(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        IssueUpdate issueUpdate = mData.get(position).getIssueUpdate();

        viewHolder.tvUserName.setText(issueUpdate.author);
        viewHolder.tvIssueDescription.setText(issueUpdate.description);

        Date date = issueUpdate.issuedAt;

        viewHolder.tvIssueDate.setText(DateUtils.getDate(date));
        viewHolder.tvIssueTime.setText(DateUtils.getTime(date));
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

        ImageView ivBikeIcon;
        TextView tvBikeType;
        TextView tvBikeName;
        TextView tvBikeDescription;

        TextView tvRecReturnedDate;
        TextView tvRecReturnedTime;
        TextView tvRecReturnedDescription;

        LinearLayout llEquipments;
        Button btn_equipments_detail;

        Button btn_add_issue;

        TextView tvIssueTitle;

        TextView tvUserName;
        TextView tvIssueDate;
        TextView tvIssueTime;
        TextView tvIssueDescription;

        public final int typeView;

        public ViewHolder(View itemView, int typeView) {
            super(itemView);

            this.typeView = typeView;
            ivBikeIcon = (ImageView) itemView.findViewById(R.id.ivBikeIcon);
            tvBikeType = (TextView) itemView.findViewById(R.id.tvBikeType);
            tvBikeName = (TextView) itemView.findViewById(R.id.tvBikeName);
            tvBikeDescription = (TextView) itemView.findViewById(R.id.tvBikeDescription);

            tvRecReturnedDate = (TextView) itemView.findViewById(R.id.tvRecReturnedDate);
            tvRecReturnedTime = (TextView) itemView.findViewById(R.id.tvRecReturnedTime);
            tvRecReturnedDescription = (TextView) itemView.findViewById(R.id.tvRecReturnedDescription);

            llEquipments = (LinearLayout) itemView.findViewById(R.id.llEquipments);
            btn_equipments_detail = (Button) itemView.findViewById(R.id.btn_equipments_detail);

            btn_add_issue = (Button) itemView.findViewById(R.id.btn_add_issue);

            tvIssueTitle = (TextView) itemView.findViewById(R.id.tvIssueTitle);

            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvIssueDate = (TextView) itemView.findViewById(R.id.tvIssueDate);
            tvIssueTime = (TextView) itemView.findViewById(R.id.tvIssueTime);
            tvIssueDescription = (TextView) itemView.findViewById(R.id.tvIssueDescription);
        }

    }
}
