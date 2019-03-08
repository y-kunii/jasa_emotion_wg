package jp.co.planis.samplehuedriver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by y_akimoto on 2016/10/05.
 */
public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);

        HueController.getInstance().initialize(this);

        Button onButton;
        onButton = (Button) findViewById(R.id.buttonOn);
        onButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HueController.getInstance().turnOnLights();;
            }
        });

        Button offButton;
        onButton = (Button) findViewById(R.id.buttonOff);
        onButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HueController.getInstance().turnOffLights();;
            }
        });

        Button randomButton;
        randomButton = (Button) findViewById(R.id.buttonRand);
        randomButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HueController.getInstance().randomLights();;
            }
        });

        Button buttonSendDeviceInfo;
        buttonSendDeviceInfo = (Button) findViewById(R.id.buttonSendDeviceInfo);
        buttonSendDeviceInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HueController.getInstance().sendDriverInfo(MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        HueController.getInstance().onDestroy();
        super.onDestroy();
    }
}
