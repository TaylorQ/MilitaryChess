package com.example.taylorq.militarychess;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    /**
     * 返回的extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    /**
     * 本地蓝牙适配器
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * 新发现的设备列表的adapter
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        // 设置用户按后退键返回的值
        setResult(Activity.RESULT_CANCELED);

        // 实例化列表的适配器
        mNewDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.device_name);

        // 列表控件初始化
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 注册Receiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // 获取本地蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Override
    protected void onResume(){
        super.onResume();

        doDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 确认扫描已关闭
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // 注销Receiver
        this.unregisterReceiver(mReceiver);
    }

    /**
     * 开始扫描
     */
    private void doDiscovery() {

        // 更改标题
        setProgressBarIndeterminateVisibility(true);
        setTitle("扫描中……");

        // 如果已经在扫描了，先关闭扫描
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // 开始扫描
        mBtAdapter.startDiscovery();
    }

    /**
     * ListViews内每个Item的OnItemClickListener
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 既然选择了设备，那就终止扫描以节约资源
            mBtAdapter.cancelDiscovery();

            // 获取Item内字符串的后17位，那是设备MAC地址
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // 创建用于返回的Intent
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // 设置返回结果并结束这个Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    /**
     * 一个BroadcastReceiver用于接收找到新设备的消息并在扫描结束后更改标题
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 扫描发现了设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从Intent获取扫描到的设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {//扫描结束
                setProgressBarIndeterminateVisibility(false);
                setTitle("请选择一个设备");
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "没有发现任何设备";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
