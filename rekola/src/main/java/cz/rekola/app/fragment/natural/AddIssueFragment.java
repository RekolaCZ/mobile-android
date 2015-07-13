package cz.rekola.app.fragment.natural;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.defaultValues.DefaultValues;
import cz.rekola.app.api.model.defaultValues.Issue;
import cz.rekola.app.core.bus.dataAvailable.DefaultValuesAvailableEvent;
import cz.rekola.app.core.interfaces.SetIssueItemInterface;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * Screen to add new issue
 */
public class AddIssueFragment extends BaseMainFragment implements SetIssueItemInterface {

    @InjectView(R.id.spn_issue_type)
    Spinner spnIssueType;

    DefaultValues mDefaultValues;

    public AddIssueFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_issue, container, false);
        ButterKnife.inject(this, view);

        setSpinner();

        return view;
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
                new ArrayAdapter<>(getActivity(), R.layout.spinner_item_text, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnIssueType.setAdapter(adapter);
        spnIssueType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    getPageController().requestSpinnerList(spinnerArray, setIssueItemInterface);
                }
                return true;
            }
        });

    }

    @Override
    public void setIssueItem(int item) {
        spnIssueType.setSelection(item);
    }
}
