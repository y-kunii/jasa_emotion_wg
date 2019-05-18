package com.example.sleeptechui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class RapiroManager {
    private String TAG = "RapiroManager";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBtDevice;
    private BluetoothSocket mBtSocket; // BTソケット
    private OutputStream mOutput; // 出力ストリーム
    private boolean isBTReady = false;

    RapiroManager(){
        connectToParingDevice();
    }

    public void connectToParingDevice(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Log.i(TAG,"Get BluetoothAdapter failed!!");
            return;
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.i(TAG,"PairedDevices ------------------------");
        if (pairedDevices.size() > 0) {
            String address = "";
            for (BluetoothDevice device : pairedDevices) {
                Log.i(TAG,"Name : " + device.getName());
                Log.i(TAG,"Address : " + device.getAddress());
                address = device.getAddress();
            }
            mBtDevice = mBluetoothAdapter.getRemoteDevice(address);
            sppConnect();
        } else {
            Log.i(TAG,"PairedDevice is none");
        }
    }

    private void sppConnect(){
        Log.i(TAG,"sppConnect Start");
        // BTソケットのインスタンスを取得
        try {
            // 接続に使用するプロファイルを指定
            mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            // // ソケットを接続する
            mBtSocket.connect();
            mOutput = mBtSocket.getOutputStream(); // 出力ストリームオブジェクトを得る
            if(mBtSocket.isConnected() && mOutput != null){
                isBTReady = true;
                Log.i(TAG,"Bluetooth Connect is Ready!!!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"sppConnect End");
    }

    public void sendCommand(String cmd){
        Log.i(TAG,"sendCommand Start");
        if(!isBTReady){
            Log.i(TAG,"Bluetooth Connect is not Ready!!!");
            return;
        }
        if (mBtSocket == null) {
            sppConnect();
        }
        try {
            //mOutput.write(cmd[0]);
            Log.i(TAG,"send ->[" + cmd.getBytes("UTF-8") + "]");
            mOutput.write(cmd.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            mBtSocket = null;
        }
        Log.i(TAG,"sendCommand End");
    }


}
