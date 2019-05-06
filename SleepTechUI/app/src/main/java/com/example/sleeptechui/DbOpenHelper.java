package com.example.sleeptechui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbOpenHelper";
    private static final String DATABASE_NAME = "sleepday.db";
    private static final String SCHEDULE_TABLE_NAME = "schedules";
    private static final int DATABASE_VERSION = 1;

    public DbOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        createScheduleTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_TABLE_NAME + ";");
        onCreate(db);
    }

    private void createScheduleTable(SQLiteDatabase db){
        // テーブル作成SQL
        String sql = "CREATE TABLE schedules ("
                + " data_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " date TEXT,"
                + " temperature INTEGER,"
                + " Humidity INTEGER,"
                + " Luminance INTEGER,"
                + " Evaluation TEXT"
                + ");";
        db.execSQL(sql);
        Log.i(TAG,"テーブルschedulesが作成されました");
    }
}
