package jp.co.planis.sampleiremocondriver.service;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import proj.iot.exchange.redge.driverlib.CommandResponseCreator;
import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;
import jp.co.planis.sampleiremocondriver.CommandManager;

/**
 * ドライバアプリの情報をゲートウェイに送信するためのサービスクラス
 * Created by y_akimoto on 2016/12/15.
 */
public class IRemoconSendDriverInfoIntentService extends AbstractConnectGatewayService {

    private static final String TAG = IRemoconSendDriverInfoIntentService.class.getSimpleName();

    public IRemoconSendDriverInfoIntentService() {
        super(IRemoconSendDriverInfoIntentService.class.getSimpleName());
    }

    public IRemoconSendDriverInfoIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        // ドライバの情報を収集
        CommandResponseCreator commandResponseCreator = new CommandResponseCreator(this);
        // 実行可能なコマンドを取得してセット
        CommandManager.getInstance().initialize(this);
        CommandManager.getInstance().setAvailableCommandList(commandResponseCreator);

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
