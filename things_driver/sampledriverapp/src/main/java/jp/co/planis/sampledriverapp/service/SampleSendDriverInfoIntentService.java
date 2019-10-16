package jp.co.planis.sampledriverapp.service;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import proj.iot.exchange.redge.driverlib.CommandResponseCreator;
import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;

/**
 * ドライバアプリの情報をゲートウェイに送信するためのサービスクラスのサンプル
 * Created by y_akimoto on 2016/09/21.
 */
public class SampleSendDriverInfoIntentService extends AbstractConnectGatewayService {

    private static final String TAG = SampleSendDriverInfoIntentService.class.getSimpleName();

    public SampleSendDriverInfoIntentService() {
        super(SampleSendDriverInfoIntentService.class.getSimpleName());
    }

    public SampleSendDriverInfoIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        // ドライバの情報を収集
        CommandResponseCreator commandResponseCreator = new CommandResponseCreator(this);
        JSONObject responseJsonObject = commandResponseCreator.getJsonObject();

        // _/_/_/_/_/_/_/_/_/_/_/_/
        // ココでいろいろな情報をセット
        // _/_/_/_/_/_/_/_/_/_/_/_/

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
