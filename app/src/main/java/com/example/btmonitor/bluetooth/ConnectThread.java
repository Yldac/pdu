package com.example.btmonitor.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;


public class ConnectThread extends Thread {
    private final Context context;
    private final BluetoothAdapter btAdapter;
    private RecieveThread rThread;
    private BluetoothSocket mSocket;
    private String valueRead;
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public ConnectThread(Context context, BluetoothAdapter btAdapter, BluetoothDevice device) {
        this.context = context;
        this.btAdapter = btAdapter;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mSocket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
        }
        btAdapter.cancelDiscovery();
        try {
            mSocket.connect();
            rThread = new RecieveThread(mSocket);
            rThread.start();
            Log.d("package:mine", "connected");
        } catch (IOException e) {
            Log.d("package:mine", "not connected");
            closeConnection();
        }
    }

    public void closeConnection(){
        try {
            mSocket.close();
        } catch (IOException y){}
    }
    public RecieveThread getRThread() {
        return rThread;
    }

}


