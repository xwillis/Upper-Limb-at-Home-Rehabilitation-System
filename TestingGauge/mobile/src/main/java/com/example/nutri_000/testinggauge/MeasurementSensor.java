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

    boolean stimming=false;

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

    public void setProgressValues(int value){
        if(value>0){
            progressBars[1].setProgress(value);
            progressBars[0].setProgress(0);
        }else{
            progressBars[0].setProgress(-1*value);
            progressBars[1].setProgress(0);
        }
    }

    public void determineStim(int value,ConstraintLayout constraintLayout, boolean compensating){
            setProgressValues(value);
            if(!compensating){
                if(value>0&&value> seekBars[1].getProgress()){
                    constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                    stimming=true;
                } else if(value<0&&value<-1* seekBars[0].getProgress()){
                    constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                    stimming=true;
                }else{
                    constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                    stimming=false;
                }
            }

    }
}
