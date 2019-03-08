package jp.co.planis.rapirodriverapp.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import jp.co.planis.rapirodriverapp.service.RapiroDriverInfoIntentService;

/**
 * ドライバアプリの情報送信のブロードキャストを受け取るレシーバのサンプル
 * Created by y_akimoto on 2016/09/21.
 */
public class RapiroCollectInfoBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = RapiroCollectInfoBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        // 受け取ったIntentの処理をIntentServiceで行う
        ComponentName componentName = new ComponentName(context.getPackageName(), RapiroDriverInfoIntentService.class.getName());

        // サービスの起動。処理中スリープを制御
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
    }
}
