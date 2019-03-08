package jp.co.planis.sampledriverapp.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import jp.co.planis.sampledriverapp.service.SampleSendDriverInfoIntentService;

/**
 * ドライバアプリの情報送信のブロードキャストを受け取るレシーバのサンプル
 * Created by y_akimoto on 2016/09/21.
 */
public class SampleCollectInfoBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = SampleCollectInfoBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        // 受け取ったIntentの処理をIntentServiceで行う
        ComponentName componentName = new ComponentName(context.getPackageName(), SampleSendDriverInfoIntentService.class.getName());

        // サービスの起動。処理中スリープを制御
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
    }
}
