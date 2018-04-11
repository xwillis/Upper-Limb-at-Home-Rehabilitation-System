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

public class WristFlexion extends AppCompatActivity {
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


    ProgressBar progCompChestXPos;
    ProgressBar progCompChestXNeg;
    SeekBar seekCompChestXPos;
    SeekBar seekCompChestXNeg;

    ProgressBar progCompChestYPos;
    ProgressBar progCompChestYNeg;
    SeekBar seekCompChestYPos;
    SeekBar seekCompChestYNeg;

    ProgressBar progCompChestZ;
    //ProgressBar progCompZNeg;
    SeekBar seekCompChestZ;
    //SeekBar seekCompZNeg;

    ProgressBar progCompWristXPos;
    ProgressBar progCompWristXNeg;
    SeekBar seekCompWristXPos;
    SeekBar seekCompWristXNeg;

    ProgressBar progCompWristYPos;
    ProgressBar progCompWristYNeg;
    SeekBar seekCompWristYPos;
    SeekBar seekCompWristYNeg;

    ProgressBar progCompWristZ;
    //ProgressBar progCompZNeg;
    SeekBar seekCompWristZ;
    //SeekBar seekCompZNeg;

    SeekBar seekBarBicepPos;
    SeekBar seekBarBicepNeg;

    ConstraintLayout constraintLayout;
    ImageButton imageButton;
    TextView textView;
    private TextView sensorStatusBicepX;
    private TextView sensorStatusBicepY;
    private TextView sensorStatusBicepZ;

    private TextView sensorStatusChestX;
    private TextView sensorStatusChestY;
    private TextView sensorStatusChestZ;

    private TextView sensorStatusWristX;
    private TextView sensorStatusWristY;
    private TextView sensorStatusWristZ;

    boolean compensating=false;
    boolean stimming=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrist_flexion);
        bindViews();
        seekCompBicepXNeg.setProgress(50);
        seekCompBicepXNeg.setMax(180);
        seekCompBicepXPos.setProgress(50);
        seekCompBicepXPos.setMax(180);

        seekCompBicepYNeg.setProgress(50);
        seekCompBicepYNeg.setMax(180);
        seekCompBicepYPos.setProgress(50);
        seekCompBicepYPos.setMax(180);

        seekCompBicepZ.setProgress(50);
        seekCompBicepZ.setMax(360);
        progCompBicepZ.setMax(360);

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

        sensorStatusBicepX =(TextView)findViewById(R.id.SensorStatusBicepX);
        sensorStatusBicepY =(TextView)findViewById(R.id.SensorStatusBicepY);
        sensorStatusBicepZ =(TextView)findViewById(R.id.SensorStatusBicepZ);

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

        sensorStatusChestX =(TextView)findViewById(R.id.SensorStatusChestX);
        sensorStatusChestY =(TextView)findViewById(R.id.SensorStatusChestY);
        sensorStatusChestZ =(TextView)findViewById(R.id.SensorStatusChestZ);

        progCompChestXPos =(ProgressBar)findViewById(R.id.progressCompChestXPos);
        progCompChestXNeg =(ProgressBar)findViewById(R.id.progressBarCompChestXNeg);
        seekCompChestXPos =(SeekBar) findViewById(R.id.seekBarCompChestXPos);
        seekCompChestXNeg =(SeekBar)findViewById(R.id.seekBarCompChestXNeg);

        progCompChestYPos =(ProgressBar)findViewById(R.id.progressBarCompChestYPos);
        progCompChestYNeg =(ProgressBar)findViewById(R.id.progressBarCompChestYNeg);
        seekCompChestYPos =(SeekBar) findViewById(R.id.seekBarCompChestYPos);
        seekCompChestYNeg =(SeekBar)findViewById(R.id.seekBarCompChestYNeg);

        progCompChestZ =(ProgressBar)findViewById(R.id.progressBarCompChestZ);
        seekCompChestZ =(SeekBar) findViewById(R.id.seekBarCompChestZ);

        sensorStatusWristX =(TextView)findViewById(R.id.SensorStatusWristX);
        sensorStatusWristY =(TextView)findViewById(R.id.SensorStatusWristY);
        sensorStatusWristZ =(TextView)findViewById(R.id.SensorStatusWristZ);

        progCompWristXPos =(ProgressBar)findViewById(R.id.progressCompWristXPos);
        progCompWristXNeg =(ProgressBar)findViewById(R.id.progressBarCompWristXNeg);
        seekCompWristXPos =(SeekBar) findViewById(R.id.seekBarCompWristXPos);
        seekCompWristXNeg =(SeekBar)findViewById(R.id.seekBarCompWristXNeg);

        progCompWristYPos =(ProgressBar)findViewById(R.id.progressBarCompWristYPos);
        progCompWristYNeg =(ProgressBar)findViewById(R.id.progressBarCompWristYNeg);
        seekCompWristYPos =(SeekBar) findViewById(R.id.seekBarCompWristYPos);
        seekCompWristYNeg =(SeekBar)findViewById(R.id.seekBarCompWristYNeg);

        progCompWristZ =(ProgressBar)findViewById(R.id.progressBarCompWristZ);
        seekCompWristZ =(SeekBar) findViewById(R.id.seekBarCompWristZ);

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
                    lookForCompensation(notification,0);

                }else if(notification.gatt.equals("bicep")) {
                    //put this code in all IMUs above the one we're measuring
                    lookForCompensation(notification,1);
                }else if(notification.gatt.equals("wrist")) {
                    //put this code at the IMU we're measuring, and choose valueX,Y,Z based on axis
                    lookForCompensation(notification,2);
                }
                else if(notification.gatt.equals("hand")){
                    textView.setText(Integer.toString((int)notification.valueX));
                    determineStim((int)notification.valueX);
                }

            }
        }
    };
    public void lookForCompensation(BleNotification notif, int position){
        if(notif.valueX> seekCompBicepXPos.getProgress()||notif.valueX<-1* seekCompBicepXNeg.getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notif.valueY> seekCompBicepYPos.getProgress()||notif.valueY<-1* seekCompBicepYNeg.getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notif.valueZ> seekCompBicepZ.getProgress()){//||notif.valueZ<seekCompBicepZ.getProgress()){//one day will need to have 2 seekbars? or range or something idk
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else {
            compensating = false;
            if (!stimming) {
                constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            setSensorStatusBicepX("not compensating");
            setSensorStatusBicepY("not compensating");
            setSensorStatusBicepZ("not compensating");
        }
        setSensorStatusBicepX("X axis is "+notif.valueX+"should be "+ seekCompBicepXPos.getProgress()+" to "+-1* seekCompBicepXNeg.getProgress());
        setSensorStatusBicepY("Y axis is "+notif.valueY+"should be "+ seekCompBicepYPos.getProgress()+" to "+-1* seekCompBicepYNeg.getProgress());
        setSensorStatusBicepZ("Z axis is "+notif.valueZ+"should be less than"+ seekCompBicepZ.getProgress());
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
        if(value>0){
            progressBarBicepPos.setProgress(value);
            progressBarBicepNeg.setProgress(0);
        }else{
            progressBarBicepNeg.setProgress(-1*value);
            progressBarBicepPos.setProgress(0);
        }
        if(!compensating){
            if(value>0&&value> seekBarBicepPos.getProgress()){
                constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                stimming=true;
            } else if(value<0&&value<-1* seekBarBicepNeg.getProgress()){
                constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                stimming=true;
            }else{
                constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                stimming=false;
            }
        }
    }
    public void setSensorStatusBicepX(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatusBicepX.setText(message);

            }
        });
    }
    public void setSensorStatusBicepY(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatusBicepY.setText(message);

            }
        });
    }
    public void setSensorStatusBicepZ(final String message) {
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
