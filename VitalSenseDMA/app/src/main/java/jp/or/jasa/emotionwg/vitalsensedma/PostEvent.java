package jp.or.jasa.emotionwg.vitalsensedma;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class PostEvent extends AsyncTask<String, Void, String> {

    private Listener listener;

    // 非同期処理
    @Override
    protected String doInBackground(String... params) {

        // 送信先URL
        String urlSt = "<オリジナルIoTサービスのURL";
//        String urlSt = "http://localhost:50297/COMMA/Values2/";
        urlSt += params[0];
        Log.d("***** debug *****","PostEvent : " + urlSt);

        HttpURLConnection httpConn = null;
        String result = null;
        String word = "SenderParam:"+params[0];

        try {
            // URL設定
            URL url = new URL(urlSt);

            // HttpURLConnection
            httpConn = (HttpURLConnection) url.openConnection();

            // request POST
            httpConn.setRequestMethod("POST");

            // no Redirects
            httpConn.setInstanceFollowRedirects(false);

            // データを書き込む
            httpConn.setDoOutput(true);

            // 時間制限
            httpConn.setReadTimeout(10000);
            httpConn.setConnectTimeout(20000);

            // 接続
            httpConn.connect();

            // POSTデータ送信処理
            OutputStream outStream = null;

            try {
                outStream = httpConn.getOutputStream();
                outStream.write( word.getBytes("UTF-8"));
                outStream.flush();
                Log.d("***** debug *****","PostEvent : Sent(flush)");
            } catch (IOException e) {
                // POST送信エラー
                e.printStackTrace();
                result="POST送信エラー";
                Log.d("***** debug *****","PostEvent : IOException");
            } finally {
                Log.d("***** debug *****","PostEvent : finally");
                if (outStream != null) {
                    outStream.close();
                }
            }

            final int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // レスポンスを受け取る処理等
                result="HTTP_OK";
            }
            else{
                result="status="+String.valueOf(status);
            }

        } catch (IOException e) {
            Log.d("***** debug *****","PostEvent : IOException 1");
            e.printStackTrace();
        } finally {
            Log.d("***** debug *****","PostEvent : finally 1");
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
        return result;
    }

    // 非同期処理が終了後、結果をメインスレッドに返す
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (listener != null) {
            listener.onSuccess(result);
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onSuccess(String result);
    }
}
