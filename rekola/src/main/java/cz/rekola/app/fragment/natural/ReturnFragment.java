package cz.rekola.app.fragment.natural;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.core.bus.LockCodeEvent;
import cz.rekola.app.core.bus.dataAvailable.BorrowedBikeAvailableEvent;
import cz.rekola.app.core.data.MyBikeWrapper;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Screen to return bike
 */

public class ReturnFragment extends BaseMainFragment {

    @InjectView(R.id.img_bike)
    ImageView mImgBike;
    @InjectView(R.id.txt_bike_name)
    TextView mTxtBikeName;
    @InjectView(R.id.txt_borrowed_from_date)
    TextView mTxtBorrowedFromDate;
    @InjectView(R.id.txt_borrowed_from_time)
    TextView mTxtBorrowedFromTime;
    @InjectView(R.id.txt_lock_code)
    TextView mTxtLockCode;

    private int bikeId = -1; // Invalid bike id
    private boolean hasIssues = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        if (!getApp().getDataManager().isOperational())
            return;

        populateData();
    }

    @OnClick(R.id.btn_return_bike)
    public void returnBikeOnClick() {
        getPageController().requestReturnMap();
    }

    @OnClick(R.id.btn_bike_detail)
    public void bikeDetailOnClick() {
        getPageController().requestBikeDetail(bikeId, hasIssues);
    }

    @Subscribe
    public void bikeBorrowed(LockCodeEvent event) {
        populateData();
    }

    @Subscribe
    public void isBorrowedBikeAvailable(BorrowedBikeAvailableEvent event) {
        populateData();
    }

    private void populateData() {
        MyBikeWrapper myBike = getApp().getDataManager().getBorrowedBike(false);
        if (myBike == null || !myBike.isBorrowed()) {
            setData(getResources().getString(R.string.error_no_bike_borrowed), "");
            return;
        }

        if (myBike.bike != null) {
            //     mTxtBorrowedFromDate.setText(DateUtils.getDate(myBike.bike.location.returnedAt)); //TODO waiting for api

            bikeId = myBike.bike.id;
            setData(myBike.bike.name, myBike.bike.lockCode);
            return;
        }

        if (myBike.lockCode != null) {
            //  mTxtBorrowedFromDate.setText(DateUtils.getDate(myBike.lockCode.bike.location.returnedAt));  //TODO waiting for api

            bikeId = myBike.lockCode.bike.id;
            hasIssues = myBike.lockCode.bike.issues.size() > 0;
            setData(myBike.lockCode.bike.name, myBike.lockCode.lockCode);
        }
    }

    private void setData(String name, String lockCode) {
        mTxtBikeName.setText(name);
        mTxtLockCode.setText(lockCode == null ? null : lockCode.replace("", " ").trim());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
