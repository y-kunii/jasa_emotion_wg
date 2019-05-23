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

    RapiroController() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.d(TAG,"PairedDevices ------------------------");
        if (pairedDevices.size() > 0) {
            String address = "";
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG,"Name : " + device.getName());
                Log.d(TAG,"Address : " + device.getAddress());
                address = device.getAddress();
            }
            mBtDevice = mBluetoothAdapter.getRemoteDevice(address);
        }
        sppConnect();
    }


    public void sppConnect(){
        Log.d(TAG,"sppConnect Start");
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
        Log.d(TAG,"sppConnect End");
    }


    public static RapiroController getInstance() {
        if (instance == null) {
            instance = new RapiroController();
        }

        return instance;
    }

    public void sendCommand(String cmd){
        Log.d(TAG,"sendCommand Start");
        if (mBtSocket == null) {
            sppConnect();
        }
        try {
            //mOutput.write(cmd[0]);
            mOutput.write(cmd.getBytes());
        }catch (IOException e) {
            e.printStackTrace();
            mBtSocket = null;
        }
        Log.d(TAG,"sendCommand End");
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
}
