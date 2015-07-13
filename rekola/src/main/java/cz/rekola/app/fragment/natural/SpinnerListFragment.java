package cz.rekola.app.fragment.natural;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.rekola.app.R;
import cz.rekola.app.core.interfaces.SetIssueItemInterface;
import cz.rekola.app.fragment.base.BaseMainFragment;

/**
 * fullscreen spinner list
 */
public class SpinnerListFragment extends BaseMainFragment {

    private ArrayList<String> mListItems;
    private SetIssueItemInterface mSetIssueItemInterface;

    @InjectView(R.id.lv_items)
    ListView mLvItems;

    public SpinnerListFragment() {
        // Required empty public constructor
    }

    public void init(ArrayList<String> listItems, SetIssueItemInterface setIssueItemInterface) {
        mListItems = listItems;
        mSetIssueItemInterface = setIssueItemInterface;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getAct().getSupportActionBar() != null) {
            getAct().getSupportActionBar().hide();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spinner_list, container, false);

        ButterKnife.inject(this, view);
        setListView();

        return view;
    }

    @OnClick(R.id.btn_close)
    public void close() {
        if (getAct().getSupportActionBar() != null) {
            getAct().getSupportActionBar().show();
        }
        getPageController().requestPrevState();
    }

    private void setListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_list_item,
                R.id.txt_item, mListItems);

        mLvItems.setAdapter(adapter);
        mLvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSetIssueItemInterface.setIssueItem(position);
                getPageController().requestPrevState();
            }
        });
    }
}
