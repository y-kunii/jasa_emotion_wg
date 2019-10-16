package jp.co.planis.sampleiremocondriver.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import proj.iot.exchange.redge.driverlib.ApplicationInfoManager;
import proj.iot.exchange.redge.driverlib.CommandResponseCreator;
import proj.iot.exchange.redge.driverlib.commandresponse.CommandResponse;
import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;
import jp.co.planis.sampleiremocondriver.CommandManager;
import jp.co.planis.sampleiremocondriver.Constants;

/**
 * ゲートウェイアプリからのコマンドを受信して結果を返却するサービス
 * Created by y_akimoto on 2016/12/15.
 */
public class IRemoconGatewayToDriverIntentService extends AbstractConnectGatewayService {

    private static final String TAG = IRemoconGatewayToDriverIntentService.class.getSimpleName();

    public IRemoconGatewayToDriverIntentService() {
        super(IRemoconGatewayToDriverIntentService.class.getSimpleName());
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
                Log.d(Constants.LOG_TAG, "thing_uuid not match");
                return;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getStackTrace().toString(), e);
            return;
        }

        // JSON形式のコマンドをオブジェクトに変換
        CommandResponseCreator commandResponseCreator = new CommandResponseCreator(this, extras.getString("json"));
        CommandResponse commandResponse = commandResponseCreator.getCommandResponse();

        // コマンド単位で処理を実行
        Log.d(Constants.LOG_TAG, "Execute commands");
        CommandManager.getInstance().initialize(this);
        if (commandResponse.command != null && commandResponse.command.length > 0) {

            for (int i = 0; i < commandResponse.command.length; i++) {
                CommandResponse.Command command = commandResponse.command[i];
                // コマンドを実行
                CommandManager.getInstance().executeCommand(command);
            }
        }

        // 結果をゲートウェイアプリに返却する
        try {
            JSONObject resultJsonObject = new JSONObject("{\"result\":\"ok\"}");
            responseToGateway(resultJsonObject);
        } catch (JSONException e) {
            Log.d(TAG, e.getStackTrace().toString(), e);
        }
    }
}
