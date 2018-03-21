package com.example.nutri_000.testinggauge;

import android.app.Activity;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
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
    public float average;
    public int calibrateCounter;
    boolean calibrate;
    boolean search;
    public int green,yellow,white;
    static TextView sensorStatus;
    public SensorUI(int button, int rPB, int lPB, int rSB, int lSB, int rTV, int lTV, int rPBY, int lPBY, int rSBY, int lSBY, int rTVY, int lTVY, int rPBZ, int lPBZ, int rSBZ, int lSBZ, int rTVZ, int lTVZ, int relativeLO, Activity MainActivity){
        connect = (ImageButton) MainActivity.findViewById(button);
        //progressbar for x axis
        rightPB = (ProgressBar) MainActivity.findViewById(rPB);
        leftPB = (ProgressBar) MainActivity.findViewById(lPB);

        //progressbar for y axis
        rightPBY = (ProgressBar) MainActivity.findViewById(rPBY);
        leftPBY = (ProgressBar) MainActivity.findViewById(lPBY);

        //progressbar for z axis
        rightPBZ = (ProgressBar) MainActivity.findViewById(rPBZ);
        leftPBZ = (ProgressBar) MainActivity.findViewById(lPBZ);

        //seek bar for x axis
        rightSB = (SeekBar) MainActivity.findViewById(rSB);
        leftSB = (SeekBar) MainActivity.findViewById(lSB);

        //seek bar for Y axis
        rightSBY = (SeekBar) MainActivity.findViewById(rSBY);
        leftSBY = (SeekBar) MainActivity.findViewById(lSBY);

        //seek bar for Z axis
        rightSBZ = (SeekBar) MainActivity.findViewById(rSBZ);
        leftSBZ = (SeekBar) MainActivity.findViewById(lSBZ);

        // text view for x axis
        rightTV = (TextView) MainActivity.findViewById(rTV);
        leftTV = (TextView) MainActivity.findViewById(lTV);

        // text view for Y axis
        rightTVY = (TextView) MainActivity.findViewById(rTVY);
        leftTVY = (TextView) MainActivity.findViewById(lTVY);

        // text view for Z axis
        rightTVZ = (TextView) MainActivity.findViewById(rTVZ);
        leftTVZ = (TextView) MainActivity.findViewById(lTVZ);

        relativeLayout = (RelativeLayout) MainActivity.findViewById(relativeLO);
        average = 0;
        calibrateCounter = 0;
        calibrate = false;
        search = false;
        sensorStatus = (TextView) MainActivity.findViewById(R.id.SensorStatus);
    }
    public void calibrateSensor(final SensorUI sensor){
        //zero the sensor
        calibrate = true;
        calibrateCounter = 0;
        average = 0;

        //ZERO X AXIS SENSOR
        sensor.leftPB.setProgress(0);
        sensor.rightPB.setProgress(0);

        //ZERO Y AXIS SENSOR
        sensor.leftPBY.setProgress(0);
        sensor.rightPBY.setProgress(0);

        //ZERO Z AXIS SENSOR
        sensor.leftPBZ.setProgress(0);
        sensor.rightPBZ.setProgress(0);
    }
}
