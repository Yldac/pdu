package com.example.btmonitor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.btmonitor.adapter.BtConsts;
import com.example.btmonitor.bluetooth.BtConnection;


import java.security.Permission;
import java.util.Objects;


public class BrightnessFragment extends Fragment {

    private SharedPreferences pref;
    private TextView text;
    private SeekBar brightnessSeekBar;
    private String level;
    private  Button btn;
    private BluetoothAdapter btAdapter;
    private BtConnection btConnection;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_brightness, container, false);

        init();

        //btn = v.findViewById(R.id.jopa);
        brightnessSeekBar = v.findViewById(R.id.brightness_seek_bar);
        text = v.findViewById(R.id.brightness_kostil);

        btn.setOnClickListener(v1 -> {
            Toast.makeText(getContext(), "GOVNOO!", Toast.LENGTH_SHORT).show();
        });
        brightnessSeekBar.setMax(255);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            brightnessSeekBar.setMin(0);
        }
        brightnessSeekBar.setProgress(125);

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int stepSize = 5;
                progress = (progress / stepSize) * stepSize;
                seekBar.setProgress(progress);
                text.setText("" + progress + " сек");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            btConnection.sendMessage(String.valueOf(text));
            }
        });

        return v;
    }
    private void init() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        SharedPreferences pref = requireContext().getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        btConnection = new BtConnection(getContext());
    }
}
