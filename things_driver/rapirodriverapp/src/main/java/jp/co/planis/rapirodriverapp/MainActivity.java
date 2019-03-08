package jp.co.planis.rapirodriverapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonSendDeviceInfo;
        buttonSendDeviceInfo = (Button) findViewById(R.id.buttonSendDeviceInfo);
        buttonSendDeviceInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                RapiroController.getInstance().sendRapiroDriverInfo(MainActivity.this);
            }
        });
    }
}
