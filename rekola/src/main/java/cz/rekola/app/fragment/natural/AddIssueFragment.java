package cz.rekola.app.fragment.natural;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Bike;
import cz.rekola.app.api.model.defaultValues.DefaultValues;
import cz.rekola.app.api.model.defaultValues.Issue;
import cz.rekola.app.api.requestmodel.IssueReport;
import cz.rekola.app.api.requestmodel.IssueReportLocation;
import cz.rekola.app.core.bus.dataAvailable.DefaultValuesAvailableEvent;
import cz.rekola.app.core.interfaces.SetIssueItemInterface;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Screen to add new issue
 */
public class AddIssueFragment extends BaseMainFragment implements SetIssueItemInterface {
    public static final String TAG = AddIssueFragment.class.getName();

    @InjectView(R.id.spn_issue_type)
    Spinner mSpnIssueType;
    @InjectView(R.id.txt_issue_description)
    EditText mTxtIssueType;
    @InjectView(R.id.chk_inoperational)
    CheckBox mChkInoperational;

    DefaultValues mDefaultValues;
    boolean mIsDefaultState = true;
    boolean mIssueTypeIsSelected = false;
    int mBikeID;

    public AddIssueFragment() {
        // Required empty public constructor
    }

    public void init(int bikeID, boolean isDefaultState) {
        Log.d("tom", "init");
        mBikeID = bikeID;
        mIsDefaultState = isDefaultState;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden && mIsDefaultState ) {
            setDefaultState();
            Log.d("tom", "setDefaultState");
            mIsDefaultState = false;
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_issue, container, false);
        ButterKnife.inject(this, view);

        setSpinner();

        return view;
    }

    @OnClick(R.id.btn_report_issue)
    public void reportIssue() {
        if (mBikeID == Bike.UNDEFINED_BIKE) {
            Log.e(TAG, "bike was not initialized");
            return;
        }

        if (!isIssueTypeSelected()) {
            return;
        }

        if (!isIssueDescriptionFilled()) {
            return;
        }


        Issue issue = mDefaultValues.issues.get(mSpnIssueType.getSelectedItemPosition());
        int type = issue.id;
        String title = issue.title;

        String description = mTxtIssueType.getText().toString();
        boolean disabling = mChkInoperational.isChecked();

        LatLng latLng = getApp().getMyLocationManager().getLastKnownMyLocation().getLatLng();
        IssueReportLocation location = new IssueReportLocation(latLng.latitude, latLng.longitude);

        IssueReport issueReport = new IssueReport(type, title, description, disabling, location);
        getApp().getDataManager().reportIssue(mBikeID, issueReport);

        showDialog(R.string.add_issue_success, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getAct().onClickActionLock();
            }
        });
    }

    @Subscribe
    public void isDefaultValueAvailable(DefaultValuesAvailableEvent event) {
        setSpinner();
    }


    private void setSpinner() {
        mDefaultValues = getApp().getDataManager().getDefaultValues();
        if (mDefaultValues == null)
            return; // will be set later by event isBikeDetailAvailable


        final ArrayList<String> spinnerArray = new ArrayList<>();
        final SetIssueItemInterface setIssueItemInterface = this;

        for (Issue issue : mDefaultValues.issues) {
            spinnerArray.add(issue.title);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(), R.layout.spinner_item_text,
                        R.id.txt_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnIssueType.setAdapter(adapter);
        mSpnIssueType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    getPageController().requestSpinnerList(spinnerArray, setIssueItemInterface);
                }
                return true;
            }
        });
    }

    private void setDefaultState() {
        mTxtIssueType.setText("");
        mChkInoperational.setChecked(false);
        mIssueTypeIsSelected = false;

        mSpnIssueType.setBackgroundResource(R.drawable.spinner_grey);
        TextView spinnerItemText = (TextView) mSpnIssueType.findViewById(R.id.txt_spinner_item);
        int grey = getActivity().getResources().getColor(R.color.spinner_text);
        spinnerItemText.setTextColor(grey);
    }

    /**
     * check if issue description is filled
     *
     * @return true if there is no problem, false if there is some problem
     */
    private boolean isIssueDescriptionFilled() {
        if (mTxtIssueType.getText().toString().trim().equals("")) {
            showDialog(R.string.error_fill_issue_description, null);
            return false;
        } else
            return true;
    }

    private boolean isIssueTypeSelected() {
        if (!mIssueTypeIsSelected) {
            showDialog(R.string.error_select_issue_type, null);
            return false;
        } else
            return true;
    }

    private void showDialog(int textID, DialogInterface.OnClickListener onClickListener) {
        String text = getString(textID);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set dialog message
        alertDialogBuilder
                .setMessage(text)
                .setPositiveButton(R.string.ok, onClickListener)
                .setCancelable(false);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void setIssueItem(int item) {

        //change spinner design if user select item
        mSpnIssueType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSpnIssueType.setBackgroundResource(R.drawable.spinner_pink);
                TextView spinnerItemText = (TextView) mSpnIssueType.findViewById(R.id.txt_spinner_item);
                int pink = getActivity().getResources().getColor(R.color.base_pink);
                spinnerItemText.setTextColor(pink);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpnIssueType.setSelection(item);
        mIssueTypeIsSelected = true;
        mIsDefaultState = false;
    }
}
