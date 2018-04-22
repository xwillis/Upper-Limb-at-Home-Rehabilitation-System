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

public class ShoulderFlexion extends AppCompatActivity {
    String tag="ShoulderFlex";

    private MeasurementSensor bicepMeasSens;
    private CompensationSensor chestCompSens;

    private boolean[] calibrate={false,false,false,false};

    ConstraintLayout constraintLayout;
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoulder_flexion);
        bindViews();

        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    public void bindViews(){
        constraintLayout=(ConstraintLayout)findViewById(R.id.shoulder_flex);
        ImageButton imageButton=(ImageButton)findViewById(R.id.returnHome);

        ProgressBar[][] chestProgress={{(ProgressBar)findViewById(R.id.progressBarCompChestXNeg), (ProgressBar)findViewById(R.id.progressBarCompChestYNeg), (ProgressBar)findViewById(R.id.progressBarCompChestZ)},
                {(ProgressBar)findViewById(R.id.progressCompChestXPos), (ProgressBar)findViewById(R.id.progressBarCompChestYPos)}};
        SeekBar[][] chestSeek={{(SeekBar)findViewById(R.id.seekBarCompChestXNeg), (SeekBar)findViewById(R.id.seekBarCompChestYNeg), (SeekBar) findViewById(R.id.seekBarCompChestZ)},
                {(SeekBar) findViewById(R.id.seekBarCompChestXPos), (SeekBar) findViewById(R.id.seekBarCompChestYPos)}};
        TextView[] chestViews={(TextView)findViewById(R.id.SensorStatusChestX),(TextView)findViewById(R.id.SensorStatusChestY), (TextView)findViewById(R.id.SensorStatusChestZ)};
        chestCompSens=new CompensationSensor(chestProgress, chestSeek, chestViews);

        ProgressBar[] measProg = {(ProgressBar)findViewById(R.id.progressBarMeasuredNeg), (ProgressBar)findViewById(R.id.progressBarMeasuredPos)};
        SeekBar[] measSeek={(SeekBar)findViewById(R.id.seekBarMeasuredNeg), (SeekBar)findViewById(R.id.seekBarMeasuredPos)};
        bicepMeasSens=new MeasurementSensor(measProg,measSeek,(TextView)findViewById(R.id.measuredValue));

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
                    if(calibrate[0]){
                        chestCompSens.calibrate(notification);
                        calibrate[0]=false;
                    }
                    chestCompSens.determineCompensation(notification,constraintLayout,bicepMeasSens.stimming);

                }else if(notification.gatt.equals("bicep")) {
                    //put this code in all IMUs above the one we're measuring
                    notification.valueZ=mapToValues((int)notification.valueZ);
                    if(calibrate[1]){
                        bicepMeasSens.calibrate((int)notification.valueZ);
                        calibrate[1]=false;
                    }
                    bicepMeasSens.determineStim((int)notification.valueZ,constraintLayout, chestCompSens.compensating);
                }else if(notification.gatt.equals("wrist")) {
                    //leave IMUs below the measured IMU blank
                }
                else if(notification.gatt.equals("hand")){
                    //leave IMUs below the measured IMU blank
                }

            }
        }
    };
    public void calibrateChest(View v){
        calibrateSens(0);
    }
    public void calibrateBicep(View v){
        calibrateSens(1);
    }
    public void calibrateWrist(View v){
        calibrateSens(2);
    }
    public void calibrateHand(View v){
        calibrateSens(3);
    }
    public void calibrateSens(int sensor){
        calibrate[sensor]=true;
    }

    public int mapToValues(int value){
        value=-value;
        if(value<-180){
            value = value+360;
        }
        return value;
    }
    public void returnToMain(View v){
        unregisterReceiver(broadcastReceiver);
        finish();
    }
}
