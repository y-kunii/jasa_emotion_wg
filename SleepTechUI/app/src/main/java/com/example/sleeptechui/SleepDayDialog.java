package com.example.sleeptechui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class SleepDayDialog extends DialogFragment {
    final private String TAG = "DialogFragment";
    final private int MAX_ITEM_NUMBER = 5;
    final private int DATE_INDEX = 0;
    final private int TEMPERATURE_INDEX = 1;
    final private int HUMIDITY_INDEX = 2;
    final private int LUMINACE_INDEX = 3;
    final private int EVALUATION_INDEX = 4;


    // ダイアログが生成された時に呼ばれるメソッド ※必須
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        Log.d(TAG, "onCreateDialog start");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        String[] items = {"","","","",""};

        // リスト項目生成
        Bundle bundle = getArguments();
        if(bundle != null) {
            SleepDayData viewSleepDayData = (SleepDayData) bundle.getSerializable("SLEEP_DAY_DATA");
            items = createItem(viewSleepDayData,MAX_ITEM_NUMBER);
        }

        // タイトル設定
        dialogBuilder.setTitle("日付データ詳細");
        // リスト項目を設定 & クリック時の処理を設定
        dialogBuilder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        Log.d(TAG, "onCreateDialog end");
        // dialogBuilderを返す
        return dialogBuilder.create();
    }

    private String[] createItem(SleepDayData sleepDayData,int itemSize){
        String[] returnItem = new String[itemSize];
        returnItem[DATE_INDEX] =  "日付 : " + sleepDayData.getDate();
        returnItem[TEMPERATURE_INDEX] = "平均室温 : " + String.valueOf(sleepDayData.getTemperature()) + " °";
        returnItem[HUMIDITY_INDEX] = "平均湿度 : " + String.valueOf(sleepDayData.getHumidity()) + " %";
        returnItem[LUMINACE_INDEX] = "平均輝度 : " + String.valueOf(sleepDayData.getLuminance()) + " lx";
        returnItem[EVALUATION_INDEX] = "この日の評価 : " + sleepDayData.getEvaluation();
        return returnItem;
    }
}
