package com.example.btmonitor.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.btmonitor.R;
import com.example.btmonitor.adapter.BtAdapter;
import com.example.btmonitor.adapter.ListItem;

import java.util.ArrayList;
import java.util.List;


public class ScanFragment extends Fragment {

    private ListView listView;
    private BtAdapter adapter;

    private ScanFragment binding;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_scan, container, false);
//    }

    //Убираем + в меню фрагмента
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

//    private void init (){
//        listView = listView.findViewById(R.id.listView);
//        List<ListItem> list = new ArrayList<>();
//        ListItem item = new ListItem();
//        item.setBtName("BT-1234");
//        list.add(item);
//        list.add(item);
//        list.add(item);
//        list.add(item);
//
//        adapter  = new BtAdapter(R.layout.bt_list_item, list);
//        listView.setAdapter(adapter);
//    }

    //    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.id_bt_btn).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}