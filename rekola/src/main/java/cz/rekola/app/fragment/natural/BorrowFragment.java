package cz.rekola.app.fragment.natural;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.rekola.app.R;
import cz.rekola.app.api.model.error.MessageError;
import cz.rekola.app.core.bus.LockCodeEvent;
import cz.rekola.app.core.bus.LockCodeFailedEvent;
import cz.rekola.app.core.bus.MessageEvent;
import cz.rekola.app.fragment.base.BaseMainFragment;

public class BorrowFragment extends BaseMainFragment {

    @InjectView(R.id.bike_code)
    EditText vBikeCode;
    @InjectView(R.id.borrow)
    Button vBorrow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_borrow, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        if (!getApp().getDataManager().isOperational())
            return;

        vBorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vBikeCode.getText().length() != getResources().getInteger(R.integer.bike_code_length)) {
                    getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_incorrect_bike_code)));
                    return;
                }
                getAct().hideKeyboard();
                getApp().getDataManager().borrowBike(Integer.parseInt(vBikeCode.getText().toString()));
            }
        });
    }

    @Subscribe
    public void bikeBorrowed(LockCodeEvent event) {
        getPageController().requestReturnBike();
    }

    @Subscribe
    public void bikeBorrowFailed(LockCodeFailedEvent event) {
        if (event.error != null && event.error instanceof MessageError) {
            getApp().getBus().post(new MessageEvent(((MessageError) event.error).message));
        } else {
            getApp().getBus().post(new MessageEvent(getResources().getString(R.string.error_borrow_bike_failed)));
        }
    }

}
