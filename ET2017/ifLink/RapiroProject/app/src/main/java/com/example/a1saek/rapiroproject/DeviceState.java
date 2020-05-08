package com.example.a1saek.rapiroproject;

import android.util.Log;

public class DeviceState {
    public  String TAG = "DeviceState";
    private rapiroState mCurrentState;
    enum rapiroState {
        seating,
        leaving,
        high_heart,
        low_heart,
        idle,
    };

    public DeviceState(){
        mCurrentState = rapiroState.idle;
    }


    private rapiroState getState(){
        return mCurrentState;
    }

    private void setState(rapiroState state){
        mCurrentState = state;
    }
    private rapiroState convertParamToState(String param){
        Log.d(TAG,"convertParamToState");
        rapiroState ret;
        if (param.equals("1")){
            ret = rapiroState.seating;
        } else if (param.equals("2")){
            ret = rapiroState.leaving;
        } else if (param.equals("3")){
            ret = rapiroState.high_heart;
        } else if (param.equals("4")){
            ret = rapiroState.low_heart;
        } else {
            ret = rapiroState.idle;
        }
        return ret;
    }

    public boolean changeState(String param){
        Log.d(TAG,"changeState");
        rapiroState state = convertParamToState(param);
        if(state == mCurrentState){
            Log.d(TAG,"No Change State");
            return false;
        } else {
            Log.d(TAG,"Chnage State [" + mCurrentState + "] -> [" + state +"]");
            mCurrentState = state;
            return true;
        }
    }

}
