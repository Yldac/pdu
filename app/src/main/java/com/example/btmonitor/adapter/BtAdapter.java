package com.example.btmonitor.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.btmonitor.R;

import java.util.ArrayList;
import java.util.List;

public class BtAdapter extends ArrayAdapter<ListItem> {

    public static final String DEF_ITEM_TYPE = "normal";
    public static final String TITLE_ITEM_TYPE = "title";
    public static final String DISCOVERY_ITEM_TYPE = "discovery";
    private final List<ListItem> mainList;
    private final List<ViewHolder> listViewHolders;
    private SharedPreferences pref; //класс для сохранения примитивных данных
    private boolean isDiscoveryType = false;

    public BtAdapter(Context context, int resource, List<ListItem> btList) {
        super(context, resource, btList);
        mainList = btList;
        listViewHolders = new ArrayList<>();
        pref = context.getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (mainList.get(position).getItemType().equals(TITLE_ITEM_TYPE)) {
            convertView = titleItem(convertView, parent);
        } else {
            convertView = defaultItem(convertView, position, parent);
        }
        return convertView;
    }

    private void savePref(int pos) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(BtConsts.MAC_KEY, mainList.get(pos).getBtDevice().getAddress());
        editor.apply();
    }

    // При скролле выбора устрйоств для подлкючения, чтобы невидимые заново не прорисовывались
    static class ViewHolder {
        TextView tvBtName;

        //Eсли менять на кнопку сохранения то, CheckBox --> ImageButtin, setChecked --> setPressed
        CheckBox chBtSelected;
    }

    @SuppressLint("SuspiciousIndentation")
    private View defaultItem(View convertView, int position, ViewGroup parent) {
        ViewHolder viewHolder;
        boolean hasViewHolder = false;
        if (convertView != null) hasViewHolder = (convertView.getTag() instanceof ViewHolder);
        if (convertView == null || !hasViewHolder) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item, null, false);
            viewHolder.tvBtName = convertView.findViewById(R.id.tvBtName);
            viewHolder.chBtSelected = convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
            listViewHolders.add(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.chBtSelected.setChecked(false);
        }
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return convertView;
        }
        if (mainList.get(position).getItemType().equals(BtAdapter.DISCOVERY_ITEM_TYPE)){
            viewHolder.chBtSelected.setVisibility(View.GONE);
            isDiscoveryType = true;
        } else {
            viewHolder.chBtSelected.setVisibility(View.VISIBLE);
            isDiscoveryType = false;
        }
        viewHolder.tvBtName.setText(mainList.get(position).getBtDevice().getName());
        viewHolder.chBtSelected.setOnClickListener(v -> {  //Лямбда вместа (new.View.OnClickListener; OV функция  Onclick
            if (!isDiscoveryType) {
            for (ViewHolder holder: listViewHolders) {
                holder.chBtSelected.setChecked(false);
                //viewHolder.chBtSelected.setChecked(true);
            }
            viewHolder.chBtSelected.setChecked(true);
            savePref(position);
            }
        });
        if (pref.getString(BtConsts.MAC_KEY, "No BT selected").equals(mainList.get(position).getBtDevice().getAddress()))viewHolder.chBtSelected.setChecked(true);
            isDiscoveryType = false;
        return convertView;
    }

    private View titleItem (View convertView, ViewGroup parent){
        boolean hasViewHolder = false;
        if (convertView != null) hasViewHolder = (convertView.getTag() instanceof ViewHolder);
        if (convertView == null || hasViewHolder){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item_title, null, false);
        }
        return convertView;
    }

}
