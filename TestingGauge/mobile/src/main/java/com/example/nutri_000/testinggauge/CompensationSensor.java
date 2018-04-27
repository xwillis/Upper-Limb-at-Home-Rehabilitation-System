package com.example.nutri_000.testinggauge;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
//todo does not update compensation bars on any
public class CompensationSensor {
    String tag="Compensation Sensor";
    public ProgressBar[][] progressBars;
    public SeekBar[][] seekBars;//0 is neg/left, 0-2 is x-z
    public TextView[] textViews;

    private int[] offsets={0,0,0};
    boolean compensating=false;

    public CompensationSensor(ProgressBar[][] progressBars, SeekBar[][] seekBars, TextView[] textViews){
        this.progressBars=progressBars;
        this.seekBars=seekBars;
        this.textViews=textViews;
        for(int i=0;i<2;i++){
            for(int ii=0;ii<2;ii++){
                this.progressBars[ii][i].setMax(180);
                this.seekBars[ii][i].setMax(180);

                this.progressBars[ii][i].setProgress(90);
                this.seekBars[ii][i].setProgress(90);
            }
        }
        progressBars[0][2].setMax(360);
        seekBars[0][2].setMax(360);

        progressBars[0][2].setProgress(180);
        seekBars[0][2].setProgress(180);
    }
    public void setProgressValues(BleNotification notification){
        int x=(int)notification.valueX-offsets[0];
        int y=(int)notification.valueY-offsets[1];
        int z=(int)notification.valueZ-offsets[2];
        if(x>0){
            progressBars[1][0].setProgress(x);
            progressBars[0][0].setProgress(0);
        }else{
            progressBars[0][0].setProgress(-1*x);
            progressBars[1][0].setProgress(0);
        }
        if(y>0){
            progressBars[1][1].setProgress(y);
            progressBars[0][1].setProgress(0);
        }else{
            progressBars[0][1].setProgress(-1*y);
            progressBars[1][1].setProgress(0);
        }
        progressBars[0][2].setProgress(z);
        textViews[0].setText(x+"/"+seekBars[1][0].getProgress()+" or "+(-1)*seekBars[0][0].getProgress());
        textViews[1].setText(y+"/"+seekBars[1][1].getProgress()+" or "+(-1)*seekBars[0][1].getProgress());
        textViews[2].setText(z+"/"+seekBars[0][2].getProgress());
    }
    public void determineCompensation(BleNotification notification, ConstraintLayout constraintLayout, boolean stimming){
        notification.valueX=(int)notification.valueX-offsets[0];
        notification.valueY=(int)notification.valueY-offsets[1];
        notification.valueZ=(int)notification.valueZ-offsets[2];
        if(notification.valueX>= seekBars[1][0].getProgress()||notification.valueX<=-1* seekBars[0][0].getProgress()
                ||notification.valueY>= seekBars[1][1].getProgress()||notification.valueY<=-1* seekBars[0][1].getProgress()
                ||notification.valueZ>= seekBars[0][2].getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else {
            compensating = false;
            if (!stimming) {
                //Log.d(tag, "Background -> white, not compensating or stimming");
                constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }

        setProgressValues(notification);

    }
    public void calibrate(BleNotification notification){
        int[] temp={(int)notification.valueX,(int)notification.valueY,(int)notification.valueZ};
        offsets=temp;
    }
}
