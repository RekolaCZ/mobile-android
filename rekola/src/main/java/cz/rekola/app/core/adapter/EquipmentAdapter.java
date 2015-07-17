package cz.rekola.app.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cz.rekola.app.R;
import cz.rekola.app.api.model.bike.Equipment;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {17. 7. 2015}
 **/
public class EquipmentAdapter extends BaseAdapter {
    public static final String TAG = EquipmentAdapter.class.getName();

    Context mContext;
    List<Equipment> mEquipmentList;
    private static LayoutInflater mInflater = null;

    public EquipmentAdapter(Context context, List<Equipment> equipmentList) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.mEquipmentList = equipmentList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mEquipmentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mEquipmentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View equipmentView = convertView;
        if (equipmentView == null)
            equipmentView = mInflater.inflate(R.layout.dialog_equipments_item, null);

        TextView txtDescription = (TextView) equipmentView
                .findViewById(R.id.txt_equipment_description);
        ImageView imgIcon = (ImageView) equipmentView
                .findViewById(R.id.img_equipment_icon);

        imgIcon.setImageResource(R.drawable.ic_warning);
        //TODO waiting for api
       /* Glide.with(mContext)
                .load(mEquipmentList.get(position).iconUrl)
                .into(imgIcon);*/

        txtDescription.setText(mEquipmentList.get(position).description);

        return equipmentView;
    }

}
