package com.example.taylorq.militarychess;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

public class BluetoothService extends Application{
    //Log标签
    private static final String TAG = "BluetoothService";


    // 本应用的唯一UUID
    private static final UUID service_uuid =
            UUID.fromString("c736f65a-37e3-4921-8723-a2e00cb2859e");

    // 成员变量
    private final BluetoothAdapter mAdapter;
    private Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    // 状态标识
    public static final int STATE_NONE = 0;       // 未连接
    public static final int STATE_LISTEN = 1;     // 监听连接请求
    public static final int STATE_CONNECTING = 2; // 正试图建立连接
    public static final int STATE_CONNECTED = 3;  // 已连接


    public BluetoothService() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
    }

    public void setmHandler(Handler handler){
        mHandler = handler;
        updateUserInterfaceTitle();
    }

    /**
     * 更新UI界面，通知UI界面当前的状态
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    /**
     * 返回当前状态
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * 启动ConnectedThread
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // 关闭ConnectThread
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 关闭现有的ConnectedThread
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 关闭AcceptThread
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        // 启动ConnectedThread
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // 更新UI界面
        updateUserInterfaceTitle();
    }

    /**
     * 关闭所有线程
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        mState = STATE_NONE;
        // 更新UI界面
        updateUserInterfaceTitle();
    }

    /**
     * 以非加锁的方式向ConnectedThread写入输出消息
     */
    public void write(byte[] out) {
        // 一个临时实例
        ConnectedThread r;
        // 在加锁状态下确定状态并获得ConnectedThread的引用
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // 已非加锁方式写入
        r.write(out);
    }

    /**
     * 连接失败并将该结果返回到UI界面
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * 连接断开
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * 监听连接请求的线程
     */
    private class AcceptThread extends Thread{
        // 本地server socket
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // 创建一个监听socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord("myServerSocket", service_uuid);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread.listen() failed", e);
            }
            mServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // 当状态不是已连接，持续监听连接请求
            while (mState != STATE_CONNECTED) {
                try {
                    // 开始监听，直到成功接收一个连接请求或返回一个exception
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread.accept() failed", e);
                    break;
                }

                // 收到了一个连接请求
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //当前状态正常，建立连接
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //当前状态处于还未准备好连接或已有连接，终止新的连接请求
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread");

        }

        //关闭这个线程
        public void cancel() {
            Log.d(TAG, "AcceptThread cancel " + this);
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread.close() of server failed", e);
            }
        }
    }

    /**
     * 启动ConnectThread以连接至指定设备
     *
     * @param device 要连接的设备
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // 如果又正在发起的连接，关闭并置空ConnectThread
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // 关闭现有的连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 向ConnectThread传入设备并启动
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        // 更新UI界面
        updateUserInterfaceTitle();
    }

    /**
     * 用于发起连接请求的线程。
     * 线程会一次走完，连接成功或失败。
     */
    private class ConnectThread extends Thread{
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
            BluetoothSocket tmp = null;

            // 用device打开一个连接
            try {
                tmp = device.createRfcommSocketToServiceRecord(service_uuid);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread create() failed", e);
            }
            mSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // 关闭扫描以节约资源
            mAdapter.cancelDiscovery();

            // 尝试连接
            try {
                // 这个方法会成功或返回一个exception
                mSocket.connect();
            } catch (IOException e) {
                // 关闭这个sokcet
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() ConnectThread socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // 重置这个线程
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // 启动连接
            connected(mSocket, mDevice);
        }

        /**
         * 关闭这个线程
         */
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * 已连接的线程
     */
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // 获取输入输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // 当连接时持续监听输入流
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * 输出一个byte串
         */
        public void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        /**
         * 关闭这个线程
         */
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}