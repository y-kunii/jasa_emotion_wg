package jp.co.planis.sampleiremocondriver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by y_akimoto on 2016/12/16.
 */
public class TCPIP {

    final private String ipaddress;
    final private int port;
    final private Context context;

    public TCPIP(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ipaddress = sharedPreferences.getString(
                context.getString(R.string.preference_key_iremocon_ipaddress),
                context.getString(R.string.default_iremocon_ipaddress));
        port = context.getResources().getInteger(R.integer.iremocon_port);
    }

    /**
     * ソケット接続を行う
     * @return
     */
    public void send(final String command, final SocketSendResultListener listener) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Socket connection = null;
                BufferedReader reader = null;
                BufferedWriter writer = null;

                try {
                    // 接続
                    connection = new Socket(ipaddress, port);
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

                    // 送信
                    writer.write(command);
                    writer.flush();

                    // データ受信
                    String data = reader.readLine();
                    Log.d(Constants.LOG_TAG, "受信文字列:" + data);
                    listener.onSocketSendResult(data);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "接続エラー", Toast.LENGTH_SHORT).show();

                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ioe) {
                            Log.e(Constants.LOG_TAG, ioe.getStackTrace().toString());
                        }
                    }
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException ioe) {
                            Log.e(Constants.LOG_TAG, ioe.getStackTrace().toString());
                        }
                    }
                    if(connection != null) {
                        try {
                            connection.close();
                        } catch (IOException ioe) {
                            Log.e(Constants.LOG_TAG, ioe.getStackTrace().toString());
                        }
                    }
                }
            }
        };
        thread.start();
    }

    public interface SocketSendResultListener {
        void onSocketSendResult(String result);
    }

}

