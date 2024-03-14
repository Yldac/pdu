package com.example.btmonitor;

import static android.bluetooth.BluetoothAdapter.STATE_OFF;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Dialog;
import android.bluetooth.BluetoothSocket;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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


import com.example.btmonitor.bluetooth.BtConnection;


import java.io.InputStream;
import java.util.Objects;


@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private BtConnection btConnection;
    private TextView timerTime, krionLogo;
    private SeekBar timerSeekBar, brightnessSeekBar;
    private String ms, md, x, level;
    private MenuItem menuItem;
    private BluetoothAdapter btAdapter;
    private static final String[] PERMISSION_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
    };
    public int ENABLE_REQUEST = 15;
    private Button buttonS, buttonPower, buttonD, buttonC, btn;
    private ImageButton buttonLiftUp, buttonLiftDown,buttonPlUp, buttonPlDown, buttonSendTimer, buttonBrightness;
    private GestureDetector mGestureDetector;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        checkPermissions();
        btOnStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(String.valueOf(STATE_OFF));
        this.registerReceiver(broadcastReceiver, filter);

        krionLogo = findViewById(R.id.krion_logo);
        timerTime = findViewById(R.id.timer_time);
        timerSeekBar = findViewById(R.id.timer_seek_bar);
        brightnessSeekBar = findViewById(R.id.brightness_seek_bar);

        buttonS = findViewById(R.id.btn_s);
        buttonPower = findViewById(R.id.btn_p);
        buttonC = findViewById(R.id.btn_c);
        buttonD = findViewById(R.id.btn_d);
        buttonPlUp = findViewById(R.id.btn_pipe_up);
        buttonPlDown = findViewById(R.id.btn_pipe_down);
        buttonSendTimer = findViewById(R.id.timer_btn_accept);
        buttonBrightness = findViewById(R.id.btn_rgb_brighness);

        mGestureDetector = new GestureDetector(this, this);
        buttonLiftUp = findViewById(R.id.btn_lift_up);
        buttonLiftUp.setOnTouchListener(this);
        buttonLiftDown = findViewById(R.id.btn_lift_down);
        buttonLiftDown.setOnTouchListener(this);

        Handler handler = new Handler(this.getMainLooper()) {
            public void handleMessage(@NonNull android.os.Message msg) {
                if (msg.obj.toString().equals("s"))
                    buttonS.setText("stop");
                else if (msg.obj.toString().equals("d"))
                    buttonS.setText("Start");

            }
        };
        btConnection.setOnMessageReceivedHandler(handler);

        buttonPower.setEnabled(false);
        buttonS.setEnabled(false);
        buttonD.setEnabled(false);
        buttonC.setEnabled(false);
        buttonLiftDown.setEnabled(false);
        buttonLiftUp.setEnabled(false);
        buttonPlDown.setEnabled(false);
        buttonPlUp.setEnabled(false);
        buttonSendTimer.setEnabled(false);
        buttonBrightness.setEnabled(false);

        timerSeekBar.setMax(360);
        timerSeekBar.setMin(60);
        timerSeekBar.setProgress(180);
        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int stepSize = 5;
                progress = (progress / stepSize) * stepSize;
                seekBar.setProgress(progress);
                timerTime.setText("" + progress + " сек");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

       buttonBrightness.setOnClickListener(v ->{

            showBrightness();

//           Fragment fragment = new BrightnessFragment();
//           FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//           ft.replace(R.id.container, fragment);
//           ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//           ft.addToBackStack(null);
//           ft.commit();
           //ft.replace(R.id.container, fragment).commit();
       });
        buttonSendTimer.setOnClickListener(v -> {
            x = String.valueOf(timerSeekBar.getProgress());
            btConnection.sendMessage("#" + x + "\n");
        });
        buttonS.setOnClickListener(v -> {
            //buttonDisable(buttonS);
            btConnection.sendMessage("$2" + "\n");
        });
        buttonPower.setOnClickListener(v -> btConnection.sendMessage("$1" + "\n"));
        buttonD.setOnClickListener(v -> btConnection.sendMessage("$4" + "\n"));
        buttonC.setOnClickListener(v -> btConnection.sendMessage("$3" + "\n"));
        buttonPlUp.setOnClickListener(v -> btConnection.sendMessage("$6" + "\n"));
        buttonPlDown.setOnClickListener(v -> btConnection.sendMessage("$5" + "\n"));
        buttonLiftUp.setOnTouchListener((v, event) -> {
            // если onShowPress "не подключен" посылает "х" и нужно водить пальцем по экрану для засирания порта
            // Если подключен, то выводит то сообщение, которое внутри onShowPress, "х" выводит пр нажатии
            // Если mGestureDetector.onTouchEvent(event); не подключен, нужно водлить пальцем
            ms = "$8";
            md = "\n";
            mGestureDetector.onTouchEvent(event);
            //btConnection.sendMessage("5");
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//
//                btConnection.sendMessage("8");
//            } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                btConnection.sendMessage("0");
            //}

            return true;
        });
        buttonLiftDown.setOnTouchListener((v, event) -> {
            ms = "$7";
            md = "\n";
            mGestureDetector.onTouchEvent(event);
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                //mGestureDetector.onTouchEvent(event);
//                btConnection.sendMessage("2");
//            } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                btConnection.sendMessage("0");
//            }

            return true;
        });
    }

//    public class ReceiveArduinoData extends Thread{
//        private final BluetoothSocket bluetoothSocket;
//        private final InputStream inputStream;
//
//
//        public ReceiveArduinoData (BluetoothSocket socket) {
//            this.bluetoothSocket = socket;
//            InputStream tmpIn = null;
//            try {
//                tmpIn = bluetoothSocket.getInputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            inputStream = tmpIn;
//        }
//        public void run (){
//            byte[] buffer = new byte[10];
//
//            while (true){
//                try {
//                    int bytes = inputStream.read(buffer);
//                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1,buffer).sendToTarget();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//    Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(@NonNull Message msg) {
//            if (msg.what == STATE_MESSAGE_RECEIVED) {
//                byte[] readBuff = (byte[]) msg.obj;
//                String message = new String(readBuff, 0, msg.arg1);
//                if (message.equals("s")) {
//                    Toast.makeText(getApplicationContext(), "Неужели!", Toast.LENGTH_SHORT).show();
//                }
//            }
//            return true;
//        }
//    });

    //Когда выходим и снова заходим чекаем блютуз и выключаем фунции, если он не включен
    @Override
    protected void onResume() {
        if (!btAdapter.isEnabled()) {
            disableAll();
        }
        super.onResume();
    }

    //Ловим с помощью филтров отключение и подключение устройства
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        BluetoothDevice device;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                deviceOnToast();
                // Toast.makeText(getApplicationContext(), "Устройство подключено!", Toast.LENGTH_SHORT).show();
                enablebleAll();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                deviceOffToast();
                //Toast.makeText(getApplicationContext(), "Устройство отключено!", Toast.LENGTH_SHORT).show();
                disableAll();
            }
//            else if (STATE_OFF) {
//                krionLogo.setTextColor(Color.LTGRAY);
//            }

        }
    };

    //Создаем меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menuItem = menu.findItem(R.id.id_bt_btn);
        return super.onCreateOptionsMenu(menu);
    }

    //Узнаем, что кнопка в меню нажата
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.id_bt_btn) {
            checkPermissions();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Нет разрешения на подключение", Toast.LENGTH_SHORT).show();
            }
            if (!btAdapter.isEnabled()) {
                enableBt();

                //Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
            if (btAdapter.isEnabled()) {
                goBtListActivity();
            }
        } else if (item.getItemId() == R.id.id_connect) {
            checkPermissions();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Нет разрешения на подключение", Toast.LENGTH_SHORT).show();
            }
            if (!btAdapter.isEnabled()) {
                //Toast.makeText(this, "Включите блютуз!", Toast.LENGTH_SHORT).show();
                //customToast();
                disableAll();
                enableBt();
            }
            btConnection.connect();
        } else if (item.getItemId() == R.id.id_info) {
            showInfoDialog();
        }


        return super.onOptionsItemSelected(item);
    }

    //Ловим ответ от всплывающего окна разрешения включения Блютуз
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ENABLE_REQUEST) {
            if (resultCode != RESULT_OK) {
                customToast();
            }
        }
    }
    //Регулируем громкость качелькой громкости телефона
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action, keycode;
        action = event.getAction();
        keycode = event.getKeyCode();

        if (keycode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (KeyEvent.ACTION_DOWN == action) {
                //ms = "+";
                btConnection.sendMessage("$10" + "\n");
            }
        }
        if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (KeyEvent.ACTION_DOWN == action) {
                //ms = "-";
                btConnection.sendMessage("$9" + "\n");
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void checkPermissions() {
        int permission2 = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        }

        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            int i = 15;
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSION_LOCATION, i
            );
        }
    }

    //Задержка отправления сообщений контроллеру
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    //Выключаем функцию нажатия у кнопок и цвет надписи "Крион" снизу
    private void disableAll (){
            krionLogo.setTextColor(Color.BLACK);
            buttonPower.setEnabled(false);
            buttonS.setEnabled(false);
            buttonD.setEnabled(false);
            buttonC.setEnabled(false);
            buttonLiftDown.setEnabled(false);
            buttonLiftUp.setEnabled(false);
            buttonPlDown.setEnabled(false);
            buttonPlUp.setEnabled(false);
            buttonSendTimer.setEnabled(false);
            buttonBrightness.setEnabled(false);
    }
    //Включаем функцию нажатия у кнопок и цвет надписи "Крион" снизу
    @SuppressLint("ResourceAsColor")
    private void enablebleAll (){
        krionLogo.setTextColor(Color.WHITE);
        buttonPower.setEnabled(true);
        buttonS.setEnabled(true);
        buttonD.setEnabled(true);
        buttonC.setEnabled(true);
        buttonLiftDown.setEnabled(true);
        buttonLiftUp.setEnabled(true);
        buttonPlDown.setEnabled(true);
        buttonPlUp.setEnabled(true);
        buttonSendTimer.setEnabled(true);
        buttonBrightness.setEnabled(true);
    }
    //Диалоговое окно при нажатии кнопки "контакты" в меню
    private void showInfoDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        LinearLayout ethernetLayout = dialog.findViewById(R.id.ethernet);
        LinearLayout phoneLayout = dialog.findViewById(R.id.phone);
        LinearLayout techlayout = dialog.findViewById(R.id.tech);
        LinearLayout tgbot = dialog.findViewById(R.id.tg_bot);

        ethernetLayout.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://krion.ru"));
            startActivity(browserIntent);
        });
        phoneLayout.setOnClickListener(v -> {
            //Ofice
            String number = "+7-812-679-28-26";
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" +number));
            startActivity(intent);
        });
        techlayout.setOnClickListener(v -> {
            //Tech
            String number = "+7 999 669-75-80";
            Intent i =new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + number));
            startActivity(i);
        });
        tgbot.setOnClickListener(v -> {
            //TgBot
            Intent i =new Intent(Intent.ACTION_VIEW,Uri.parse("https://t.me/+fAy4IEM3PHNiOTNi"));
            startActivity(i);
        });
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialiogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    // Диалог яркости RGB
    public void showBrightness(){
        Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.fragment_brightness);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View lol = layoutInflater.inflate(R.layout.fragment_brightness, null);

        brightnessSeekBar = lol.findViewById(R.id.brightness_seek_bar);
        brightnessSeekBar.setMax(255);
        brightnessSeekBar.setProgress(125);
        brightnessSeekBar.setMin(1);

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int stepSize = 10;
                progress = (progress / stepSize) * stepSize;
                seekBar.setProgress(progress);
                level = String.valueOf(brightnessSeekBar.getProgress());

//                x = String.valueOf(timerSeekBar.getProgress());
//                btConnection.sendMessage("#" + x + "\n");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                btConnection.sendMessage("@" + level + "\n");
            }
        });

//        btn = lol.findViewById(R.id.jopa);
//        btn.setOnClickListener(v -> {
//            btConnection.sendMessage("sad");
//        });

        dialog.setContentView(lol);
        //dialog.setTitle("Яркость");
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialiogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    // Mеняем картинку (не используем)
    private void setBtIcon() {
        if (btAdapter.isEnabled()) {
            menuItem.setIcon(R.drawable.ic_bt_disable);
        } else {
            menuItem.setIcon(R.drawable.ic_bt_enable);
        }
    }

    // Инициализируем Адаптер
    private void init() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btConnection = new BtConnection(this);

    }
    //Toast "Включите блютуз"
    private void customToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.toast_layout));

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 800);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
    //Toast подключения утсройства
    private void deviceOnToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_device_on, findViewById(R.id.toast_layout));

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 760);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
    //Toast отключения устройства
    private void deviceOffToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_device_off, findViewById(R.id.toast_layout));

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 760);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    //Включаем блютуз
    private void enableBt() {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivityForResult(i, 15);
    }

    //Функия перехода про нажатии на кнопку поиска устройств
    private void goBtListActivity() {
        Intent intent = new Intent(MainActivity.this, BtListActivity.class);
        startActivity(intent);
    }

    //Функции реагирования на жесты нажатия (используем только onShowPress)
    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        btConnection.sendMessage(ms + md);
        wait(180);
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
    }

    @Override
    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    // Рабочий код для бескончеыной отправки "8" в порт
    //
    //в On Create

//    mGestureDetector = new GestureDetector(this, this);
//    buttonLUP = findViewById(R.id.btn_lift_up);
//        buttonLUP.setOnTouchListener(this);
//    buttonLDW = findViewById(R.id.btn_lift_down);
//        buttonLDW.setOnTouchListener(this);
//    buttonVolDown = findViewById(R.id.btn_vol_down);
//        buttonVolDown.setOnTouchListener(this);
//    buttonVolUp = findViewById(R.id.btn_vol_up);
//        buttonVolUp.setOnTouchListener(this);

//    @Override
//    public boolean onDown(@NonNull MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public void onShowPress(@NonNull MotionEvent e) {
//        mGestureDetector.onTouchEvent(e);
//        btConnection.sendMessage("8");
//    }
//
//    @Override
//    public boolean onSingleTapUp(@NonNull MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
//        return false;
//    }
//
//    @Override
//    public void onLongPress(@NonNull MotionEvent e) {
//
//    }
//
//    @Override
//    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
//        return false;
//    }
//
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if (v.getId() == R.id.btn_lift_up){
//            mGestureDetector.onTouchEvent(event);
//        }
//        return false;
//    }

    //Функция отправления сообщения при нажатии кнопки (не используем)
    private void btMessage(MotionEvent event, String x) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            btConnection.sendMessage(ms);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //btConnection.sendMessage(x);
        }
    }

    //При входе в приложение заставляем включить блютуз
    private void btOnStart() {
        if (!btAdapter.isEnabled()) {
            enableBt();
            //Toast.makeText(this, "Включите блютуз", Toast.LENGTH_LONG).show();
        }
    }



    // MainActivity to Fragment
//    private void goScanFragmentTest() {
//        btn_add_device = findViewById(R.id.button);
//        btn_add_device.setVisibility(View.GONE);
//        Fragment fragment = new ScanFragment();
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//
//        //container = id main activity.xml layout
//        ft.replace(R.id.container, fragment).commit();
//    }
}
