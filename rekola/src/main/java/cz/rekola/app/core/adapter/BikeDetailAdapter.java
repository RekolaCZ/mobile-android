package cz.rekola.app.core.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Equipment;
import cz.rekola.app.api.model.bike.IssueUpdate;
import cz.rekola.app.core.Constants;
import cz.rekola.app.core.model.BikeDetailItem;
import cz.rekola.app.utils.DateUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Adapter for recycleview in BikeDetailFragment
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {24. 6. 2015}
 */
public class BikeDetailAdapter extends RecyclerView.Adapter<BikeDetailAdapter.ViewHolder> {
    public static final String TAG = BikeDetailAdapter.class.getName();

    private final List<BikeDetailItem> mData;
    private final Context mContext;

    public BikeDetailAdapter(List<BikeDetailItem> data, Context context) {
        mData = data;
        mContext = context;
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
        BikeDetailItem bikeDetailItem = mData.get(position);
        Glide.with(mContext).load(bikeDetailItem.getBikeIconUrl()).into(viewHolder.mImgBike);

        int operationalWithProblemsVisibility = bikeDetailItem.isOperationalWithIssues() ? View.VISIBLE : View.GONE;
        int inoperationalVisibility = bikeDetailItem.isInOperational() ? View.VISIBLE : View.GONE;
        viewHolder.mTxtOperationalWithProblems.setVisibility(operationalWithProblemsVisibility);
        viewHolder.mTxtInoperational.setVisibility(inoperationalVisibility);

        viewHolder.mTxtBikeType.setText(bikeDetailItem.getBikeType());
        viewHolder.mTxtBikeName.setText(bikeDetailItem.getBikeName());
        viewHolder.mTxtBikeDescription.setText(bikeDetailItem.getBikeDescription());
    }

    private void onBindRecentlyReturned(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {

        String recentPlaceDescription = mData.get(position).getRecentPlaceDescription();

        if (recentPlaceDescription.trim().equals("")) {
            viewHolder.mTxtRecReturnedDescription.setVisibility(View.GONE);
        } else {
            viewHolder.mTxtRecReturnedDescription.setVisibility(View.VISIBLE);
            viewHolder.mTxtRecReturnedDescription.setText(recentPlaceDescription);
        }
    }

    private void onBindEquipment(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {

        viewHolder.mLlEquipments.removeAllViews();

        BikeDetailItem bikeDetailItem = mData.get(position);

        int count = 0;
        for (Equipment equipment : bikeDetailItem.getEquipments()) {
            if (count == Constants.MAX_COUNT_OF_VISIBLE_EQUIPMENTS) {
                addLastEquipmentIcon(viewHolder.mLlEquipments);
                break;
            }

            addEquipmentIcon(viewHolder.mLlEquipments, equipment.iconUrl);
            count++;
        }

        viewHolder.mBtnEquipmentsDetail.setOnClickListener(mData.get(position)
                .getEquipmentsDetailListener());
    }

    private void addEquipmentIcon(LinearLayout mLlEquipments, String iconUrl) {
        View flEquipmentIcon = LayoutInflater.from(mContext)
                .inflate(R.layout.bike_detail_equipment_icon, mLlEquipments, false);

        ImageView imgEquipment = (ImageView) flEquipmentIcon
                .findViewById(R.id.img_equipment_icon);

        Glide.with(mContext).load(iconUrl).fitCenter().into(imgEquipment);

        mLlEquipments.addView(flEquipmentIcon);

    }

    private void addLastEquipmentIcon(LinearLayout mLlEquipments) {

        View flEquipmentIcon = LayoutInflater.from(mContext).
                inflate(R.layout.bike_detail_equipment_icon_more, mLlEquipments, false);
        mLlEquipments.addView(flEquipmentIcon);

    }


    private void onBindIssueHeader(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        BikeDetailItem bikeDetailItem = mData.get(position);
        viewHolder.mBtnAddIssue.setOnClickListener(bikeDetailItem.getAddIssueListener());

        if (bikeDetailItem.hasIssues()) {
            viewHolder.mTxtNoIssues.setVisibility(View.GONE);
        } else {
            viewHolder.mTxtNoIssues.setVisibility(View.VISIBLE);
        }
    }

    private void onBindIssueTitle(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {
        viewHolder.mTxtIssueTitle.setText(mData.get(position).getIssueTitle());
    }

    private void onBindIssueItem(BikeDetailAdapter.ViewHolder viewHolder, final int
            position) {

        IssueUpdate issueUpdate = mData.get(position).getIssueUpdate();

        int colorBasePink = mContext.getResources().getColor(R.color.base_pink);
        String slash = " / ";

        SpannableStringBuilder userNameAndDateTime = new SpannableStringBuilder();
        int start = userNameAndDateTime.length();

        //set author name
        userNameAndDateTime.append(issueUpdate.author);


        String fontRobotoMedium = mContext.getResources().getString(R.string.font_roboto_medium);
        String fontRobotoRegular = mContext.getResources().getString(R.string.font_roboto_regular);

        CalligraphyTypefaceSpan typefaceSpanMedium = new CalligraphyTypefaceSpan(TypefaceUtils
                .load(mContext.getAssets(), fontRobotoMedium));

        CalligraphyTypefaceSpan typefaceSpanRegular = new CalligraphyTypefaceSpan(TypefaceUtils
                .load(mContext.getAssets(), fontRobotoRegular));


        userNameAndDateTime.setSpan(typefaceSpanMedium, start,
                userNameAndDateTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // set " / "
        start = userNameAndDateTime.length();
        userNameAndDateTime.append(slash);

        userNameAndDateTime.setSpan(new ForegroundColorSpan(colorBasePink), start,
                userNameAndDateTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        userNameAndDateTime.setSpan(typefaceSpanRegular, start, userNameAndDateTime.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        //set issue date
        userNameAndDateTime.append(DateUtils.getDate(issueUpdate.issuedAt));

        // set " / "
        start = userNameAndDateTime.length();
        userNameAndDateTime.append(slash);
        userNameAndDateTime.setSpan(new ForegroundColorSpan(colorBasePink), start, userNameAndDateTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // set issue time
        userNameAndDateTime.append(DateUtils.getTime(issueUpdate.issuedAt));

        viewHolder.mTxtUserNameAndDateTime.setText(userNameAndDateTime);
        viewHolder.mTxtIssueDescription.setText(issueUpdate.description);
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
        TextView mTxtOperationalWithProblems;
        TextView mTxtInoperational;

        //TYPE_RECENTLY_RETURNED
        TextView mTxtRecReturnedDescription;

        //TYPE_EQUIPMENT
        LinearLayout mLlEquipments;
        Button mBtnEquipmentsDetail;

        //TYPE_ISSUE_HEADER
        Button mBtnAddIssue;
        TextView mTxtNoIssues;

        //TYPE_ISSUE_TITLE
        TextView mTxtIssueTitle;

        //TYPE_ISSUE_ITEM
        TextView mTxtUserNameAndDateTime;
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
                    mTxtOperationalWithProblems = (TextView) itemView.findViewById(R.id.txt_operational_with_problems);
                    mTxtInoperational = (TextView) itemView.findViewById(R.id.txt_inoperational);

                    break;
                case BikeDetailItem.TYPE_RECENTLY_RETURNED:
                    mTxtRecReturnedDescription = (TextView) itemView.findViewById(R.id.txt_rec_returned_description);
                    break;
                case BikeDetailItem.TYPE_EQUIPMENT:
                    mLlEquipments = (LinearLayout) itemView.findViewById(R.id.ll_equipments);
                    mBtnEquipmentsDetail = (Button) itemView.findViewById(R.id.btn_equipments_detail);
                    break;
                case BikeDetailItem.TYPE_ISSUE_HEADER:
                    mBtnAddIssue = (Button) itemView.findViewById(R.id.btn_add_issue);
                    mTxtNoIssues = (TextView) itemView.findViewById(R.id.txt_no_issue);
                    break;
                case BikeDetailItem.TYPE_ISSUE_TITLE:
                    mTxtIssueTitle = (TextView) itemView.findViewById(R.id.txt_issue_title);
                    break;
                case BikeDetailItem.TYPE_ISSUE_ITEM:
                    mTxtUserNameAndDateTime = (TextView) itemView.findViewById(R.id.txt_user_name_and_datetime);
                    mTxtIssueDescription = (TextView) itemView.findViewById(R.id.txt_issue_description);
                    break;
                default:
                    Log.e(TAG, "unknown typeView " + typeView);
            }
        }
    }
}
