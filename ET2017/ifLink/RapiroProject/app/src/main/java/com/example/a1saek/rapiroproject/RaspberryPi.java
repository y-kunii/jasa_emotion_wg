package com.example.a1saek.rapiroproject;

import jp.co.toshiba.iflink.imsif.BaseDevice;
import jp.co.toshiba.iflink.imsif.BaseIms;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static android.R.attr.name;
import static android.content.ContentValues.TAG;

public class RaspberryPi extends BaseDevice {

    public  String TAG = "RaspberryPi";
    private static final String RAPIRO_DEVICE_NAME = "rapiro-demo";
    private static final String RAPIRO_SERIAL = "any";
    private static final String RAPIRO_SCHEMA_NAME = "default";
    private static final String RAPIRO_SCHEMA = "";
//    private static final String RAPIRO_COOKIE = BaseIms.EPA_COOKIE_KEY_TYPE + "=" + BaseIms.EPA_COOKIE_TYPE_VALUE_JOB +
//            ";, " + BaseIms.EPA_COOKIE_KEY_DEVICE + "=" + RAPIRO_DEVICE_NAME +
//            ";, " + BaseIms.EPA_COOKIE_KEY_ADDRESS + "=" + BaseIms.EPA_COOKIE_VALUE_ANY;
    /* EPAに登録するコールバック関数用のCookie設定 */
    private static final String RAPIRO_COOKIE = BaseIms.EPA_COOKIE_KEY_TYPE + "=" + BaseIms.EPA_COOKIE_TYPE_VALUE_JOB
                + BaseIms.COOKIE_DELIMITER + BaseIms.EPA_COOKIE_KEY_DEVICE + "=" + RAPIRO_DEVICE_NAME
                + BaseIms.COOKIE_DELIMITER + BaseIms.EPA_COOKIE_KEY_ADDRESS + "=any";
    private static final String RAPIRO_ADDRESS = "1c:15:1f:d2:da:bf";
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBtDevice;
    BluetoothSocket mBtSocket; // BTソケット
    OutputStream mOutput; // 出力ストリーム
    private DeviceState mCurrentState;

    RaspberryPi(BaseIms tms) {
        super(tms);
        Log.i(TAG, "RaspberryPi!!!");
        //TODO:デバイスネーム Fix
        mDeviceName = RAPIRO_DEVICE_NAME;
        //TODO:シリアル Fix
        mDeviceSerial = RAPIRO_SERIAL;
        //TODO:スキーマネーム Fix
        mSchemaName = RAPIRO_SCHEMA_NAME;
        //TODO:スキーマ Fix
        mSchema = "\"<schema name=\"" + mSchemaName + "\">\n\"" +
                "\"<property name=\"devicename\" type=\"string\" />\n\"" +
                "\"<property name=\"動作パターン\" type=\"string\" />\n\"" +
                "\"</schema>\"";
        //<property name="動作パターン" datatype="string" type="select" option="動作1,動作2,動作3,動作4" key="reaction" value="1,2,3,4" default="1" />
        //mSchema = RAPIRO_SCHEMA;
        mAssetName = RAPIRO_DEVICE_NAME;
        //TODO:クッキー Fix
        mCookie = RAPIRO_COOKIE;
        /* EPAに登録するアセット名設定 */
        mAssetName = mDeviceName;

        setDeviceName(mDeviceName);
        setDeviceSerial(mDeviceSerial);
        setSchema(mSchema);
        setSchemaName(RAPIRO_SCHEMA_NAME);
        setCookie(RAPIRO_COOKIE);
        mCurrentState = new DeviceState();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
        }
        //sppConnect();
    }

    public String getDeviceAddress(){
        return RAPIRO_ADDRESS;
    }

    public void sppConnect(){
        Log.i(TAG,"sppConnect Start");
        // BTソケットのインスタンスを取得
        try {
            // 接続に使用するプロファイルを指定
            mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            // // ソケットを接続する
            mBtSocket.connect();
            mOutput = mBtSocket.getOutputStream(); // 出力ストリームオブジェクトを得る
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"sppConnect End");
    }

    public void sendCommand(String cmd){
        Log.i(TAG,"sendCommand Start");
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

    public boolean onJob(HashMap<String, Object> map) {
        Log.i(TAG, "onJob Start!!!");
        //TODO:Jobハンドリング処理
        String control = String.valueOf(map.get("control"));
        String name = String.valueOf(map.get(BaseIms.EPA_DATA_DEVICE_KEY));
        String serial = String.valueOf(map.get(BaseIms.EPA_DATA_ADDRESS_KEY));
        Log.i(TAG, "control = ["+ control + "]");
        Log.i(TAG, "name = ["+ name + "]");
        Log.i(TAG, "serial = ["+ serial + "]");
        /** control 名のチェック */
        if (control.equalsIgnoreCase("speak")) {
            /** 対象デバイス情報の取得 */
//            String name = String.valueOf(map.get(BaseIms.EPA_DATA_DEVICE_KEY));
//            String serial = String.valueOf(map.get(BaseIms.EPA_DATA_ADDRESS_KEY));
            if (isMyJob(name, serial)) {
                /** 自分への JOB なら実行する */
                /** control パラメータの取得 */
                String param = String.valueOf(map.get("reaction"));
                Log.i(TAG, param);
                doExecJob(param);
                return true;
            }
        }
        Log.i(TAG, "onJob End!!!");
        return false;
    }

    private boolean isMyJob(String name, String serial) {
        //TODO: Jobが自分のものか判断
        return true;
    }

    private void doExecJob(String param) {
        Log.i(TAG,"doExecJob start ->[" + param +"]");
        if(!param.isEmpty()){
            String cmd = "";
            if(param.equals("1")){
                cmd = "M1";
            } else if(param.equals("2")){
                cmd = "M2";
            } else if(param.equals("3")){
                cmd = "M3";
            } else if(param.equals("4")){
                cmd = "M4";
            } else {
                //do nothing
            }
            sendToRapiro(cmd);
        } else {
            Log.i(TAG,"no thing to do");
        }
    }

    @Override
    public void onTimeout(int i) {

    }

    public void notifyEpaServiceConnected(){
        Log.i(TAG,"notifyEpaServiceConnected");
        try {
            setDeviceStatus(BaseDevice.DEV_STATE_CONNECT_DEVICE);
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

    private void stateAction(String param){
        try{
            if (param.equals("1")){
                sendCommand("M1");
            } else if (param.equals("2")){
                sendCommand("M2");
            } else if (param.equals("3")){
                sendCommand("M3");
            } else if (param.equals("4")){
                sendCommand("M4");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToRapiro(String str) {
        Log.i(TAG, "######## sendToRapiro #########");
        sendCommand(str);
    }


    public void turnOnRapiro() {
        Log.i(TAG, "######## turnOnRapiro #########");
        sendCommand("on");
    }
    public void turnOffRapiro() {
        Log.i(TAG, "######## turnOffRapiro #########");
        sendCommand("off");
    }
    /**
     * DEV_STATE_OFFの時にActivate開始検知した時の処理.
     */
    public void activateDevice() {
        Log.i(TAG, "######## activateDevice #########");
        try {
            /* デバイス状態更新 */
            setDeviceStatus(DEV_STATE_ACTIVATE);
        } catch (RemoteException | IllegalStateException e) {
            e.printStackTrace();
        }
    }
    /**
     * DEV_STATE_ACTIVATEの時にActivate完了検知した時の処理.
     */
    public void activatedDevice() {
        Log.i(TAG, "######## activatedDevice #########");
        try {
            /* デバイス状態更新 */
            setDeviceStatus(DEV_STATE_IDLE);
        } catch (RemoteException | IllegalStateException e) {
            e.printStackTrace();
        }
    }
    /**
     * DEV_STATE_IDLEの時にデバイス開始要求の処理.
     */
    @Override
    public boolean startDevice() {
        Log.i(TAG, "[DEV_STATE_RUN]");
        try {
            /* デバイス状態更新 */
            setDeviceStatus(DEV_STATE_RUN);
        } catch (RemoteException | IllegalStateException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * DEV_STATE_RUNの時にデバイス停止要求の処理.
     */
    @Override
    public boolean stopDevice() {
        Log.i(TAG, "[DEV_STATE_IDLE]");
        try {
            /* デバイス状態更新 */
            setDeviceStatus(DEV_STATE_IDLE);
        } catch (RemoteException | IllegalStateException e) {
            e.printStackTrace();
        }
        return true;
    }
}
