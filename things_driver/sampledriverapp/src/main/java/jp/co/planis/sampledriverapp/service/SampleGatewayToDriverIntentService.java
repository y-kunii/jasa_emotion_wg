package jp.co.planis.sampledriverapp.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;

/**
 * ゲートウェイアプリからのコマンドを受信して結果を返却するサービスのサンプル
 * Created by y_akimoto on 2016/09/21.
 */
public class SampleGatewayToDriverIntentService extends AbstractConnectGatewayService {

    private static final String TAG = SampleGatewayToDriverIntentService.class.getSimpleName();

    public SampleGatewayToDriverIntentService() {
        super(SampleGatewayToDriverIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Bundle extras = intent.getExtras();
        Log.d(TAG, extras.getString("json"));

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        //
        // 何らかの処理
        try {
            Thread.sleep(3000);
        } catch (Exception e){}
        //
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

        // 結果をゲートウェイアプリに返却する
        try {
            JSONObject resultJsonObject = new JSONObject("{\"result\":\"ok\"}");
            responseToGateway(resultJsonObject);
        } catch (JSONException e) {
            Log.d(TAG, e.getStackTrace().toString(), e);
        }
    }
}
