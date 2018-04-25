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

public class WristDeflection extends AppCompatActivity {
    String tag="WristDefl";

    private MeasurementSensor handMeasSens;
    private CompensationSensor chestCompSens;
    private CompensationSensor bicepCompSens;
    private CompensationSensor wristCompSens;

    private boolean[] calibrate={false,false,false,false};

    ConstraintLayout constraintLayout;
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrist_deflection);
        bindViews();

        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    public void bindViews(){
        constraintLayout=(ConstraintLayout)findViewById(R.id.wrist_def);
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

        ProgressBar[] measProg = {(ProgressBar)findViewById(R.id.progressBarMeasuredNeg), (ProgressBar)findViewById(R.id.progressBarMeasuredPos)};
        SeekBar[] measSeek={(SeekBar)findViewById(R.id.seekBarMeasuredNeg), (SeekBar)findViewById(R.id.seekBarMeasuredPos)};
        handMeasSens=new MeasurementSensor(measProg,measSeek,(TextView)findViewById(R.id.measuredValue));

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


                if (notification.gatt.equals("chest")) {
                    if(calibrate[0]){
                        chestCompSens.calibrate(notification);
                        calibrate[0]=false;
                    }

                    chestCompSens.determineCompensation(notification,constraintLayout,handMeasSens.stimming);

                }else if(notification.gatt.equals("bicep")) {
                    if(calibrate[1]){
                        bicepCompSens.calibrate(notification);
                        calibrate[1]=false;
                    }

                    bicepCompSens.determineCompensation(notification,constraintLayout,handMeasSens.stimming);
                }else if(notification.gatt.equals("wrist")) {
                    if(calibrate[2]){
                        wristCompSens.calibrate(notification);
                        calibrate[2]=false;
                    }
                    wristCompSens.determineCompensation(notification, constraintLayout, handMeasSens.stimming);
                }
                else if(notification.gatt.equals("hand")){
                    notification.valueZ=mapToValues((int)notification.valueZ);
                    if(calibrate[3]){
                        handMeasSens.calibrate((int)notification.valueZ);
                        calibrate[3]=false;
                    }
                    handMeasSens.determineStim((int)notification.valueZ, constraintLayout, chestCompSens.compensating||bicepCompSens.compensating||wristCompSens.compensating);
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
    //todo test mapping
    public int mapToValues(int value){
        if(value>180){
            value=value-360;
        }
        return value;

    }
    public void returnToMain(View v){
        unregisterReceiver(broadcastReceiver);
        finish();
    }
}
