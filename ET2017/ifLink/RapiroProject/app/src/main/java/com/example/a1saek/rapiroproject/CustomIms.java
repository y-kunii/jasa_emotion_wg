package com.example.a1saek.rapiroproject;

import jp.co.toshiba.iflink.epaapi.EPADevice;
import jp.co.toshiba.iflink.imsif.BaseIms;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

public class CustomIms extends BaseIms {
    String TAG = "CustomTMS";
    RaspberryPi mRaspberryPi;
    BluetoothAdapter mBluetoothAdapter;
    long mHealthCheckInterval = 1000;
    BluetoothDevice mBtDevice;
    BluetoothSocket mBtSocket; // BTソケット
    OutputStream mOutput; // 出力ストリーム
    byte[] command;
    private Timer timer = null;
    Handler handler = new Handler();

    public CustomIms(){
        super("CustomIms");
        Log.i(TAG,"CustomIms");
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate!!!");
        super.onCreate();
        mRaspberryPi = new RaspberryPi(this);
        mSensorList.add(mRaspberryPi);
    }

    /* デバイス開始要求. */
    @Override
    protected void onDeviceStart(final String s, final String s1) {
        Log.i(TAG, "onDeviceStart(" + s + ")");
        /* CustomDeviceクラスのデバイス開始を呼び出す */
        mRaspberryPi.startDevice();
    }

    /* デバイス停止要求. */
    @Override
    protected void onDeviceStop(final String s, final String s1) {
        Log.i(TAG, "onDeviceStop(" + s + ")");
        /* CustomDeviceクラスのデバイス停止を呼び出す */
        mRaspberryPi.stopDevice();
    }

    @Override
    public void onActivationResult(boolean result, EPADevice device){
        Log.i(TAG,"onActivationResult!!!");
        if(result) {
            try {
                if (!createDevices()) {
                    Log.i(TAG,"createDevices failed!!!");
                    return;
                }
                /* CustomDeviceのActivate開始検知を呼び出す */
                mRaspberryPi.activateDevice();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand!!!");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onEpaServiceConnected() {
        Log.i(TAG,"onEpaServiceConnected!!!");
        //デバイス登録
        try {
            if (!createImsDevice()) {
                Log.i(TAG,"createDevice Error!!!");
                return;
            }
            Log.i(TAG,"createDevice Success");

            // コールバック登録
            registerCallback();
        }  catch (RemoteException | IllegalStateException e) {
            Log.i(TAG,"createDevice failed");
            e.printStackTrace();
        }
        Log.i(TAG,"mHealthCheckInterval Start");
        /* ヘルスチェック開始 */
        //startHealthCheck(mHealthCheckInterval);
        int command = 'a';
        //sendCommand(command);

        mRaspberryPi.notifyEpaServiceConnected();
    }

    @Override
    public void onEpaServiceDied(){
        Log.i(TAG,"onEpaServiceDied");
    }

    @Override
    public void onEpaEvent(HashMap map){
        Log.i(TAG,"onEpaEvent");
        execJob(map);
    }

    @Override
    public void onDeviceActivationResult(boolean result, EPADevice device){
        Log.i(TAG,"result = " + result + "");
        if (result) {
            /* 結果がTrueの場合 */
            /* CustomDeviceのActivate完了検知を呼び出す */
            mRaspberryPi.activatedDevice();
            try {
                /* BaseImsクラスのデバイス状態通知を呼び出す(IMS_STATE_START) */
                setDeviceStatus(mDeviceName, mDeviceSerial, IMS_STATE_START, "");
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onEpaServiceDisconnected() {
        Log.i(TAG,"onEpaServiceDisconnected Start");
        //TODO: 切断処理
    }

    public void sendCommand(int cmd){
        Log.i(TAG,"sendCommand Start");
        // BTソケットのインスタンスを取得
        try {
            // 接続に使用するプロファイルを指定
            mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            // // ソケットを接続する
            mBtSocket.connect();
            mOutput = mBtSocket.getOutputStream(); // 出力ストリームオブジェクトを得る
            //mOutput.write(cmd[0]);
            mOutput.write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"sendCommand End");
    }

    @Override
    public void onStopIMS () {
        /* デバイス停止 */
        if (stopDevices()) {
            try {
                /* デバイス登録解除 */
                deleteDevices();
                /* IMSデバイス登録解除 */
                deleteImsDevice();
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // ソケットを閉じる
        try {
            mBtSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
