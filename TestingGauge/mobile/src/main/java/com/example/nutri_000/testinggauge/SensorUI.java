package com.example.nutri_000.testinggauge;

import android.app.Activity;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SensorUI extends MainActivity {
    public ImageButton connect;
    public ProgressBar rightPB, leftPB, rightPBY, leftPBY, rightPBZ, leftPBZ;
    public SeekBar rightSB, leftSB, rightSBY, leftSBY, rightSBZ, leftSBZ;
    public TextView rightTV, leftTV, rightTVY, leftTVY, rightTVZ, leftTVZ;
    public RelativeLayout relativeLayout;
    public float[] average={0,0,0};
    public int[] calibrateCounter={0,0,0};
    boolean[] calibrate={false,false,false};
    boolean search;
    public int green,yellow,white;
    static TextView sensorStatus;
    public SensorUI(int button, int rPB, int rPBY, int rPBZ, int lPB, int lPBY, int lPBZ, int rSB, int rSBY, int rSBZ, int lSB,int lSBY,
                    int lSBZ, int rTV, int lTV, int rTVY, int lTVY, int rTVZ, int lTVZ, int relativeLO, Activity MainActivity){
        connect = (ImageButton) MainActivity.findViewById(button);
        rightPB = (ProgressBar) MainActivity.findViewById(rPB);
        leftPB = (ProgressBar) MainActivity.findViewById(lPB);

        //FOR Y AXIS
        rightPBY = (ProgressBar) MainActivity.findViewById(rPBY);
        leftPBY = (ProgressBar) MainActivity.findViewById(lPBY);
        //END Y AXIS

        //FOR Z AXIS
        rightPBZ = (ProgressBar) MainActivity.findViewById(rPBZ);
        leftPBZ = (ProgressBar) MainActivity.findViewById(lPBZ);
        //END Z AXIS

        rightSB = (SeekBar) MainActivity.findViewById(rSB);
        leftSB = (SeekBar) MainActivity.findViewById(lSB);

        //FOR Y AXIS
        rightSBY = (SeekBar) MainActivity.findViewById(rSBY);
        leftSBY = (SeekBar) MainActivity.findViewById(lSBY);
        //END Y AXIS

        //FOR Z AXIS
        rightSBZ = (SeekBar) MainActivity.findViewById(rSBZ);
        leftSBZ = (SeekBar) MainActivity.findViewById(lSBZ);
        //END Z AXIS

        rightTV = (TextView) MainActivity.findViewById(rTV);
        leftTV = (TextView) MainActivity.findViewById(lTV);

        //FOR Y AXIS
        rightTVY = (TextView) MainActivity.findViewById(rTVY);
        leftTVY = (TextView) MainActivity.findViewById(lTVY);
        //END Y AXIS

        //FOR Z AXIS
        rightTVZ = (TextView) MainActivity.findViewById(rTVZ);
        leftTVZ = (TextView) MainActivity.findViewById(lTVZ);
        //END Z AXIS

        relativeLayout = (RelativeLayout) MainActivity.findViewById(relativeLO);
        search = false;
        sensorStatus = (TextView) MainActivity.findViewById(R.id.SensorStatus);
    }
    public void calibrateSensor(final SensorUI sensor, int axis){
        //zero the sensor
        calibrate[axis] = true;
        calibrateCounter[axis] = 0;
        average[axis] = 0;
        sensor.leftPB.setProgress(0);
        sensor.rightPB.setProgress(0);
    }
}
