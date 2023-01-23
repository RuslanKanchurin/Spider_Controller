package ru.kruslan.spidercontroller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    ThreadConnected myThreadConnected;
    BluetoothAdapter bluetoothAdapter;
    private OutputStream connectedOutputStream;
    boolean StopConnection=false;
    boolean success;
    BluetoothSocket bluetoothSocket = null;
    BluetoothDevice device2;
    UUID myUUID;

    TextView tv;
    BluetoothManager BluetoothManager1=new BluetoothManager();
    boolean connectionType=false; //false - bluetooth, true - wifi
    Connection connection=new Connection();

    private  String     HOST      = "192.168.4.1";
    private  int        PORT      = 8880;
    private TelnetManage tm;

    private void checkConnectionStatus(){

        if(connection.isConnected()){
            tv.setText("Подключено");
        }else {
            tv.setText("Не подключено");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=(TextView) findViewById(R.id.isConnected_text);
        tv.setText("Подключено");
        //checkConnectionStatus();
        ImageButton levo=(ImageButton) findViewById(R.id.levo);
        ImageButton pravo=(ImageButton) findViewById(R.id.pravo);
        ImageButton vniz=(ImageButton) findViewById(R.id.vniz);
        ImageButton verh=(ImageButton) findViewById(R.id.verh);
        ImageButton bluetooth_button=(ImageButton) findViewById(R.id.bluetooth_image);
        ImageButton wifi_button=(ImageButton) findViewById(R.id.wifi_image);
        Button dance=(Button)findViewById(R.id.dance);
        Button shake=(Button)findViewById(R.id.hand_shake);
        Button wave=(Button)findViewById(R.id.hand_wave);

        SwitchCompat switchCompat=(SwitchCompat)findViewById(R.id.switch1);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //checkConnectionStatus();
                    if (isChecked){
                        connection.writeMessage("I");
                }else{
                        connection.writeMessage("O");
                    }
            }
        });

        levo.setOnTouchListener(MainActivity.this);
        pravo.setOnTouchListener(MainActivity.this);
        vniz.setOnTouchListener(MainActivity.this);
        verh.setOnTouchListener(MainActivity.this);
        dance.setOnClickListener(MainActivity.this);
        shake.setOnClickListener(MainActivity.this);
        wave.setOnClickListener(MainActivity.this);
        bluetooth_button.setOnClickListener(MainActivity.this);
        wifi_button.setOnClickListener(MainActivity.this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetooth is not supported on this hardware platform", Toast.LENGTH_LONG).show();
            finish();
            return;
        }



    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {


                        }

                    });
            mStartForResult.launch(enableIntent);
        }
        checkConnectionStatus();
    }




    @Override
    public boolean onTouch(View v, MotionEvent event) {
        checkConnectionStatus();
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    switch (v.getId()){
                        case R.id.levo:connection.writeMessage("a");break;
                        case R.id.verh:connection.writeMessage("w");break;
                        case R.id.vniz:connection.writeMessage("s");break;
                        case R.id.pravo:connection.writeMessage("d");break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + v.getId());
                    }
                    break;

                case MotionEvent.ACTION_UP:
                        switch (v.getId()){
                            case R.id.levo:connection.writeMessage("t");break;
                            case R.id.verh:connection.writeMessage("t");break;
                            case R.id.vniz:connection.writeMessage("t");break;
                            case R.id.pravo:connection.writeMessage("t");break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + v.getId());



                    }
                    break;
            }
        return true;
    }

    @Override
    public void onClick(View v) {
        checkConnectionStatus();
        String mes;

            switch (v.getId()){
                case R.id.dance:connection.writeMessage("M");break;
                case R.id.hand_wave:connection.writeMessage("P");break;
                case R.id.hand_shake:connection.writeMessage("T");break;
                case R.id.bluetooth_image:  connectionType=false; connection.connect();break;
                case R.id.wifi_image:connectionType=true;connection.connect(); break;
                default:
                    throw new IllegalStateException("Unexpected value: " + v.getId());



        }
    }


    public class Connection {


        public void connect(){
            if (connectionType) {
                tm=TelnetManage.getInstance();
                //mConnect = new Connection(HOST, PORT);
                // Открытие сокета в отдельном потоке
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tm.connect(HOST, PORT);

                        } catch (Exception e) {

                        }
                    }
                }).start();
            }
            else {
                blue_list();
            }
        }
        public boolean isConnected(){
            if (connectionType) {
                return false;
            }
            else {
                return success;
            }
        }
        public void writeMessage(String mes){
            if (connectionType) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            tm.sendMsg(mes);
                        } catch (Exception e) {

                        }
                    }
                }).start();
            }
            else {
                BluetoothManager1.writeMessage(mes);
            }
        }

    }
    ActivityResultLauncher<Intent> listForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        String MAC = intent.getStringExtra("MAC");
                        tv.setText("Подключено");
                        device2 = bluetoothAdapter.getRemoteDevice(MAC);
                        StopConnection=false;
                        ThreadConnectBTdevice myThreadConnectBTdevice = new ThreadConnectBTdevice(device2);
                        myThreadConnectBTdevice.start();


                    }
                }

            });
    public void blue_list() {
        Intent intent=new Intent(MainActivity.this,DevicesListActivity.class);
        listForResult.launch(intent);


    }

    public class BluetoothManager {

        public void writeMessage(String mes){
            if (success) {
                checkThreadConnected();
                myThreadConnected.write(mes.getBytes());
            }
        }
        public void checkThreadConnected() {
            if (myThreadConnected == null) {
                myThreadConnected = new ThreadConnected(bluetoothSocket);
                myThreadConnected.start();
            }
        }


    }
    public void checkBluetoothAdapter() {


    }
    public void getRequestEnable() {

    }
    public class ThreadConnected extends Thread {
        private final InputStream connectedInputStream;

        private String sbprint;

        public ThreadConnected(BluetoothSocket socket) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectedInputStream = in;
            connectedOutputStream = out;
        }



        @Override
        public void run() {
            while (!StopConnection) {
                try {
                    byte[] buffer = new byte[1];
                    int bytes = connectedInputStream.read(buffer);
                }
                catch (IOException e){
                    success=false;
                    myThreadConnected = null;
                    try {
                        bluetoothSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    StopConnection=true;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class ThreadConnectBTdevice extends Thread {

        public ThreadConnectBTdevice(BluetoothDevice device) {
            try {
                final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
                myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            success = false;
            try {
                bluetoothSocket.connect();
                success = true;

            }
            catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Нет соединения", Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    bluetoothSocket.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            //tv.setText("Подключено");

        }

        public void cancel() {
            Toast.makeText(getApplicationContext(), "Close - BluetoothSocket", Toast.LENGTH_LONG).show();
            try {
                bluetoothSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



}