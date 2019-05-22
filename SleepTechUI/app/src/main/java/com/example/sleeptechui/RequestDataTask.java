package com.example.sleeptechui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_BED;
import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_HUMID;
import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_TEMP;
import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_TRILLION;
import static com.example.sleeptechui.SleepTechUIConstants.R_EDGE_API_RESPONSE_RUNNING;
import static com.example.sleeptechui.SleepTechUIConstants.R_EDGE_API_RESPONSE_SUCCEEDED;
import static com.example.sleeptechui.SleepTechUIConstants.URL_IOT_EXCHANGE_QUERY;
import static com.example.sleeptechui.SleepTechUIConstants.URL_IOT_EXCHANGE_STATUS;
import static java.lang.System.lineSeparator;
import static java.lang.System.out;


public class RequestDataTask extends AsyncTask<String, Integer, Integer> {
    final private String TAG = "RequestDataTask";
    final int CONNECTION_TIMEOUT = 3000;
    final int READ_TIMEOUT = 3000;
    final long WAIT_TIME = 500;
    final long WAIT_HULF_TIME = 5000;
    final long ONE_MINUTE_FOR_MULTIPLE_NUMBER = 60;
    final long MILLISECOND = 1000;
    final long MULTIPLE_NUMBER = 6*60;
    final long INTERVAL_SECOND_BY_MILLISECOND = MILLISECOND * ONE_MINUTE_FOR_MULTIPLE_NUMBER * MULTIPLE_NUMBER;
    final int INDEX_CURRENT_DATA_TIME_FROM = 0;
    final int INDEX_CURRENT_DATA_TIME_TO = 1;
    final int MAX_SIZE_CURRENT_DATA_TIME = 2;

    private String time;
    private String version;
    private Activity activity;
    private String tmpString = null;
    private boolean isContinue = false;
    private Map<String,String> mLatestDateSet = null;
    /**
     * コンストラクタ
     */
    public RequestDataTask(Activity _activity) {
        super();
        activity = _activity;
        isContinue = true;
    }

    /**
     * バックグランドで行う処理
     */
    @Override
    protected Integer doInBackground(String... value) {
        URL url = null;
        HttpURLConnection conn = null;

        while(isContinue){
            try{
                URL downloadURLForTrillion = null;
                URL downloadURLForRoom = null;

                String queryIdForTrillion = requestQueryIdForTrillion();
                String queryIdForRoom = requestQueryIdForRoom();
                Thread.sleep(WAIT_HULF_TIME);
                //queryIdを取得し、ダウンロードURLを取得
                if(queryIdForTrillion != null && queryIdForRoom != null){
                    downloadURLForTrillion = requestStatus(queryIdForTrillion);
                    downloadURLForRoom = requestStatus(queryIdForRoom);
                } else {
                    Log.d(TAG,"get queryId failed!!");
                }

                if(downloadURLForTrillion != null && downloadURLForRoom != null){
                    getLatestDataSetFromServer(downloadURLForTrillion);
                    getLatestDataSetFromServer(downloadURLForRoom);
                } else {
                    Log.d(TAG,"get downloadURL failed!!");
                }
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isContinue = false; //デバッグ用 : 1回だけ通信をしたいときにコメントアウト外す
        }
        return 0;
    }

    /**
     * バックグランド処理が完了し、UIスレッドに反映する
     */
    @Override
    protected void onPostExecute(Integer res) {
        String error = null;
        String result = "";
        try {
            JSONObject json = new JSONObject(result);
            error = json.getJSONObject("ret").getString("error");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String type = "";
        String msg = "";

        if(error.equals("1")){

            //msg = activity.getString(R.string.error_msg);

        }
    }

    public void switchRequestContinue(){
        isContinue = !isContinue;
    }

    private String requestQueryIdForTrillion(){
        String queryId = null;
        URL url = null;
        HttpURLConnection conn = null;
        String result = "";
        try {
            url = new URL(URL_IOT_EXCHANGE_QUERY);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("X-IOT-API-KEY","391de4c3cc41c971e567595ee18329c7fb308281");
            conn.setRequestProperty("X-IOT-API-KEY","391de4c3cc41c971e567595ee18329c7fb308281");
            conn.setRequestMethod("POST");
            conn.connect();

            JSONObject requestJSON = createJSONForQueryIdForTrillion();
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),"utf-8");
            out.write(requestJSON.toString());
            out.close();
            //結果を確認する。
            int statusCode = conn.getResponseCode();

            if(statusCode == HttpURLConnection.HTTP_OK){

                //responseの読み込み
                final InputStream in = conn.getInputStream();
                String encoding = conn.getContentEncoding();
                if(null == encoding){
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufferedReader = new BufferedReader(inReader);
                String line = null;
                //int cnt = 0; //test
                while((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                Log.d(TAG, "response data | " + result);
                JSONObject tmpJSON = new JSONObject(result);
                queryId = getQueryIdFromJSON(tmpJSON);
                bufferedReader.close();
                inReader.close();
                in.close();
            } else {
                Log.d(TAG,"statusCode ->[" + statusCode + "]");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                // コネクションを切断
                conn.disconnect();
            }
        }
        return queryId;
    }

    private String requestQueryIdForRoom(){
        String queryId = null;
        URL url = null;
        HttpURLConnection conn = null;
        String result = "";
        try {
            url = new URL(URL_IOT_EXCHANGE_QUERY);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("X-IOT-API-KEY","391de4c3cc41c971e567595ee18329c7fb308281");
            conn.setRequestMethod("POST");
            conn.connect();

            JSONObject requestJSON = createJSONForQueryIdForRoom();
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),"utf-8");
            out.write(requestJSON.toString());
            out.close();
            //結果を確認する。
            int statusCode = conn.getResponseCode();

            if(statusCode == HttpURLConnection.HTTP_OK){

                //responseの読み込み
                final InputStream in = conn.getInputStream();
                String encoding = conn.getContentEncoding();
                if(null == encoding){
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufferedReader = new BufferedReader(inReader);
                String line = null;
                //int cnt = 0; //test
                while((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                Log.d(TAG, "response data | " + result);
                JSONObject tmpJSON = new JSONObject(result);
                queryId = getQueryIdFromJSON(tmpJSON);
                bufferedReader.close();
                inReader.close();
                in.close();
            } else {
                Log.d(TAG,"statusCode ->[" + statusCode + "]");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                // コネクションを切断
                conn.disconnect();
            }
        }
        return queryId;
    }

    private URL requestStatus(String queryID){
        String queryId = null;
        URL url = null;
        URL retUrl = null;
        HttpURLConnection conn = null;
        String result = "";
        try {
            url = new URL(URL_IOT_EXCHANGE_STATUS);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("X-IOT-API-KEY","391de4c3cc41c971e567595ee18329c7fb308281");
            conn.setRequestMethod("POST");
            conn.connect();

            JSONObject requestJSON = createJSONForStatus(queryID);
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(requestJSON.toString());
            out.close();
            //結果を確認する。
            int statusCode = conn.getResponseCode();

            if(statusCode == HttpURLConnection.HTTP_OK){

                //responseの読み込み
                final InputStream in = conn.getInputStream();
                String encoding = conn.getContentEncoding();
                if(null == encoding){
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufferedReader = new BufferedReader(inReader);
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                Log.d(TAG, "response data | " + result);
                JSONObject tmpJSON = new JSONObject(result);
                retUrl = getDownloadURLFromJSON(tmpJSON);
                bufferedReader.close();
                inReader.close();
                in.close();
            }
            Thread.sleep(WAIT_TIME);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                // コネクションを切断
                conn.disconnect();
            }
        }
        return retUrl;
    }

    private JSONObject createJSONForQueryIdForTrillion(){
        JSONObject retJSON = new JSONObject();
        JSONObject childJSON = new JSONObject();
        JSONArray childJSONsArray = new JSONArray();
        //JSONObject  conditionJSON = new JSONObject();
        long[] currentDataTimeWidth = culculateCurrentDataTimeWidth();
        try{
            childJSON.put("thing_uuid","");
            childJSON.put("r_edge_id","cf42f859-a84b-462d-8666-8f4c9680f4da");
            childJSON.put("driver_id","proj.iot.exchange.redge.driver.logger");
            childJSON.put("log_code","trillion_log");
            childJSON.put("timestamp_from",currentDataTimeWidth[INDEX_CURRENT_DATA_TIME_FROM]);
            childJSON.put("timestamp_to",currentDataTimeWidth[INDEX_CURRENT_DATA_TIME_TO]);
            childJSON.put("conditions",new JSONArray());
            childJSONsArray.put(childJSON);
            retJSON.put("queries", childJSONsArray);
            Log.d(TAG,"create JSON |" + retJSON.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retJSON;
    }

    private JSONObject createJSONForQueryIdForRoom(){
        JSONObject retJSON = new JSONObject();
        JSONObject childJSON = new JSONObject();
        JSONArray childJSONsArray = new JSONArray();
        //JSONObject  conditionJSON = new JSONObject();
        long[] currentDataTimeWidth = culculateCurrentDataTimeWidth();
        try{
            childJSON.put("thing_uuid","");
            childJSON.put("r_edge_id","1b38cf11-936b-403d-918d-200a649d634e");
            childJSON.put("driver_id","proj.iot.exchange.redge.driver.logger");
            childJSON.put("log_code","fp071");
            childJSON.put("timestamp_from",currentDataTimeWidth[INDEX_CURRENT_DATA_TIME_FROM]);
            childJSON.put("timestamp_to",currentDataTimeWidth[INDEX_CURRENT_DATA_TIME_TO]);
            childJSON.put("conditions",new JSONArray());
            childJSONsArray.put(childJSON);
            retJSON.put("queries", childJSONsArray);
            Log.d(TAG,"create JSON |" + retJSON.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retJSON;
    }

    private JSONObject createJSONForStatus(String queryID){
        JSONObject retJSON = new JSONObject();
        JSONArray idArray = new JSONArray();
        try{
            idArray.put(queryID);
            retJSON.put("query_execution_ids", idArray);
            Log.d(TAG,"create JSON |" + retJSON.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retJSON;
    }

    private String getQueryIdFromJSON(JSONObject json){
        String retQueryId = null;
        try{
            JSONArray resultJSONArray = json.getJSONArray("results");
            retQueryId = resultJSONArray.getJSONObject(0).getString("query_execution_id");
            Log.d(TAG,"retQueryId -> [" + retQueryId + "]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retQueryId;
    }

    private URL getDownloadURLFromJSON(JSONObject json){
        URL retDownloadURL = null;
        String urlString = null;
        try{
            JSONArray resultJSONArray = json.getJSONArray("results");
            JSONObject responseData = resultJSONArray.getJSONObject(0);
            String tempResultString = responseData.getString("result");
            if(tempResultString.equals(R_EDGE_API_RESPONSE_SUCCEEDED)){
                Log.d(TAG,"get download url success !!!");
                urlString = responseData.getString("download_url");
            } else if (tempResultString.equals(R_EDGE_API_RESPONSE_RUNNING)){
                Log.d(TAG,"get download url wait...");
            } else {
                Log.d(TAG,"get download url failed !!! status is ["+ responseData.getString("result") +"]");
            }
            retDownloadURL = new URL(urlString);
            Log.d(TAG,"retDownloadURL -> [" + retDownloadURL + "]");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return retDownloadURL;
    }

    private void getLatestDataSetFromServer(URL downloadUrl){
        String DataCSVString = downloadCSVFile(downloadUrl);
        //String DataCSVString = newDownloadCSVFile(downloadUrl);
        //Log.d(TAG,"CSVの文字数は" + DataCSVString.length());
        mLatestDateSet = SensorDataUtil.getLatestDataSet(DataCSVString);
        Log.d(TAG,"LatestDateSet Get !!!!");
        Log.d(TAG,"LatestDateSet -> [type,value]");
        Log.d(TAG,"LatestDateSet -> [" + KEY_STRING_FANACT_TEMP + "," + mLatestDateSet.get(KEY_STRING_FANACT_TEMP) + "]");
        Log.d(TAG,"LatestDateSet -> [" + KEY_STRING_FANACT_HUMID + "," + mLatestDateSet.get(KEY_STRING_FANACT_HUMID) + "]");
        Log.d(TAG,"LatestDateSet -> [" + KEY_STRING_FANACT_BED + "," + mLatestDateSet.get(KEY_STRING_FANACT_BED) + "]");
        Log.d(TAG,"LatestDateSet -> [" + KEY_STRING_FANACT_TRILLION + "," + mLatestDateSet.get(KEY_STRING_FANACT_TRILLION) + "]");
    }

    private String downloadCSVFile(URL downloadUrl){
        byte[] retBuffer = null;
        String retString = null;
        try{
            URLConnection conn = downloadUrl.openConnection();

            InputStream in = conn.getInputStream();
            retBuffer = new byte[conn.getContentLength()];
            in.read(retBuffer, 0, conn.getContentLength());
            retString = new String(retBuffer,"UTF-8");
            //retString = retBuffer.toString();
            //全部読み込み
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return retString;
    }

    private String newDownloadCSVFile(URL downloadUrl){
        HttpURLConnection con = null;
        String retString = null;
        // 出力ファイルフルパス
        final String filePath = "./filename.csv";
        // ローカル処理
        // コネクション取得
        try {
            con = (HttpURLConnection) downloadUrl.openConnection();
            con.connect();

            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // ファイルのダウンロード処理を実行
                // 読み込み用ストリーム
                final InputStream input = con.getInputStream();
                final DataInputStream dataInput = new DataInputStream(input);
                // 書き込み用ストリーム
                final FileOutputStream fileOutput = new FileOutputStream(filePath);
                //final FileOutputStream fileOutput = openFileOutput("test.txt", Context.MODE_PRIVATE);
                final DataOutputStream dataOut = new DataOutputStream(fileOutput);
                // 読み込みデータ単位
                final byte[] buffer = new byte[con.getContentLength()];
                // 読み込んだデータを一時的に格納しておく変数
                int readByte = 0;

                // ファイルを読み込む
                while((readByte = dataInput.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, readByte);
                }
                retString = new String(buffer,"UTF-8");
                // 各ストリームを閉じる
                dataInput.close();
                fileOutput.close();
                dataInput.close();
                input.close();
                // 処理成功
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }
        return retString;
    }


    private long[] culculateCurrentDataTimeWidth(){
        long[] currentDataTimeWidth = new long[MAX_SIZE_CURRENT_DATA_TIME];
        long oneSecondAgo = System.currentTimeMillis() - INTERVAL_SECOND_BY_MILLISECOND;
        long currentTime = System.currentTimeMillis();
        currentDataTimeWidth[INDEX_CURRENT_DATA_TIME_FROM] = oneSecondAgo;
        currentDataTimeWidth[INDEX_CURRENT_DATA_TIME_TO] = currentTime;
        return currentDataTimeWidth;
    }

    private String[] seperateJSONConnectionString(String connectedJson){
        String regex1 = "}";
        String regex2 = "{";
        String[] JSONStrings = connectedJson.split(regex1+ regex2);
        for(String JSONString : JSONStrings){
            Log.d(TAG,JSONString);
        }
        return JSONStrings;
    }


}
