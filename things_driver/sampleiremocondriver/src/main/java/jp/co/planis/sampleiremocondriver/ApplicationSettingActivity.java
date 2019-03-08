package jp.co.planis.sampleiremocondriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by y_akimoto on 2016/12/15.
 */
public class ApplicationSettingActivity extends PreferenceActivity {

    private ApplicationSettingFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = new ApplicationSettingFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    public static class ApplicationSettingFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            Preference button = findPreference("button_pref_key");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    TCPIP tcpip = new TCPIP(getContext());
                    tcpip.send("*au\r\n", new TCPIP.SocketSendResultListener() {
                        @Override
                        public void onSocketSendResult(final String result) {
                            Log.d(Constants.LOG_TAG, "Socket send result:" + result);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    return false;
                }
            });

            // 各学習ボタンのセット
            int maxRemoconNum = this.getResources().getInteger(R.integer.max_remocon_num);
            for (int i = 1; i <= maxRemoconNum; i++) {
                setLearningButton(i);
            }
        }

        private void setLearningButton(final int learningNo) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String learningTitle = sharedPreferences.getString(LearningActivity.PREFERENSE_KEY_LEARNING_TITLE_PREFIX + learningNo, "");

            Preference button = findPreference("button_pref_learning_" + learningNo);

            if (learningTitle != "") {
                button.setSummary(learningTitle);
            }

            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), LearningActivity.class);
                    intent.putExtra("learningNo", learningNo);
                    startActivity(intent);
                    return false;
                }
            });
        }
    }
}
