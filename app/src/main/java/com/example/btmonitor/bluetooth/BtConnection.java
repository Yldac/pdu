package com.example.btmonitor.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.btmonitor.adapter.BtConsts;

import java.io.IOException;
import java.util.Arrays;

public class BtConnection {
    private Context context;
    private SharedPreferences pref; // получаем доступ к памяти, где хранится MAC выбранного устройства
    private BluetoothAdapter btAdapter;
    private BluetoothDevice device; //Класс который хранит инфо об устройстве
    private ConnectThread connectThread;
    private RecieveThread receiveThread;
    public BtConnection(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect(){
        String mac = pref.getString(BtConsts.MAC_KEY, "");
        if (!btAdapter.isEnabled() || mac.isEmpty()) return;
        device = btAdapter.getRemoteDevice(mac);
        if(device == null) return;
        connectThread = new ConnectThread(context, btAdapter, device);
        connectThread.start();
    }

    public void sendMessage (String message){
        connectThread.getRThread().sendMessage(message.getBytes());

    }
}
