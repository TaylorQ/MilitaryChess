package com.example.taylorq.militarychess;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Intent请求标识
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private Button bluetooth;
    private Button new_game;
    private Button illustrate;
    private TextView bt_stage;
    private Switch new_rule;

    /**
     * 蓝牙消息的发送缓冲区
     */
    private StringBuffer mOutStringBuffer;

    /**
     * 本地蓝牙适配器
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * 蓝牙服务类实例
     */
    private BluetoothService bluetoothService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取本地蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 如果获取不到，证明蓝牙不可用
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        ItemSetup();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 如果蓝牙还未开启，发起开启蓝牙请求
        // ItemSetup()会在onActivityResult()中调用
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (bluetoothService == null) {
            ItemSetup();
        } else if (bluetoothService != null){//将蓝牙服务类的handler重置为本activity的handler
            bluetoothService.setmHandler(mHandler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //在Resume时检查蓝牙服务类是否已经启动
        //这样无论是onStart时顺利实例化了蓝牙服务类还是
        //打开了开启蓝牙请求窗口并返回
        //都能保证启动蓝牙服务类
        if (bluetoothService != null) {
            // 当服务类状态处于STAGE_NONE时意味着还未启动
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                bluetoothService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // 从DeviceListActivity返回了要连接的设备
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // 请求打开蓝牙的窗口返回了开启结果
                if (resultCode == Activity.RESULT_OK) {
                    // 蓝牙已开启
                    ItemSetup();
                } else {
                    // 用户没有允许打开蓝牙或出现了错误
                    Toast.makeText(getApplicationContext(), "蓝牙开启失败",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void ensureBluetoothActivated(){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private void ItemSetup() {

        bt_stage = findViewById(R.id.bt_stage);
        new_rule = findViewById(R.id.rule);

        bluetooth = findViewById(R.id.bluetooth);
        new_game = findViewById(R.id.new_game);
        illustrate = findViewById(R.id.illustrate);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.bluetooth:
                        bluetooth_click();
                        break;
                    case R.id.new_game:
                        String Msg = Constants.new_game + "|" + (new_rule.isChecked()?'b':'a');
                        sendMessage(Msg);
                        new_game_click(new_rule.isChecked());
                        break;
                    case R.id.illustrate:
                        illustrate_click();
                        break;
                }
            }
        };

        bluetooth.setOnClickListener(onClickListener);
        new_game.setOnClickListener(onClickListener);
        illustrate.setOnClickListener(onClickListener);

        // 实例化蓝牙服务类
        bluetoothService = (BluetoothService)getApplication();
        bluetoothService.setmHandler(mHandler);

        // 实例化输出缓冲区
        mOutStringBuffer = new StringBuffer("");
    }

    private void bluetooth_click(){
        ensureBluetoothActivated();

        Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }

    private void new_game_click(boolean newrule){
        if (bt_stage.getText().toString().equals("已连接")){//已经和对手连接，直接开始游戏
            Intent Intent = new Intent(getApplicationContext(), GameActivity.class);
            Intent.putExtra("newrule", newrule);
            startActivity(Intent);
        }else{//还未和对手连接，无法开始游戏
            Toast.makeText(getApplicationContext(), "尚未与任何对手连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void illustrate_click(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("规则说明")
                .setMessage(R.string.illustration)
                .setPositiveButton("懂了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    /**
     * 与设备建立连接
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     */
    private void connectDevice(Intent data) {
        // 获取MAC地址并以此获得设备对象
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // 尝试连接
        bluetoothService.connect(device);
    }

    /**
     * 发送消息
     *
     * @param message 需要发送的消息
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "尚未和任何人连接", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService.write(send);
        }
    }

    /**
     * 处理来自BluetoothChatService消息的handler
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "对手已连接", Toast.LENGTH_SHORT).show();
                            bt_stage.setText("已连接");
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            bt_stage.setText("连接中……");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            bt_stage.setText("未连接");
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    String[] Msg = readMessage.split("\\|");
                    if (Msg[0].equals(Constants.new_game)){
                        new_game_click(Msg[1].charAt(0) == 'b');
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
