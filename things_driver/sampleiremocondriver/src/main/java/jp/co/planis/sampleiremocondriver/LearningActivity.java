package jp.co.planis.sampleiremocondriver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by y_akimoto on 2016/12/16.
 */
public class LearningActivity extends Activity {

    public static final String PREFERENSE_KEY_LEARNING_TITLE_PREFIX = "preference_key_learning_title_";

    private boolean isLearning = false;
    private Integer learningNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        learningNo = intent.getIntExtra("learningNo", 0);
        if (learningNo == 0) {
            this.finish();
        }

        setContentView(R.layout.remocon_learning);

        EditText editText = (EditText)findViewById(R.id.learning_title);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String learningTitle = sharedPreferences.getString(PREFERENSE_KEY_LEARNING_TITLE_PREFIX + this.learningNo, "");
        editText.setText(learningTitle);

        TextView learningNoView = (TextView)findViewById(R.id.learning_no);
        learningNoView.setText(String.valueOf(this.learningNo));
    }

    public void onClickLearningButton(View view) {
        final Button button = (Button) findViewById(R.id.learning_button);
        TCPIP tcpip = new TCPIP(this);

        if (!isLearning) {
            isLearning = true;
            button.setText("リモコン学習中止");
            Toast.makeText(LearningActivity.this, "リモコン学習を開始します", Toast.LENGTH_SHORT).show();

            tcpip.send("*ic;" + learningNo + "\r\n", new TCPIP.SocketSendResultListener() {
                @Override
                public void onSocketSendResult(final String result) {
                    Log.d(Constants.LOG_TAG, "Learning result:" + result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LearningActivity.this, "リモコン学習完了:" + result, Toast.LENGTH_LONG).show();
                            isLearning = false;
                            button.setText("リモコン学習開始");
                        }
                    });
                }
            });
        } else {
            isLearning = false;
            button.setText("リモコン学習開始");
            Toast.makeText(LearningActivity.this, "リモコン学習を中止しました", Toast.LENGTH_SHORT).show();

            tcpip.send("*cc\r\n", new TCPIP.SocketSendResultListener() {
                @Override
                public void onSocketSendResult(String result) {
                    Log.d(Constants.LOG_TAG, "Learning cancel result:" + result);
                }
            });
        }
    }

    public void onClickSaveLearningTitleButton(View view) {
        EditText editText = (EditText)findViewById(R.id.learning_title);
        String learningTitle = editText.getText().toString();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENSE_KEY_LEARNING_TITLE_PREFIX + this.learningNo, learningTitle);
        editor.apply();

        Toast.makeText(LearningActivity.this, "学習タイトルを保存しました", Toast.LENGTH_LONG).show();
    }

    public void onClickExecute(View view) {
        TCPIP tcpip = new TCPIP(this);

        Toast.makeText(this, "赤外線を送信します", Toast.LENGTH_SHORT).show();

        tcpip.send("*is;" + learningNo + "\r\n", new TCPIP.SocketSendResultListener() {
            @Override
            public void onSocketSendResult(final String result) {
                Log.d(Constants.LOG_TAG, "send result:" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LearningActivity.this, "赤外線送信完了:" + result, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
