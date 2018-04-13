package com.example.nutri_000.testinggauge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class WristFlexion extends AppCompatActivity {
    String tag="ShoulderAb";

    ConstraintLayout constraintLayout;
    ImageButton imageButton;
    private MeasurementSensor handMeasSens;
    private CompensationSensor chestCompSens;
    private CompensationSensor bicepCompSens;
    private CompensationSensor wristCompSens;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrist_flexion);
        bindViews();
        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    public void bindViews(){
        constraintLayout=(ConstraintLayout)findViewById(R.id.bicep_layout);
        ImageButton imageButton=(ImageButton)findViewById(R.id.returnHome);

        ProgressBar[][] chestProgress={{(ProgressBar)findViewById(R.id.progressBarCompChestXNeg), (ProgressBar)findViewById(R.id.progressBarCompChestYNeg), (ProgressBar)findViewById(R.id.progressBarCompChestZ)},
                {(ProgressBar)findViewById(R.id.progressCompChestXPos), (ProgressBar)findViewById(R.id.progressBarCompChestYPos)}};
        SeekBar[][] chestSeek={{(SeekBar)findViewById(R.id.seekBarCompChestXNeg), (SeekBar)findViewById(R.id.seekBarCompChestYNeg), (SeekBar) findViewById(R.id.seekBarCompChestZ)},
                {(SeekBar) findViewById(R.id.seekBarCompChestXPos), (SeekBar) findViewById(R.id.seekBarCompChestYPos)}};
        TextView[] chestViews={(TextView)findViewById(R.id.SensorStatusChestX),(TextView)findViewById(R.id.SensorStatusChestY), (TextView)findViewById(R.id.SensorStatusChestZ)};
        chestCompSens=new CompensationSensor(chestProgress, chestSeek, chestViews);

        TextView[] bicepViews={(TextView)findViewById(R.id.SensorStatusBicepX),(TextView)findViewById(R.id.SensorStatusBicepY), (TextView)findViewById(R.id.SensorStatusBicepZ)};
        ProgressBar[][] bicepProgress={{(ProgressBar)findViewById(R.id.progressBarCompBicepXNeg), (ProgressBar)findViewById(R.id.progressBarCompBicepYNeg), (ProgressBar)findViewById(R.id.progressBarCompBicepZ)},
                {(ProgressBar)findViewById(R.id.progressCompBicepXPos), (ProgressBar)findViewById(R.id.progressBarCompBicepYPos)}};
        SeekBar[][] bicepSeek={{(SeekBar)findViewById(R.id.seekBarCompBicepXNeg), (SeekBar)findViewById(R.id.seekBarCompBicepYNeg), (SeekBar) findViewById(R.id.seekBarCompBicepZ)},
                {(SeekBar) findViewById(R.id.seekBarCompBicepXPos), (SeekBar) findViewById(R.id.seekBarCompBicepYPos)}};
        bicepCompSens=new CompensationSensor(bicepProgress, bicepSeek, bicepViews);

        ProgressBar[][] wristProgress={{(ProgressBar)findViewById(R.id.progressBarCompWristXNeg), (ProgressBar)findViewById(R.id.progressBarCompWristYNeg), (ProgressBar)findViewById(R.id.progressBarCompWristZ)},
                {(ProgressBar)findViewById(R.id.progressCompWristXPos), (ProgressBar)findViewById(R.id.progressBarCompWristYPos)}};
        SeekBar[][] wristSeek={{(SeekBar)findViewById(R.id.seekBarCompWristXNeg), (SeekBar)findViewById(R.id.seekBarCompWristYNeg), (SeekBar) findViewById(R.id.seekBarCompWristZ)},
                {(SeekBar) findViewById(R.id.seekBarCompWristXPos), (SeekBar) findViewById(R.id.seekBarCompWristYPos)}};
        TextView[] wristViews={(TextView)findViewById(R.id.SensorStatusWristX),(TextView)findViewById(R.id.SensorStatusWristY), (TextView)findViewById(R.id.SensorStatusWristZ)};
        wristCompSens=new CompensationSensor(wristProgress, wristSeek, wristViews);

        ProgressBar[] handMeasProg = {(ProgressBar)findViewById(R.id.progressBarMeasuredNeg), (ProgressBar)findViewById(R.id.progressBarMeasuredPos)};
        SeekBar[] handMeasSeek={(SeekBar)findViewById(R.id.seekBarMeasuredNeg), (SeekBar)findViewById(R.id.seekBarMeasuredPos)};
        handMeasSens=new MeasurementSensor(handMeasProg,handMeasSeek,(TextView)findViewById(R.id.measuredValue));

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
                    chestCompSens.determineCompensation(notification, constraintLayout, handMeasSens.stimming);

                }else if(notification.gatt.equals("bicep")) {
                    //put this code in all IMUs above the one we're measuring
                    bicepCompSens.determineCompensation(notification, constraintLayout, handMeasSens.stimming);
                }else if(notification.gatt.equals("wrist")) {
                    //put this code at the IMU we're measuring, and choose valueX,Y,Z based on axis
                    wristCompSens.determineCompensation(notification, constraintLayout, handMeasSens.stimming);
                }
                else if(notification.gatt.equals("hand")){
                    handMeasSens.setText((int)notification.valueX);
                    //make it or because we want to know if there is any compensation, ie if any of the compensations is true
                    handMeasSens.determineStim((int)notification.valueX, constraintLayout, chestCompSens.compensating||bicepCompSens.compensating||wristCompSens.compensating);
                }

            }
        }
    };

    public void returnToMain(View v){
        unregisterReceiver(broadcastReceiver);
        finish();
    }
}
