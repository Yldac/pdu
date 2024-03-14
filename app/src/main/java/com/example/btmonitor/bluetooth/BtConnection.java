package com.example.btmonitor.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.btmonitor.adapter.BtConsts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BtConnection {
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Handler onMessageReceivedHandler;

    public BtConnection(@NonNull Context context) {
        this.onMessageReceivedHandler = null;
        try {
            SharedPreferences preferences = context.getSharedPreferences(BtConsts.MY_PREF,
                    Context.MODE_PRIVATE);
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            String remouteMac = preferences.getString(BtConsts.MAC_KEY, "");
            if (!btAdapter.isEnabled() || remouteMac.isEmpty())
                return;
            BluetoothDevice device = btAdapter.getRemoteDevice(remouteMac);
            if (device == null)
                return;
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                return;
            socket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) !=
                    PackageManager.PERMISSION_GRANTED)
                return;
            btAdapter.cancelDiscovery();
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            startReceiveRoutine();
            Log.d("package:mine", "connected");
        }
        catch (IOException e) {
            Log.d("package:mine", "not connected");
            closeConnection();
        }
    }

    public void setOnMessageReceivedHandler(Handler onMessageReceivedHandler) {
        this.onMessageReceivedHandler = onMessageReceivedHandler;
    }

    private void startReceiveRoutine() {
        Runnable receiveRoutine = () -> {
            byte[] readBuffer = new byte[1024];
            while (true) {
                try {
                    int bytesRead = inputStream.read(readBuffer);
                    String message = new String (readBuffer, 0, bytesRead);
                    Log.d("package:mine", "Message: "+ message);
                    if (onMessageReceivedHandler != null)
                    {
                        Message messageToMain;
                        messageToMain = onMessageReceivedHandler.obtainMessage(0, message);
                        onMessageReceivedHandler.sendMessage(messageToMain);
                    }
                }
                catch (IOException ignored) {

                }
            }
        };
    }

    public void sendMessage(@NonNull String message) {
        try {
            outputStream.write(message.getBytes());
        }
        catch (IOException ignored) {
        }
    }

    private void closeConnection() {
        try {
            socket.close();
        }
        catch (IOException ignored) {
        }
    }
}
