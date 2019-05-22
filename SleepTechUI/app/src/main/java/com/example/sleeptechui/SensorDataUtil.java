package com.example.sleeptechui;


import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_BED;
import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_HUMID;
import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_TEMP;
import static com.example.sleeptechui.SleepTechUIConstants.KEY_STRING_FANACT_TRILLION;

public class SensorDataUtil {
    final static String TAG = "SensorDataUtil";
    final static int INDEX_CULMUN_NAME = 0;
    final static int INDEX_DATA_RECORD_START = 1;
    final static int INDEX_DATA_CULUMN_START = 0;
    final private static String LC = System.getProperty("line.separator");
    final private static String DATA_SET_KEY_TYPE = "type";
    final private static String DATA_SET_KEY_VALUE = "value";
    final private static String DATA_SET_KEY_LOG_DATE_TIME = "timestamp";
    //final private static String DATA_SET_KEY_LOG_DATE_TIME = "log_date_time";
    final private static String DATA_SET_KEY_EXTRA_DEVICE_TYPE ="extra_device_type";
    final private static String DATA_SET_KEY_EXTRA_DEVICE_OS = "extra_device_os";
    final private static String DATA_SET_KEY_EXTRA_DEVICE_CODE ="extra_device_code";
    final private static String DATA_SET_KEY_DT = "dt";
    final private static String DATA_SET_KEY_R_EDGE_ID = "r_edge_id";
    final private static String DATA_SET_KEY_THING_UUID = "thing_uuid";

//    public SensorDataUtil(){
//        //latestDataSet = new HashMap<>();
//    }

    static public Map<String,String> getLatestDataSet(String csv){
        List<Map<String,String>> fullDataSet;
        fullDataSet = createFullDataSet(csv);
        return extractLatestDataSet(fullDataSet);
    }

    static private List<Map<String,String>> createFullDataSet(String firstCSV){
        List<Map<String,String>> fullDataSet = new ArrayList<>();
        String[] tempRecords = firstCSV.split(LC);
        Log.d(TAG,tempRecords[tempRecords.length-1]);
        strListLog(tempRecords);
        String[] culumnSet = trimmingStrings(tempRecords[INDEX_CULMUN_NAME].split(","));
        for(int record_idx = INDEX_DATA_RECORD_START; record_idx < tempRecords.length-1; record_idx++){
            Map<String,String> tempMap = new HashMap<>();
            String[] tmpDataGroup = trimmingStrings(tempRecords[record_idx].split(","));
            for(int culumn_idx = INDEX_DATA_CULUMN_START; culumn_idx < tmpDataGroup.length; culumn_idx++) {
                tempMap.put(culumnSet[culumn_idx], tmpDataGroup[culumn_idx]);
            }
            fullDataSet.add(tempMap);
        }
        Log.d(TAG,"data list's size is [" + fullDataSet.size() + "]");
        return fullDataSet;
    }

    static public Map<String,String> extractLatestDataSet(List<Map<String,String>> DateSet){
        Map<String,String> latestDataSet = new HashMap<>();
        latestDataSet.put(KEY_STRING_FANACT_TEMP,getLatestDataFromType(KEY_STRING_FANACT_TEMP,DateSet));
        latestDataSet.put(KEY_STRING_FANACT_HUMID,getLatestDataFromType(KEY_STRING_FANACT_HUMID,DateSet));
        latestDataSet.put(KEY_STRING_FANACT_BED,getLatestDataFromType(KEY_STRING_FANACT_BED,DateSet));
        latestDataSet.put(KEY_STRING_FANACT_TRILLION,getLatestDataFromType(KEY_STRING_FANACT_TRILLION,DateSet));
        return latestDataSet;
    }

    static private String getLatestDataFromType(String type,List<Map<String,String>> DateSet){
        if(DateSet == null) return null;
        if(DateSet.size() == 0) return null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date latestDay = null;
        String latestData = null;

        try{
            Log.d(TAG,DateSet.get(0).get(DATA_SET_KEY_LOG_DATE_TIME));
            latestDay = df.parse(DateSet.get(0).get(DATA_SET_KEY_LOG_DATE_TIME));
        } catch (ParseException e){
            e.printStackTrace();
        }

        //各要素について見ていく
        for(Map<String,String> tempDataMap : DateSet){
            if(tempDataMap.get(DATA_SET_KEY_TYPE).equals(type)){
                Date compDay = null;
                try{
                    compDay = df.parse(tempDataMap.get(DATA_SET_KEY_LOG_DATE_TIME));
                    //Log.d(TAG,"type = [" + type + "], check day is [" + compDay + "]");
                } catch (ParseException e){
                    e.printStackTrace();
                }
                if(latestDay.before(compDay)){
                    //最新のデータ
                    latestData = tempDataMap.get(DATA_SET_KEY_VALUE);
                    //Log.d(TAG,"type = [" + type + "], latest day is [" + compDay + "], latest day is [" + latestData +"]");
                }

            }
        }
        return latestData;
    }

    static private String trimmingString(String str){
        return str.replaceAll("\"","");
    }

    static private String[] trimmingStrings(String[] strs){
        String[] retStrings = new String[strs.length];
        for(int idx = 0; idx < strs.length; idx++){
            retStrings[idx] = trimmingString(strs[idx]);
        }
        return retStrings;
    }

    //for debug
    static private void strListLog(String[] strs){
        Thread logThread = new strLogThread(strs);
        logThread.start();
    }


    static private class strLogThread extends Thread{
        //変数の宣言
        private String[] mStrs;

        //スレッド作成時に実行される処理
        public strLogThread(String[] strs){
            this.mStrs = strs;
        }

        //スレッド実行時の処理
        public void run(){
            for(String str : mStrs){
                Log.d(TAG,str);
            }
        }

        //スレッド終了時に呼び出し
        public void stopThread(){
        }
    }
}
