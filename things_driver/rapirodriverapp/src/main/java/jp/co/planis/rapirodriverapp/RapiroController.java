package jp.co.planis.rapirodriverapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import proj.iot.exchange.redge.driverlib.CommandResponseCreator;
import proj.iot.exchange.redge.driverlib.commandresponse.CommandResponse;
import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;

import static android.content.ContentValues.TAG;

/**
 * Created by azara on 2018/03/26.
 */

public class RapiroController {
    private static RapiroController instance;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBtDevice;
    BluetoothSocket mBtSocket; // BTソケット
    OutputStream mOutput; // 出力ストリーム
    boolean isBTReady = false;

    RapiroController() {
        connectToParingDevice();
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        Log.d(TAG,"PairedDevices ------------------------");
//        if (pairedDevices.size() > 0) {
//            String address = "";
//            for (BluetoothDevice device : pairedDevices) {
//                Log.d(TAG,"Name : " + device.getName());
//                Log.d(TAG,"Address : " + device.getAddress());
//                address = device.getAddress();
//            }
//            mBtDevice = mBluetoothAdapter.getRemoteDevice(address);
//            if(mBtDevice != null){
//                isBTReady = true;
//            }
//        }
//        sppConnect();
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

//    public void sppConnect(){
//        Log.d(TAG,"sppConnect Start");
//        // BTソケットのインスタンスを取得
//        try {
//            // 接続に使用するプロファイルを指定
//            mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//            // // ソケットを接続する
//            mBtSocket.connect();
//            mOutput = mBtSocket.getOutputStream(); // 出力ストリームオブジェクトを得る
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG,"sppConnect End");
//    }

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

    public void sppDisconnected(){
        Log.i(TAG,"sppDisconnected Start");
        try{
            if(mBtSocket != null) {
                mBtSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        Log.i(TAG,"sppDisconnected End");
    }

    public static RapiroController getInstance() {
        if (instance == null) {
            instance = new RapiroController();
        }

        return instance;
    }

//    public void sendCommand(String cmd){
//        Log.d(TAG,"sendCommand Start");
//        if (mBtSocket == null) {
//            sppConnect();
//        }
//        try {
//            //mOutput.write(cmd[0]);
//            mOutput.write(cmd.getBytes());
//        }catch (IOException e) {
//            e.printStackTrace();
//            mBtSocket = null;
//        }
//        Log.d(TAG,"sendCommand End");
//    }

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

    public void sendRapiroDriverInfo(Context context) {
        // ドライバの情報を収集
        CommandResponseCreator commandResponseCreator = new CommandResponseCreator(context);
        List<CommandResponse.ThingData.AvailableCommand> availableCommands = CommandCreator.createAvailableCommandList(commandResponseCreator);
        for (CommandResponse.ThingData.AvailableCommand availableCommand : availableCommands) {
            commandResponseCreator.addAvailableCommand(availableCommand);
        }

        JSONObject responseJsonObject = commandResponseCreator.getJsonObject();
        JSONArray resultsJsonArray = new JSONArray();
        resultsJsonArray.put(responseJsonObject);

        JSONObject resultsJsonObject = new JSONObject();
        try {
            resultsJsonObject.put("results", resultsJsonArray);
        } catch (JSONException e) {
            Log.e(TAG, e.getStackTrace().toString(), e);
            return;
        }

        // GatewayにJsonを送信
        AbstractConnectGatewayService.responseToGateway(context, resultsJsonObject);
    }

    public void turnOnRapiro() {
        Log.i(TAG, "######## turnOnRapiro #########");
        sendCommand("on");
    }
    public void turnOffRapiro() {
        Log.i(TAG, "######## turnOffRapiro #########");
        sendCommand("off");
    }

    public void sendCommandM1() {
        Log.i(TAG, "######## sendCommand M1 #########");
        sendCommand("M1");
    }

    public void sendCommandM2() {
        Log.i(TAG, "######## sendCommand M1 #########");
        sendCommand("M2");
    }

    public void sendCommandM3() {
        Log.i(TAG, "######## sendCommand M1 #########");
        sendCommand("M3");
    }

    public void sendCommandM4() {
        Log.i(TAG, "######## sendCommand M1 #########");
        sendCommand("M4");
    }

    public void sendCommandM5() {
        Log.i(TAG, "######## sendCommand M1 #########");
        sendCommand("M5");
    }

}
