package cz.rekola.app.fragment.natural;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.rekola.app.R;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Info about bike for user (there is info screen for serviceman)like name, problems, last returned, ...
 */
public class BikeDetailFragment extends BaseMainFragment {

    private static String ARG_BIKE_ID = "BIKE_ID";

    private int mBikeID;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bike_detail, container, false);
    }


}
