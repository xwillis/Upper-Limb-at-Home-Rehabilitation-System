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

public class BicepFlexMeasurement extends AppCompatActivity {
String tag="BicepFlex";
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
SeekBar seekCompBicepZ;

    ProgressBar progCompChestXPos;
    ProgressBar progCompChestXNeg;
    SeekBar seekCompChestXPos;
    SeekBar seekCompChestXNeg;

    ProgressBar progCompChestYPos;
    ProgressBar progCompChestYNeg;
    SeekBar seekCompChestYPos;
    SeekBar seekCompChestYNeg;

    ProgressBar progCompChestZ;
    SeekBar seekCompChestZ;

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
        setContentView(R.layout.activity_bicep_flex_measurement);
        bindViews();
        seekCompBicepXNeg.setProgress(50);
        seekCompBicepXPos.setProgress(50);

        seekCompBicepYNeg.setProgress(50);
        seekCompBicepYPos.setProgress(50);

        seekCompBicepZ.setProgress(50);
        seekCompBicepZ.setMax(360);
        progCompBicepZ.setMax(360);

        seekCompChestXNeg.setProgress(50);
        seekCompChestXPos.setProgress(50);

        seekCompChestYNeg.setProgress(50);
        seekCompChestYPos.setProgress(50);

        seekCompChestZ.setProgress(50);
        seekCompChestZ.setMax(360);
        progCompChestZ.setMax(360);

        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    public void bindViews(){
        //displays movement values we want to see
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

        sensorStatusBicepX =(TextView)findViewById(R.id.SensorStatusBicepX);
        sensorStatusBicepY =(TextView)findViewById(R.id.SensorStatusBicepY);
        sensorStatusBicepZ =(TextView)findViewById(R.id.SensorStatusBicepZ);
        //seek and progressbars for the x compensation for bicep
        progCompChestXPos =(ProgressBar)findViewById(R.id.progressCompBicepXPos);
        progCompChestXNeg =(ProgressBar)findViewById(R.id.progressBarCompBicepXNeg);
        seekCompChestXPos =(SeekBar) findViewById(R.id.seekBarCompBicepXPos);
        seekCompChestXNeg =(SeekBar)findViewById(R.id.seekBarCompBicepXNeg);

        progCompChestYPos =(ProgressBar)findViewById(R.id.progressBarCompBicepYPos);
        progCompChestYNeg =(ProgressBar)findViewById(R.id.progressBarCompBicepYNeg);
        seekCompChestYPos =(SeekBar) findViewById(R.id.seekBarCompBicepYPos);
        seekCompChestYNeg =(SeekBar)findViewById(R.id.seekBarCompBicepYNeg);

        progCompChestZ =(ProgressBar)findViewById(R.id.progressBarCompBicepZ);
        seekCompChestZ =(SeekBar) findViewById(R.id.seekBarCompBicepZ);

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

                    lookForCompensation(notification);

                }else if(notification.gatt.equals("bicep")) {

                    lookForCompensation(notification);
                }else if(notification.gatt.equals("wrist")) {

                    textView.setText(Integer.toString((int)notification.valueX));
                    determineStim(notification);
                }
                else if(notification.gatt.equals("hand")){

                }

            }
        }
    };
    public void lookForCompensation(BleNotification notif){
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
    public void determineStim(BleNotification notif){
        if(notif.valueX>0){
            progressBarBicepPos.setProgress((int)notif.valueX);
            progressBarBicepNeg.setProgress(0);
        }else{
            progressBarBicepNeg.setProgress(-1*(int)notif.valueX);
            progressBarBicepPos.setProgress(0);
        }
        if(!compensating){
            if(notif.valueX>0&&notif.valueX> seekBarBicepPos.getProgress()){
                constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                stimming=true;
            } else if(notif.valueX<0&&notif.valueX<-1* seekBarBicepNeg.getProgress()){
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
