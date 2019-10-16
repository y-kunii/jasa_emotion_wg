package jp.co.planis.rapirodriverapp.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import proj.iot.exchange.redge.driverlib.ApplicationInfoManager;
import proj.iot.exchange.redge.driverlib.CommandResponseCreator;
import proj.iot.exchange.redge.driverlib.commandresponse.CommandResponse;
import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;
import jp.co.planis.rapirodriverapp.CommandCreator;
import jp.co.planis.rapirodriverapp.RapiroController;

/**
 * ゲートウェイアプリからのコマンドを受信して結果を返却するサービスのサンプル
 * Created by y_akimoto on 2016/09/21.
 */
public class RapiroGatewayToDriverIntentService extends AbstractConnectGatewayService {

    private static final String TAG = RapiroGatewayToDriverIntentService.class.getSimpleName();
    private static final String thing_uuid = "21fa027e-923f-4207-ae8c-178f520d84ad";

    public RapiroGatewayToDriverIntentService() {
        super(RapiroGatewayToDriverIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Bundle extras = intent.getExtras();
        Log.d(TAG, extras.getString("json"));

        try {
            // 機器IDが一致してなければ実行しない
            JSONObject jsonObject = new JSONObject(extras.getString("json"));
            Log.e(TAG, jsonObject.getString("thing_uuid"));
            //if (jsonObject.isNull("thing_uuid") || !jsonObject.getString("thing_uuid").equals(ApplicationInfoManager.getUUID(this))) {
            if (jsonObject.isNull("thing_uuid") ||
                !jsonObject.getString("thing_uuid").equals(thing_uuid) ||
                !jsonObject.getString("thing_uuid").equals(ApplicationInfoManager.getUUID(this))) {
                return;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getStackTrace().toString(), e);
            return;
        }


        // JSON形式のコマンドをオブジェクトに変換
        CommandResponseCreator commandResponseCreator = new CommandResponseCreator(this, extras.getString("json"));
        CommandResponse commandResponse = commandResponseCreator.getCommandResponse();

        // HueControllerの初期化
        // RapiroController.getInstance().initialize(this);

        // コマンド単位で処理を実行
        if (commandResponse.command != null && commandResponse.command.length > 0) {
            for (int i = 0; i < commandResponse.command.length; i++) {
                // コマンドをHueコントローラで実行できる形式に変換
                CommandCreator.RapiroCommand rapiroCommand = CommandCreator.convertRapiroCommand(commandResponse.command[i]);
                if (rapiroCommand == null) {
                    Log.e(TAG, "convert hue controller command:" + commandResponse.command[i]);
                } else {
                    // Hueコントローラの指令を実行
                    rapiroCommand.execute();
                    Log.d(TAG, "############## execute convert command ##############");

                    //TODO: 結果をセットする
                }
            }
        }

        // 結果をゲートウェイアプリに返却する
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
    }
}
