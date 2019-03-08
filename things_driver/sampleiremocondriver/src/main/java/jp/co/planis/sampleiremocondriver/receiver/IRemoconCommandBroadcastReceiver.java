package jp.co.planis.sampleiremocondriver.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import jp.co.planis.sampleiremocondriver.service.IRemoconGatewayToDriverIntentService;

/**
 * ゲートウェイからのコマンドを受信するレシーバ
 * Created by y_akimoto on 2016/12/15.
 */
public class IRemoconCommandBroadcastReceiver extends WakefulBroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        // 受け取ったIntentの処理をIntentServiceで行う
        ComponentName componentName = new ComponentName(context.getPackageName(), IRemoconGatewayToDriverIntentService.class.getName());

        // サービスの起動。処理中スリープを制御
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
    }
}
