package com.haijun.mystudyway.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.Toast;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    private BluetoothAdapter defaultAdapter;
    private Set<BluetoothDevice> bondedDevices;
    private BluetoothDevice currentDevive;
    private final String uuidString = "5b898218-3f59-11e6-beb8-9e71128cae77";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter==null){
            //不支持蓝牙
        }




    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void turnOn(View view){
        if (!defaultAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,2);
            Toast.makeText(this,"正在开启",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this,"已经开启",Toast.LENGTH_SHORT).show();
        }


    }

    public void visible(View view){
        //如果蓝牙没有打开，调用的话会自动打开蓝牙。
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //设置蓝牙的可被发现时间，0——3600，
        intent.putExtra(defaultAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivityForResult(intent,1);
        Toast.makeText(this,"蓝牙可见  ",Toast.LENGTH_SHORT).show();
    }

    public void find(View view){
        boolean startDiscovery = defaultAdapter.startDiscovery();
        //start成功,开启广播接收者
        if (startDiscovery){
            Toast.makeText(this,"查找蓝牙  ",Toast.LENGTH_SHORT).show();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(discoveryReciver,intentFilter);
        }

    }

    final BroadcastReceiver discoveryReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //收到蓝牙发现广播,每收到一个设备就收到广播
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG,"device:name->"+bluetoothDevice.getName()+",address->"+bluetoothDevice.getAddress());
               /* ParcelUuid[] uuids = bluetoothDevice.getUuids();
                if (uuids!=null){
                    Log.i(TAG,"uuids:length->"+uuids.length);
                    for (int i=0;i<uuids.length;i++){
                        Log.i(TAG,"uuid:"+uuids[i].getUuid());
                    }
                }
                else {
                    Log.i(TAG,"uuids:length->null");
                }*/
                //找的配对的蓝牙，并进行连接时断开查找蓝牙，
                /*
                Performing device discovery is a heavy procedure for the Bluetooth adapter and will consume a lot of its resources.
                 Once you have found a device to connect, be certain that you always stop discovery with cancelDiscovery()
                before attempting a connection.
                */
                //defaultAdapter.cancelDiscovery();

                Log.i(TAG,"发现目标蓝牙，准备连接，bondedDevices:"+bluetoothDevice.getName());
                currentDevive = bluetoothDevice;
                defaultAdapter.cancelDiscovery();
                new AcceptThread().start();  //开启服务端
				                /*if (bluetoothDevice.getName().equals("Ctyon-A9")){
                    Log.i(TAG,"发现目标蓝牙，准备连接，bondedDevices:"+bluetoothDevice.getName());
                    currentDevive = bluetoothDevice;

                    defaultAdapter.cancelDiscovery();
                    new AcceptThread().start();  //开启服务端
                }*/
            }
        }
    };


    public void list(View view){
        bondedDevices = defaultAdapter.getBondedDevices();
        Iterator<BluetoothDevice> iterator = bondedDevices.iterator();
        while (iterator.hasNext()){
            BluetoothDevice next = iterator.next();
            ParcelUuid[] uuids = next.getUuids();
            Log.i(TAG,next.getName()+",  uuids:"+uuids.length);
            for (int i=0;i<uuids.length;i++){
                Log.i(TAG,"uuid:"+uuids[i].getUuid());
            }
            Log.i(TAG,"bondedDevices:"+next.getName()+":"+next.getAddress());
        }
    }

    public void turnOff(View view){
        defaultAdapter.disable();
        Toast.makeText(this,"关闭蓝牙",Toast.LENGTH_SHORT).show();
    }


    public void serverStart(View view){

    }

    public void clientStart(View view){
        bondedDevices = defaultAdapter.getBondedDevices();
        Iterator<BluetoothDevice> iterator = bondedDevices.iterator();
        BluetoothDevice next = iterator.next();
        Log.i(TAG,"客户端连接蓝牙：名称-》"+next.getName());

        new ConnectThread(next).start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2){
            if (resultCode==Activity.RESULT_OK){
                Toast.makeText(this,"开启成功",Toast.LENGTH_SHORT).show();
            }
            else if (resultCode== Activity.RESULT_CANCELED){
                Toast.makeText(this,"开启失败",Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode==1){
            //返回正确的话，结果码是请求被发现的时间
            if (resultCode==300){
                Toast.makeText(this,"允许被发现",Toast.LENGTH_SHORT).show();
            }
            else if (resultCode== Activity.RESULT_CANCELED){
                Toast.makeText(this,"拒绝被发现",Toast.LENGTH_SHORT).show();
            }
        }
    }

    class AcceptThread extends Thread{
        private final BluetoothServerSocket bluetoothServerSocket;

        AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                BluetoothServerSocket name = defaultAdapter.listenUsingRfcommWithServiceRecord("name", UUID.fromString(uuidString));
                tmp = name;
                Log.i(TAG,"服务端新建ServerSocket，蓝牙名称为："+currentDevive.getName());

            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothServerSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket acceptSocket;
            while (true){
                try {
                    acceptSocket = bluetoothServerSocket.accept();

                } catch (IOException e) {
                    break;
                }
                if (acceptSocket!=null){
                    Log.i(TAG,"服务端收到accept()  ");
                    ConnectDataThread connectDataThread = new ConnectDataThread(acceptSocket);

                    connectDataThread.write("哈哈，这是服务器发送的数据".getBytes());
                    connectDataThread.start();
                    try {
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void cancel(){
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //a fictional method in the application that will initiate the thread for transferring data
    private void manageConnectedSocket(BluetoothSocket acceptSocket) {

    }

    class ConnectThread extends Thread{
        BluetoothSocket bluetoothSocket;
        public ConnectThread(BluetoothDevice bluetoothDevice) {
            BluetoothSocket tem=null;
            try {
                tem = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuidString));
                Log.i(TAG,"客户端新建BluetoothSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }

            bluetoothSocket  = tem;
        }

        @Override
        public void run() {
            defaultAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
                Log.i(TAG,"客户端connect()");
            } catch (IOException e) {
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    return;
                }
            }

            Log.i(TAG,"客户端链接成功");
            ConnectDataThread connectDataThread = new ConnectDataThread(bluetoothSocket);
            connectDataThread.write("客户端发送的数据".getBytes());
            connectDataThread.start();
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class ConnectDataThread extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ConnectDataThread(BluetoothSocket bluetoothSocket){
            this.bluetoothSocket = bluetoothSocket;
            InputStream teminput = null;
            OutputStream temout = null;
            try {
                teminput = bluetoothSocket.getInputStream();
                temout = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG,"ConnectDataThread线程创建");
            inputStream = teminput;
            outputStream = temout;

        }

        @Override
        public void run() {
            Log.i(TAG,"ConnectDataThread线程run()");
            byte[] bytes = new byte[1024];
            while (true){
                try {
                    inputStream.read(bytes);
                    Log.i(TAG,"inputStream.read:"+bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
                Log.i(TAG,"ConnectDataThread线程写入数据");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
