package com.example.nutri_000.testinggauge;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
//todo does not update compensation bars on any
public class MeasurementSensor {
    public ProgressBar[] progressBars;
    public SeekBar[] seekBars;//0 is neg/left
    public TextView[] textViews;

    boolean compensating=false;

    public MeasurementSensor(ProgressBar[] progressBars, SeekBar[] seekBars, TextView[] textViews){
        this.progressBars=progressBars;
        this.seekBars=seekBars;
        this.textViews=textViews;
        for(int i=0;i<1;i++){

                this.progressBars[i].setMax(180);
                this.seekBars[i].setMax(180);

                this.progressBars[i].setProgress(90);
                this.seekBars[i].setProgress(90);

        }

    }
    public void setProgressValues(BleNotification notification){
        if(notification.valueX>0){
            progressBars[1][0].setProgress((int)notification.valueX);
            progressBars[0][0].setProgress(0);
        }else{
            progressBars[0][0].setProgress(-1*(int)notification.valueX);
            progressBars[1][0].setProgress(0);
        }
        if(notification.valueY>0){
            progressBars[1][1].setProgress((int)notification.valueY);
            progressBars[0][1].setProgress(0);
        }else{
            progressBars[0][1].setProgress(-1*(int)notification.valueY);
            progressBars[1][1].setProgress(0);
        }
        progressBars[0][2].setProgress((int)notification.valueZ);
    }
    public void determineCompensation(BleNotification notification, ConstraintLayout constraintLayout, boolean stimming){
        if(notification.valueX> seekBars[1][0].getProgress()||notification.valueX<-1* seekBars[0][0].getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notification.valueY> seekBars[1][1].getProgress()||notification.valueY<-1* seekBars[0][1].getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notification.valueZ> seekBars[0][2].getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else {
            compensating = false;
            if (!stimming) {
                constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }

        setProgressValues(notification);

    }
}
