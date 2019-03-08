package jp.co.planis.samplehuedriver.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import jp.co.planis.samplehuedriver.HueController;
import jp.co.planis.samplehuedriver.service.HueSendDriverInfoIntentService;

/**
 * ドライバアプリの情報送信のブロードキャストを受け取るレシーバ
 * Created by y_akimoto on 2016/09/21.
 */
public class HueCollectInfoBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = HueCollectInfoBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        // 受け取ったIntentの処理をIntentServiceで行う
        ComponentName componentName = new ComponentName(context.getPackageName(), HueSendDriverInfoIntentService.class.getName());

        // サービスの起動。処理中スリープを制御
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);

        HueController.getInstance().sendDriverInfo(context);
    }
}
