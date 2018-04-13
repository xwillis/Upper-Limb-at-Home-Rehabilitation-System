package com.example.nutri_000.testinggauge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class ShoulderRotation extends AppCompatActivity {
    String tag="ShoulderAb";
    ProgressBar progressBarMeasuredPos;
    ProgressBar progressBarMeasuredNeg;

    ProgressBar progCompMeasuredXPos;
    ProgressBar progCompMeasuredXNeg;
    SeekBar seekCompMeasuredXPos;
    SeekBar seekCompMeasuredXNeg;

    ProgressBar progCompMeasuredYPos;
    ProgressBar progCompMeasuredYNeg;
    SeekBar seekCompMeasuredYPos;
    SeekBar seekCompMeasuredYNeg;

    ProgressBar progCompMeasuredZ;
    //ProgressBar progCompZNeg;
    SeekBar seekCompMeasuredZ;
    //SeekBar seekCompZNeg;

    SeekBar seekBarMeasuredPos;
    SeekBar seekBarMeasuredNeg;

    private CompensationSensor chestCompSens;
    private CompensationSensor MeasuredCompSens;
    private CompensationSensor wristCompSens;

    ConstraintLayout constraintLayout;
    ImageButton imageButton;
    TextView textView;
    private TextView sensorStatusMeasuredX;
    private TextView sensorStatusMeasuredY;
    private TextView sensorStatusMeasuredZ;
    boolean compensating=false;
    boolean stimming=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoulder_rotation);
        bindViews();

        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    public void bindViews(){
        progressBarMeasuredPos =(ProgressBar)findViewById(R.id.progressBarMeasuredPos);
        progressBarMeasuredNeg =(ProgressBar)findViewById(R.id.progressBarMeasuredNeg);
        seekBarMeasuredPos =(SeekBar)findViewById(R.id.seekBarMeasuredPos);
        seekBarMeasuredNeg =(SeekBar)findViewById(R.id.seekBarMeasuredNeg);
        constraintLayout=(ConstraintLayout)findViewById(R.id.shoulderAbd_layout);
        imageButton=(ImageButton)findViewById(R.id.returnHome);
        textView=(TextView)findViewById(R.id.measuredValue);
//prints out compensation values
        TextView sensorStatusChestX =(TextView)findViewById(R.id.SensorStatusChestX);
        TextView sensorStatusChestY =(TextView)findViewById(R.id.SensorStatusChestY);
        TextView sensorStatusChestZ =(TextView)findViewById(R.id.SensorStatusChestZ);
        TextView[] chestViews={sensorStatusChestX,sensorStatusChestY, sensorStatusChestZ};
        ProgressBar progCompChestXPos =(ProgressBar)findViewById(R.id.progressCompChestXPos);
        ProgressBar progCompChestXNeg =(ProgressBar)findViewById(R.id.progressBarCompChestXNeg);
        SeekBar seekCompChestXPos =(SeekBar) findViewById(R.id.seekBarCompChestXPos);
        SeekBar seekCompChestXNeg =(SeekBar)findViewById(R.id.seekBarCompChestXNeg);

        ProgressBar progCompChestYPos =(ProgressBar)findViewById(R.id.progressBarCompChestYPos);
        ProgressBar progCompChestYNeg =(ProgressBar)findViewById(R.id.progressBarCompChestYNeg);
        SeekBar seekCompChestYPos =(SeekBar) findViewById(R.id.seekBarCompChestYPos);
        SeekBar seekCompChestYNeg =(SeekBar)findViewById(R.id.seekBarCompChestYNeg);

        ProgressBar progCompChestZ =(ProgressBar)findViewById(R.id.progressBarCompChestZ);
        SeekBar seekCompChestZ =(SeekBar) findViewById(R.id.seekBarCompChestZ);
        ProgressBar[][] chestProgress={{progCompChestXNeg, progCompChestYNeg, progCompChestZ},{progCompChestXPos, progCompChestYPos}};
        SeekBar[][] chestSeek={{seekCompChestXNeg, seekCompChestYNeg, seekCompChestZ},{seekCompChestXPos, seekCompChestYPos}};
        chestCompSens=new CompensationSensor(chestProgress, chestSeek, chestViews);

    }
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
                    chestCompSens.determineCompensation(notification,constraintLayout,stimming);

                }else if(notification.gatt.equals("bicep")) {
                    //put this code at the IMU we're measuring, and choose valueX,Y,Z based on axis
                    determineStim((int)notification.valueY);
                }else if(notification.gatt.equals("wrist")) {
                    //leave IMUs below the measured IMU blank
                }
                else if(notification.gatt.equals("hand")){
                    //leave IMUs below the measured IMU blank
                }

            }
        }
    };



    public void returnToMain(View v){
        unregisterReceiver(broadcastReceiver);
        finish();
    }

}
