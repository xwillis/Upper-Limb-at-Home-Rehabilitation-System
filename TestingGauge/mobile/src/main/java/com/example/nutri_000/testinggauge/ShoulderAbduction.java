package com.example.nutri_000.testinggauge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class ShoulderAbduction extends AppCompatActivity {
    String tag="ShoulderAb";
    ProgressBar progressBarBicepPos;
    ProgressBar progressBarBicepNeg;

    ProgressBar progCompBicepXPos;
    ProgressBar progCompBicepXNeg;
    SeekBar seekCompBicepXPos;
    SeekBar seekCompBicepXNeg;

    ProgressBar progCompBicepYPos;
    ProgressBar progCompBicepYNeg;
    SeekBar seekCompBicepYPos;
    SeekBar seekCompBicepYNeg;

    ProgressBar progCompBicepZ;
    //ProgressBar progCompZNeg;
    SeekBar seekCompBicepZ;
    //SeekBar seekCompZNeg;

    SeekBar seekBarBicepPos;
    SeekBar seekBarBicepNeg;

    ConstraintLayout constraintLayout;
    ImageButton imageButton;
    TextView textView;
    private TextView sensorStatusBicepX;
    private TextView sensorStatusBicepY;
    private TextView sensorStatusBicepZ;
    boolean compensating=false;
    boolean stimming=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoulder_abduction);
        bindViews();
        seekCompBicepXNeg.setProgress(50);
        seekCompBicepXPos.setProgress(50);

        seekCompBicepYNeg.setProgress(50);
        seekCompBicepYPos.setProgress(50);

        seekCompBicepZ.setProgress(50);
        seekCompBicepZ.setMax(180);
        progCompBicepZ.setMax(180);

        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    public void bindViews(){
        progressBarBicepPos =(ProgressBar)findViewById(R.id.progressBarBicepFlexPos);
        progressBarBicepNeg =(ProgressBar)findViewById(R.id.progressBarBicepFlexNeg);
        seekBarBicepPos =(SeekBar)findViewById(R.id.seekBarBicepFlexPos);
        seekBarBicepNeg =(SeekBar)findViewById(R.id.seekBarBicepFlexNeg);
        constraintLayout=(ConstraintLayout)findViewById(R.id.bicep_layout);
        imageButton=(ImageButton)findViewById(R.id.returnHome);
        textView=(TextView)findViewById(R.id.bicepValue);
        //prints out compensation values
        sensorStatusBicepX =(TextView)findViewById(R.id.SensorStatusBicepX);
        sensorStatusBicepY =(TextView)findViewById(R.id.SensorStatusBicepY);
        sensorStatusBicepZ =(TextView)findViewById(R.id.SensorStatusBicepZ);
        //seek and progressbars for the x compensation for bicep
        progCompBicepXPos =(ProgressBar)findViewById(R.id.progressCompBicepXPos);
        progCompBicepXNeg =(ProgressBar)findViewById(R.id.progressBarCompBicepXNeg);
        seekCompBicepXPos =(SeekBar) findViewById(R.id.seekBarCompBicepXPos);
        seekCompBicepXNeg =(SeekBar)findViewById(R.id.seekBarCompBicepXNeg);

        progCompBicepYPos =(ProgressBar)findViewById(R.id.progressBarCompBicepYPos);
        progCompBicepYNeg =(ProgressBar)findViewById(R.id.progressBarCompBicepYNeg);
        seekCompBicepYPos =(SeekBar) findViewById(R.id.seekBarCompBicepYPos);
        seekCompBicepYNeg =(SeekBar)findViewById(R.id.seekBarCompBicepYNeg);

        progCompBicepZ =(ProgressBar)findViewById(R.id.progressBarCompBicepZ);
        seekCompBicepZ =(SeekBar) findViewById(R.id.seekBarCompBicepZ);

    }
    //only change this
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.v(tag,"Got bundle of things");
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            //Log.v(tag,"Event type is "+eventType);

            if (eventType.equals("notification")) {
                // Log.v(tag,"You have mail event");
                BleNotification notification = intent.getParcelableExtra("notifyObject");
                //notification object is null for wrist, but works normally for chest...
                // Log.v(tag, "notification gatt is "+notification.gatt);
                if (notification.gatt.equals("chest")) {
                    //put this code in all IMUs above the one we're measuring
                    lookForCompensation(notification);

                }else if(notification.gatt.equals("bicep")) {
                    //put this code at the IMU we're measuring, and choose valueX,Y,Z based on axis
                    textView.setText(Integer.toString((int)notification.valueX));
                    determineStim((int)notification.valueX);
                }
                else if(notification.gatt.equals("hand")){
                    //leave IMUs below the measured IMU blank
                }
                else if(notification.gatt.equals("wrist")) {
                    //leave IMUs below the measured IMU blank
                }

            }
        }
    };
    public void lookForCompensation(BleNotification notif){
        if(notif.valueX>seekCompBicepXPos.getProgress()||notif.valueX<-1*seekCompBicepXNeg.getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notif.valueY>seekCompBicepYPos.getProgress()||notif.valueY<-1*seekCompBicepYNeg.getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notif.valueZ>seekCompBicepZ.getProgress()){//||notif.valueZ<seekCompBicepZ.getProgress()){//one day will need to have 2 seekbars? or range or something idk
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else {
            compensating = false;
            if (!stimming) {
                constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            setSensorStatusX("not compensating");
            setSensorStatusY("not compensating");
            setSensorStatusZ("not compensating");
        }
        setSensorStatusX("X axis is "+notif.valueX+"should be "+seekCompBicepXPos.getProgress()+" to "+-1*seekCompBicepXNeg.getProgress());
        setSensorStatusY("Y axis is "+notif.valueY+"should be "+seekCompBicepYPos.getProgress()+" to "+-1*seekCompBicepYNeg.getProgress());
        setSensorStatusZ("Z axis is "+notif.valueZ+"should be less than"+seekCompBicepZ.getProgress());
        if(notif.valueX>0){
            progCompBicepXPos.setProgress((int)notif.valueX);
            progCompBicepXNeg.setProgress(0);
        }else{
            progCompBicepXNeg.setProgress(-1*(int)notif.valueX);
            progCompBicepXPos.setProgress(0);
        }
        if(notif.valueY>0){
            progCompBicepYPos.setProgress((int)notif.valueY);
            progCompBicepYNeg.setProgress(0);
        }else{
            progCompBicepYNeg.setProgress(-1*(int)notif.valueY);
            progCompBicepYPos.setProgress(0);
        }
        progCompBicepZ.setProgress((int)notif.valueZ);
    }

    public void determineStim(int value){
        //this section just sets the progressbar values
        if(value>0){
            progressBarBicepPos.setProgress(value);
            progressBarBicepNeg.setProgress(0);
        }else{
            progressBarBicepNeg.setProgress(-1*value);
            progressBarBicepPos.setProgress(0);
        }
        //this section determines if stim or not
        if(!compensating){
            if(value>0&&value>seekBarBicepPos.getProgress()){
                constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                stimming=true;
            } else if(value<0&&value<-1*seekBarBicepNeg.getProgress()){
                constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                stimming=true;
            }else{
                constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                stimming=false;
            }
        }
    }
    public void setSensorStatusX(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatusBicepX.setText(message);

            }
        });
    }
    public void setSensorStatusY(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatusBicepY.setText(message);

            }
        });
    }
    public void setSensorStatusZ(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatusBicepZ.setText(message);

            }
        });
    }
    public void returnToMain(View v){
        finish();
    }

}
