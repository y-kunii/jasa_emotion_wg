package com.example.sleeptechui;

import java.io.Serializable;

public class SleepDayData implements Serializable {
    int mDataId;
    String mDate;
    int mTemperature;
    int mHumidity;
    int mLuminance;
    String mEvaluation;

    public SleepDayData(
            String Date,
            int Temperature,
            int Humidity,
            int Luminance,
            String Evaluation)
    {
        mDate = Date;
        mTemperature = Temperature;
        mHumidity = Humidity;
        mLuminance = Luminance;
        mEvaluation = Evaluation;
    }

    public int getDataId() {
        return mDataId;
    }
    public String getDate(){
        return mDate;
    }
    public int getTemperature(){
        return mTemperature;
    }
    public int getHumidity(){
        return mHumidity;
    }
    public int getLuminance(){
        return mLuminance;
    }
    public String getEvaluation(){
        return mEvaluation;
    }
    public void setDataId(int DataId) {
        mDataId = DataId;
    }
    public void setDate(String Date){
        mDate = Date;
    }
    public void setTemperature(int Temperature){
        mTemperature = Temperature;
    }
    public void setHumidity(int Humidity){
        mHumidity = Humidity;
    }
    public void setLuminance(int Luminance){
        mLuminance = Luminance;
    }
    public void setEvaluation(String Evaluation){
        mEvaluation = Evaluation;
    }

}
