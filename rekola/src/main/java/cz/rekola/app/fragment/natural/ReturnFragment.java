package cz.rekola.app.fragment.natural;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.core.bus.BorrowedBikeAvailableEvent;
import cz.rekola.app.core.bus.LockCodeEvent;
import cz.rekola.app.core.data.MyBikeWrapper;
import cz.rekola.app.fragment.base.BaseMainFragment;

public class ReturnFragment extends BaseMainFragment {


    @InjectView(R.id.vehicleName)
    TextView vBikeName;
    @InjectView(R.id.lock_code)
    TextView vLockCode;
    @InjectView(R.id.borrowed_from_date)
    TextView vBorrowedFromDate;
    @InjectView(R.id.borrowed_from_time)
    TextView vBorrowedFromTime;


    @InjectView(R.id.return_bike)
    Button vReturn;
    @InjectView(R.id.return_bike_detail)
    Button vDetail;

    private int bikeId = -1; // Invalid bike id

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_return, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        if (!getApp().getDataManager().isOperational())
            return;

        populateData();

        vReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPageController().requestReturnMap();
            }
        });

        vDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPageController().requestBikeDetail(bikeId);
            }
        });

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

        //TODO: lastSeen should be date and time
        if (myBike.bike != null) {
            vBorrowedFromDate.setText(myBike.bike.lastSeen);

            bikeId = myBike.bike.id;
            setData(myBike.bike.name, myBike.bike.lockCode);
            return;
        }

        if (myBike.lockCode != null) {
            vBorrowedFromDate.setText(myBike.lockCode.bike.lastSeen);

            bikeId = myBike.lockCode.bike.id;
            setData(myBike.lockCode.bike.name, myBike.lockCode.lockCode);
            return;
        }
    }

    private void setData(String name, String lockCode) {
        vBikeName.setText(name);
        vLockCode.setText(lockCode == null ? null : lockCode.replace("", " ").trim());
    }

}
