package com.example.btmonitor.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.btmonitor.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecieveThread extends Thread {
    private BluetoothSocket socket;
    private InputStream inputS;
    private OutputStream outputS;
    private byte[] rBuffer;
    static final int STATE_MESSAGE_RECEIVED=5;
    private String valueRead;
    MainActivity activity;
    //private Handler handler;


    public RecieveThread(BluetoothSocket socket) {
        this.socket = socket;
        try {
           inputS = socket.getInputStream();
        } catch (IOException e) {}
        try {
            outputS = socket.getOutputStream();
        } catch (IOException e) {}
    }



    @Override
    public void run() {
        rBuffer = new byte[5];
        while (true){
            try {
                int size = inputS.read(rBuffer);
                String message = new String (rBuffer, 0, size);
                handler.obtainMessage(STATE_MESSAGE_RECEIVED, message).sendToTarget();
                Log.d("package:mine", "Message: "+ message);
            } catch (IOException ignored){}
        }
    }
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case STATE_MESSAGE_RECEIVED:
//                    handler.sendMessage();
//                    Toast.makeText(activity, "asd", Toast.LENGTH_SHORT).show();
//                    byte[] readBuff = (byte[]) msg.obj;
//                    valueRead = new String(readBuff,0,msg.arg1);
                    break;
            }
        }
    };

    public void sendMessage (byte[] byteArray){
        try {
            outputS.write(byteArray);
            //outputS.write(valueRead.getBytes());
        } catch (IOException ignored){}
    }

    public String ValueRead (){
        return valueRead;
    }

}
