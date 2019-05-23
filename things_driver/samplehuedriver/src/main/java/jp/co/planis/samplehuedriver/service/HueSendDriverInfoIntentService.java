package jp.co.planis.samplehuedriver.service;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import proj.iot.exchange.redge.driverlib.CommandResponseCreator;
import proj.iot.exchange.redge.driverlib.commandresponse.CommandResponse;
import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;
import jp.co.planis.samplehuedriver.CommandCreator;

/**
 * ドライバアプリの情報をゲートウェイに送信するためのサービスクラス
 * Created by y_akimoto on 2016/09/21.
 */
public class HueSendDriverInfoIntentService extends AbstractConnectGatewayService {

    private static final String TAG = HueSendDriverInfoIntentService.class.getSimpleName();

    public HueSendDriverInfoIntentService() {
        super(HueSendDriverInfoIntentService.class.getSimpleName());
    }

    public HueSendDriverInfoIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        // ドライバの情報を収集
        CommandResponseCreator commandResponseCreator = new CommandResponseCreator(this);
        List<CommandResponse.ThingData.AvailableCommand> availableCommands = CommandCreator.createAvailableCommandList(commandResponseCreator);
        for (CommandResponse.ThingData.AvailableCommand availableCommand : availableCommands) {
            commandResponseCreator.addAvailableCommand(availableCommand);
        }

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
