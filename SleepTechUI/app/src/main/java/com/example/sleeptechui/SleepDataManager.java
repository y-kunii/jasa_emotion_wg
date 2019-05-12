package com.example.sleeptechui;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SleepDataManager {
    final String TAG = "SleepDataManager";
    DbOpenHelper mDBHelper = null;
    SQLiteDatabase db = null;
    final String SCHEDULE_TABLE_NAME = "schedules";

    SleepDataManager(Context context){
        if(mDBHelper == null || db == null) {
            mDBHelper = new DbOpenHelper(context);
            db = mDBHelper.getWritableDatabase();
        }
    }

    public void createTacle(){

    }

//    public String searchData(String date){
//        // Cursorを確実にcloseするために、try{}～finally{}にする
//        Cursor cursor = null;
//        try{
//            //SQL文
//            String sql    = "SELECT * FROM " + SCHEDULE_TABLE_NAME + " WHERE date = ?";
//
//            //SQL文の実行
//            cursor = db.rawQuery(sql , new String[]{date});
//
//            // 検索結果をcursorから読み込んで返す
//            return readCursor( cursor );
//        }
//        finally{
//            // Cursorを忘れずにcloseする
//            if( cursor != null ){
//                cursor.close();
//            }
//        }
//    }

//    /** 検索結果の読み込み */
//    private String readCursor(Cursor cursor ){
//        //カーソル開始位置を先頭にする
//        cursor.moveToFirst();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 1; i <= cursor.getCount(); i++) {
//            //SQL文の結果から、必要な値を取り出す
//            sb.append(cursor.getString(1)).・・・//何か処理
//            cursor.moveToNext();
//        }
//        return sb.toString();
//    }

    public void insertData(SleepDayData insertData){
//        SleepDayData insertData = new SleepDayData();
        String sql = "";
        sql = createInsertSQL(insertData);
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public SleepDayData selectData(String day){
        String sql = createSelectSQLForDayData(day);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if(cursor.getCount() != 0) {
            Log.d(TAG, "データの取得ができました : [" + cursor.getCount() + "]件");
        } else {
            Log.d(TAG, "データの取得に失敗しました。");
        }
        SleepDayData returnSleepDayData = convertCursorToSleepDayData(cursor);
        if(returnSleepDayData != null){
            Log.d(TAG,"selectData success!");
        } else {
            Log.d(TAG,"selectData failed!");
        }
        return returnSleepDayData;
    }

    private String[] selectDaysList(){
        String sql = createSelectSQLForDayList();
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if(cursor.getCount() != 0) {
            Log.d(TAG, "データの取得ができました : [" + cursor.getCount() + "]件");
        } else {
            Log.d(TAG, "データの取得に失敗しました。");
        }
        String[] returnSleepDaysList = convertCursorToSleepDaysList(cursor);
        if(returnSleepDaysList != null){
            Log.d(TAG,"selectData success!");
        } else {
            Log.d(TAG,"selectData failed!");
        }
        return returnSleepDaysList;
    }

    public void updateData(String title, String place,String memo,String startAt, String date){
        String sql = "update " + SCHEDULE_TABLE_NAME
                + " SET title =?, place = ?,memo = ?, start_at = ? "
                + "WHERE date = ? ;";
        String[] bindStr = new String[]{
                title,
                place,
                memo,
                startAt,
                date
        };
        try {
            db.execSQL(sql,bindStr);
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void deleteData(String date){
        // ContentValuesのインスタンスにデータを格納
        String sql = "DELETE FROM " + SCHEDULE_TABLE_NAME
                + " WHERE date = ?;";
        String[] bindStr = new String[]{
                date
        };
        try {
            db.execSQL(sql,bindStr);
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void deleteAllData(){
        // ContentValuesのインスタンスにデータを格納
        String sql = "DELETE FROM " + SCHEDULE_TABLE_NAME+ ";";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }
    }

    private String createInsertSQL(SleepDayData sleepData){
        String sql = "insert into " + SCHEDULE_TABLE_NAME + " (date, temperature, Humidity, Luminance, Evaluation) values ("
          + "\"" + sleepData.getDate() + "\"" + ","
          + sleepData.getTemperature() + ","
          + sleepData.getHumidity() + ","
          + sleepData.getLuminance() + ","
          + "\"" + sleepData.getEvaluation() + "\""
          + ");";
        Log.d(TAG,"createInsertSQL -> [" + sql + "]");
        return sql;
    }

    private String createSelectSQLForDayData(String day){
        String sql = "SELECT * FROM " + SCHEDULE_TABLE_NAME + " WHERE date = " + "\"" + day + "\"";
        Log.d(TAG,"createSelectSQL -> [" + sql + "]");
        return  sql;
    }

    private String createSelectSQLForDayList(){
        String sql = "SELECT date FROM " + SCHEDULE_TABLE_NAME;
        Log.d(TAG,"createSelectSQL -> [" + sql + "]");
        return  sql;
    }

    private SleepDayData convertCursorToSleepDayData(Cursor c){
        SleepDayData reterunSleepDayData = null;
        Log.d(TAG,"convertCursorToSleepDayData -> "
                + String.valueOf(c.getColumnIndex("date")) + ","
                + c.getColumnIndex("temperature") + ","
                + c.getColumnIndex("Humidity") + ","
                + c.getColumnIndex("Luminance") + ","
                + String.valueOf(c.getColumnIndex("Evaluation"))
        );
        if(c != null) {
            reterunSleepDayData = new SleepDayData(
                    //c.getColumnIndex("data_id"),
                    c.getString(c.getColumnIndex("date")),
                    c.getInt(c.getColumnIndex("temperature")),
                    c.getInt(c.getColumnIndex("Humidity")),
                    c.getInt(c.getColumnIndex("Luminance")),
                    c.getString(c.getColumnIndex("Evaluation"))
            );
        }
        return reterunSleepDayData;
    }

    private String[] convertCursorToSleepDaysList(Cursor c){
        int maxCount = c.getCount();
        String[] reterunSleepDaysList = new String[maxCount];
        if(c != null) {
            do{
                int count = c.getPosition();
                Log.d(TAG,"count -> [" + count + "]");
                reterunSleepDaysList[count] = c.getString(c.getColumnIndex("date"));
            } while(c.moveToNext());
        }
        return reterunSleepDaysList;
    }

    public String[] getSleepDaysListFromDB(){
        String[] SleepDaysList = selectDaysList();
        Log.d(TAG,"Get Days List -> ");
        for(int i = 0;i < SleepDaysList.length; i++){
            Log.d(TAG,"SleepDaysList[" + i + "]" + " = " + SleepDaysList[i]);
        }
        return SleepDaysList;
    }

}
