package com.example.nutri_000.testinggauge;

import android.app.Activity;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SensorUI extends MainActivity {
    public ImageButton connect;
    public ProgressBar[][] progressBars;
    public SeekBar[][] seekBars;
    public TextView[][] textViews;
    /*public ProgressBar rightPB, leftPB, rightPBY, leftPBY, rightPBZ, leftPBZ;
    public SeekBar rightSB, leftSB, rightSBY, leftSBY, rightSBZ, leftSBZ;
    public TextView rightTV, leftTV, rightTVY, leftTVY, rightTVZ, leftTVZ;*/
    public ConstraintLayout constraintLayout;
    /*public float[] average={0,0,0};
    public int[] calibrateCounter={0,0,0};*/
    boolean[] calibrate={true,true,true};
    boolean search;
    public int green,yellow,white;
    static TextView sensorStatus;
    public SensorUI(int button, int[] progressRight, int[] progressLeft, int seekRight[],int seekLeft[], int[] textRight, int[] textLeft, int consLayOut, Activity MainActivity){
        connect = (ImageButton) MainActivity.findViewById(button);
        //set right progress bars
        progressBars=new ProgressBar[2][3];
        seekBars=new SeekBar[2][3];
        textViews=new TextView[2][3];
        for(int i=0;i<progressRight.length;i++){
            progressBars[0][i]=(ProgressBar)MainActivity.findViewById(progressRight[i]);
        }
        //set left progress bars
        for(int i=0;i<progressLeft.length;i++){
            progressBars[1][i]=(ProgressBar)MainActivity.findViewById(progressLeft[i]);
        }
        //set right seek bars
        for(int i=0;i<seekRight.length;i++){
            seekBars[0][i]=(SeekBar) MainActivity.findViewById(seekRight[i]);
        }
        //set left seek bars
        for(int i=0;i<seekLeft.length;i++){
            seekBars[1][i]=(SeekBar) MainActivity.findViewById(seekLeft[i]);
        }
        //set right text views
        for(int i=0;i<textRight.length;i++){
            textViews[0][i]=(TextView) MainActivity.findViewById(textRight[i]);
        }
        //set left text views
        for(int i=0;i<textLeft.length;i++){
            textViews[1][i]=(TextView) MainActivity.findViewById(textLeft[i]);
        }

        //END Z AXIS

        constraintLayout = (ConstraintLayout) MainActivity.findViewById(consLayOut);
        search = false;
        sensorStatus = (TextView) MainActivity.findViewById(R.id.SensorStatus);
    }
    public void initializeSensor(){
        //zero the sensor
        /*calibrate[axis] = true;
        calibrateCounter[axis] = 0;
        average[axis] = 0;*/
        for(int i=0;i<progressBars.length;i++){
            for(int ii=0;ii<progressBars[0].length;ii++){
                progressBars[i][ii].setProgress(0);
            }
        }
    }
    public void setSensorBackgroundColor(String color){
        constraintLayout.setBackgroundColor(Color.parseColor(color));
    }
}
