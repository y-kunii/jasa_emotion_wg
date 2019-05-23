package jp.co.planis.samplehuedriver;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Random;

import proj.iot.exchange.redge.driverlib.CommandResponseCreator;
import proj.iot.exchange.redge.driverlib.commandresponse.CommandResponse;
import proj.iot.exchange.redge.driverlib.service.AbstractConnectGatewayService;
import jp.co.planis.samplehuedriver.data.HueSharedPreferences;

/**
 * Hueをコントロールするためのクラス
 * Created by y_akimoto on 2016/10/05.
 */
public class HueController {
    private final static String TAG = HueController.class.getSimpleName();
    private static final int MAX_HUE = 65535;

    private static HueController instance;

    private PHHueSDK phHueSDK;

    private HueSharedPreferences prefs;

    /** Bridgeへの接続までの待機時間 */
    private final static long BRIDGE_CONNECT_WAITING_TIME = 3000;

    public static HueController getInstance() {
        if (instance == null) {
            instance = new HueController();
        }

        return instance;
    }

    private HueController() {
        phHueSDK = PHHueSDK.getInstance();

        phHueSDK.getNotificationManager().registerSDKListener(phsdkListener);
    }

    public void initialize(Context context) {
        phHueSDK.setAppName(context.getResources().getString(R.string.app_name));
        phHueSDK.setDeviceName(Build.MODEL);

        prefs = HueSharedPreferences.getInstance(context);
        String lastIpAddress   = prefs.getLastConnectedIPAddress();
        String lastUsername    = prefs.getUsername();

        if (lastIpAddress != null && !lastIpAddress.equals("")) {
            Log.d(TAG, "last IP Address:" + lastIpAddress);
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);

            if (!phHueSDK.isAccessPointConnected(lastAccessPoint)) {
                Log.d(TAG, "Connect to bridge. IP:" + lastIpAddress);
                phHueSDK.connect(lastAccessPoint);
            }
        } else {
            Log.e(TAG, "No last access point!");
        }
    }

    /**
     * ブリッジを取得する
     * @return
     */
    public PHBridge getBridge() {
        return phHueSDK.getSelectedBridge();
    }

    public void onDestroy() {
        phHueSDK.getNotificationManager().unregisterSDKListener(phsdkListener);
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
        }
    }

    private PHSDKListener phsdkListener = new PHSDKListener() {
        /**
         * Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
         * and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.
         * @param list
         */
        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
            Log.d(TAG, "onAccessPointsFound");

        }

        /**
         * Here you receive notifications that the BridgeResource Cache was updated. Use the PHMessageType to
         * check which cache was updated, e.g.
         * @param list
         * @param phBridge
         */
        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
            Log.d(TAG, "onCacheUpdated");

        }

        /**
         * Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
         * At this point you are connected to a bridge so you should pass control to your main program/activity.
         * The username is generated randomly by the bridge.
         * Also it is recommended you store the connected IP Address/ Username in your app here.  This will allow easy automatic connection on subsequent use.
         * @param b
         * @param username
         */
        @Override
        public void onBridgeConnected(PHBridge b, String username) {
            Log.d(TAG, "onBridgeConnected");

            phHueSDK.setSelectedBridge(b);
            phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            phHueSDK.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration() .getIpAddress(), System.currentTimeMillis());
            prefs.setLastConnectedIPAddress(b.getResourceCache().getBridgeConfiguration().getIpAddress());
            prefs.setUsername(username);
        }

        /**
         * Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
         * you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.
         * @param phAccessPoint
         */
        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onAuthenticationRequired");

        }

        @Override
        public void onError(int code, String message) {
            if (code == PHHueError.NO_CONNECTION) {
                Log.w(TAG, "On No Connection");
            }
            else if (code == PHHueError.AUTHENTICATION_FAILED || code== PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
                Log.w(TAG, "Authentication Failed");
            }
            else if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
                Log.w(TAG, "Bridge Not Responding . . . ");

            }
            else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
                Log.w(TAG, "Bridge Not Found . . . ");
            }

        }

        @Override
        public void onConnectionResumed(PHBridge bridge) {

            Log.v(TAG, "onConnectionResumed" + bridge.getResourceCache().getBridgeConfiguration().getIpAddress());
            phHueSDK.getLastHeartbeat().put(bridge.getResourceCache().getBridgeConfiguration().getIpAddress(),  System.currentTimeMillis());
            for (int i = 0; i < phHueSDK.getDisconnectedAccessPoint().size(); i++) {

                if (phHueSDK.getDisconnectedAccessPoint().get(i).getIpAddress().equals(bridge.getResourceCache().getBridgeConfiguration().getIpAddress())) {
                    phHueSDK.getDisconnectedAccessPoint().remove(i);
                }
            }

        }

        /**
         * Here you would handle the loss of connection to your bridge.
         * @param accessPoint
         */
        @Override
        public void onConnectionLost(PHAccessPoint accessPoint) {
            Log.v(TAG, "onConnectionLost : " + accessPoint.getIpAddress());
            if (!phHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
                phHueSDK.getDisconnectedAccessPoint().add(accessPoint);
            }

        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {
            Log.d(TAG, "onParsingErrors");

        }
    };

    /**
     * ライトをランダムの色にする
     */
    public void randomLights() {
        PHBridge bridge = getBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setHue(rand.nextInt(MAX_HUE));
            // To validate your lightstate is valid (before sending to the bridge) you can use:
            // String validState = lightState.validateState();
            bridge.updateLightState(light, lightState, listener);
            //  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
        }
    }

    /**
     * ライトを全てONにする
     */
    public void turnOnLights() {
        if(!waitingConnect())
            return;

        this.turnOnOffLights(true);
    }

    /**
     * ライトを全てOFFにする
     */
    public void turnOffLights() {
        if(!waitingConnect())
            return;

        this.turnOnOffLights(false);
    }

    /**
     * ライトを全てON/OFFにする
     */
    private void turnOnOffLights(boolean on) {
        Log.d(TAG, "turnOnOffLights. type:" + on);

        if(!waitingConnect())
            return;

        PHBridge bridge = getBridge();

        if (bridge == null) {
            Log.e(TAG, "turnOnOffLights. bridge is null");
            return;
        }
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setOn(on);
            bridge.updateLightState(light, lightState, listener);
        }
    }

    /**
     * ライトの色を変更する
     * @param value
     */
    public void changeColor(String value) {
        Log.d(TAG, "changeColor. value:" + value);

        if(!waitingConnect())
            return;

        if (value == null) {
            Log.e(TAG, "changeColor. value is null");
            return;
        }

        value = value.replace("#", "");
        if (value.length() != 6) {
            Log.e(TAG, "changeColor. value length is not 6");
            return;
        }

        int r = 0;
        int g = 0;
        int b = 0;
        try {
            r = Integer.parseInt(value.substring(0, 2), 16);
            g = Integer.parseInt(value.substring(2, 4), 16);
            b = Integer.parseInt(value.substring(4, 6), 16);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getStackTrace().toString(), e);
            return;
        }

        Log.d(TAG, "changeColor. RGB. r:" + r + " g:" + g + " b:" + b);

        PHBridge bridge = getBridge();

        if (bridge == null) {
            Log.w(TAG, "PHBridge is null.");
            return;
        } else if (bridge.getResourceCache() == null) {
            Log.w(TAG, "bridge.getResourceCache is null.");
            return;
        }
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        for (PHLight light : allLights) {
            float xy[] = PHUtilities.calculateXYFromRGB(r, g, b, light.getModelNumber());
            PHLightState lightState = new PHLightState();
            lightState.setX(xy[0]);
            lightState.setY(xy[1]);
            bridge.updateLightState(light, lightState, listener);
        }
    }

    // If you want to handle the response from the bridge, create a PHLightListener object.
    PHLightListener listener = new PHLightListener() {

        @Override
        public void onSuccess() {
        }

        @Override
        public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
            Log.w(TAG, "Light has updated");
        }

        @Override
        public void onError(int arg0, String arg1) {}

        @Override
        public void onReceivingLightDetails(PHLight arg0) {}

        @Override
        public void onReceivingLights(List<PHBridgeResource> arg0) {}

        @Override
        public void onSearchComplete() {}
    };

    /**
     * ブリッジに接続するまで待機する.
     * 接続していたらTrueを返す. 接続待機時間以上待機したらFalseを返す.
     * @return
     */
    private boolean waitingConnect() {
        long startTime = System.currentTimeMillis();

        while (phHueSDK.getSelectedBridge() == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime > startTime + BRIDGE_CONNECT_WAITING_TIME) {
                return false;
            }
        }

        return true;
    }

    public void sendDriverInfo(Context context) {
        // ドライバの情報を収集
        CommandResponseCreator commandResponseCreator = new CommandResponseCreator(context);
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
        AbstractConnectGatewayService.responseToGateway(context, resultsJsonObject);
    }
}
