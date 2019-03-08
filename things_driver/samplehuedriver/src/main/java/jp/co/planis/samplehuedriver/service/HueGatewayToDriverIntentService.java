package jp.co.planis.samplehuedriver.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jp.co.planis.iotgatewaylib.ApplicationInfoManager;
import jp.co.planis.iotgatewaylib.CommandResponseCreator;
import jp.co.planis.iotgatewaylib.commandresponse.CommandResponse;
import jp.co.planis.iotgatewaylib.service.AbstractConnectGatewayService;
import jp.co.planis.samplehuedriver.CommandCreator;
import jp.co.planis.samplehuedriver.HueController;

/**
 * ゲートウェイアプリからのコマンドを受信して結果を返却するサービス
 * Created by y_akimoto on 2016/09/21.
 */
public class HueGatewayToDriverIntentService extends AbstractConnectGatewayService {

    private static final String TAG = HueGatewayToDriverIntentService.class.getSimpleName();

    public HueGatewayToDriverIntentService() {
        super(HueGatewayToDriverIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Bundle extras = intent.getExtras();
        Log.d(TAG, extras.getString("json"));

        try {
            // 機器IDが一致してなければ実行しない
            JSONObject jsonObject = new JSONObject(extras.getString("json"));
            if (jsonObject.isNull("thing_uuid") || !jsonObject.getString("thing_uuid").equals(ApplicationInfoManager.getUUID(this))) {
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
        HueController.getInstance().initialize(this);

        // コマンド単位で処理を実行
        if (commandResponse.command != null && commandResponse.command.length > 0) {
            for (int i = 0; i < commandResponse.command.length; i++) {
                // コマンドをHueコントローラで実行できる形式に変換
                CommandCreator.HueCommand hueCommand = CommandCreator.convertHueCommand(commandResponse.command[i]);
                if (hueCommand == null) {
                    Log.e(TAG, "convert hue controller command:" + commandResponse.command[i]);
                } else {
                    // Hueコントローラの指令を実行
                    hueCommand.execute();

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

        // GatewayにJsonを送信
        responseToGateway(resultsJsonObject);
    }
}
