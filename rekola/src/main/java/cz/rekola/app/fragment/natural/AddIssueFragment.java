package cz.rekola.app.fragment.natural;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddIssueFragment extends BaseMainFragment {


    @InjectView(R.id.spn_issue_type)
    Spinner spn_issue_type;

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

    private void setSpinner() {
        List<String> spinnerArray = new ArrayList<String>();
        for (int i = 0; i < 10; i++)
            spinnerArray.add("item " + i);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout
                .simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_issue_type.setAdapter(adapter);
    }


}
