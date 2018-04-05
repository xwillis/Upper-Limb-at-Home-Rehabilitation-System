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
    String tag="BicepFlex";
    ProgressBar progressBarPos;
    ProgressBar progressBarNeg;

    ProgressBar progCompXPos;
    ProgressBar progCompXNeg;
    SeekBar seekCompXPos;
    SeekBar seekCompXNeg;

    ProgressBar progCompYPos;
    ProgressBar progCompYNeg;
    SeekBar seekCompYPos;
    SeekBar seekCompYNeg;

    ProgressBar progCompZ;
    //ProgressBar progCompZNeg;
    SeekBar seekCompZ;
    //SeekBar seekCompZNeg;

    SeekBar seekBarPos;
    SeekBar seekBarNeg;

    ConstraintLayout constraintLayout;
    ImageButton imageButton;
    TextView textView;
    private TextView sensorStatusX;
    private TextView sensorStatusY;
    private TextView sensorStatusZ;
    boolean compensating=false;
    boolean stimming=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bicep_flex_measurement);
        bindViews();
        seekCompXNeg.setProgress(50);
        seekCompXPos.setProgress(50);

        seekCompYNeg.setProgress(50);
        seekCompYPos.setProgress(50);

        seekCompZ.setProgress(50);
        seekCompZ.setMax(360);
        progCompZ.setMax(360);

        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    public void bindViews(){
        progressBarPos=(ProgressBar)findViewById(R.id.progressBarBicepFlexPos);
        progressBarNeg=(ProgressBar)findViewById(R.id.progressBarBicepFlexNeg);
        seekBarPos=(SeekBar)findViewById(R.id.seekBarBicepFlexPos);
        seekBarNeg=(SeekBar)findViewById(R.id.seekBarBicepFlexNeg);
        constraintLayout=(ConstraintLayout)findViewById(R.id.bicep_layout);
        imageButton=(ImageButton)findViewById(R.id.returnHome);
        textView=(TextView)findViewById(R.id.bicepValue);

        sensorStatusX=(TextView)findViewById(R.id.SensorStatusX);
        sensorStatusY=(TextView)findViewById(R.id.SensorStatusY);
        sensorStatusZ=(TextView)findViewById(R.id.SensorStatusZ);

        progCompXPos=(ProgressBar)findViewById(R.id.progressCompXPos);
        progCompXNeg=(ProgressBar)findViewById(R.id.progressBarCompXNeg);
        seekCompXPos=(SeekBar) findViewById(R.id.seekBarCompXPos);
        seekCompXNeg=(SeekBar)findViewById(R.id.seekBarCompXNeg);

        progCompYPos=(ProgressBar)findViewById(R.id.progressBarCompYPos);
        progCompYNeg=(ProgressBar)findViewById(R.id.progressBarCompYNeg);
        seekCompYPos=(SeekBar) findViewById(R.id.seekBarCompYPos);
        seekCompYNeg=(SeekBar)findViewById(R.id.seekBarCompYNeg);

        progCompZ=(ProgressBar)findViewById(R.id.progressBarCompZ);
        seekCompZ=(SeekBar) findViewById(R.id.seekBarCompZ);

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
                    // Log.v(tag," from the chest");
                    //find value x, switched to different value coding
                    //  Log.v(tag, "Value x from object is "+notification.valueX);
                    // setBicepValue((int)notification.valueX,chestUI,0);
                    //find value y, switched to different value coding
                    //Log.v(tag, "Value y from object is "+notification.valueY);
                    //setBicepValue((int)notification.valueY,chestUI,1);
                    //find value z, switched to different value coding
                    //Log.v(tag, "Value z from object is "+notification.valueZ);
                    //setBicepValue((int)notification.valueZ,chestUI,2);
                    lookForCompensation(notification);

                }else if(notification.gatt.equals("bicep")) {
                    // Log.v(tag, " from the bicep");
                    //find value x, switched to different value coding
                    //Log.v(tag, "Value x from object is " + notification.valueX);
                    //setBicepValue((int) notification.valueX, bicepUI, 0);
                    //find value y, switched to different value coding
                    //Log.v(tag, "Value y from object is " + notification.valueY);
                    //setBicepValue((int) notification.valueY, bicepUI, 1);
                    //find value z, switched to different value coding
                    //Log.v(tag, "Value z from object is " + notification.valueZ);
                    //setBicepValue((int)notification.valueZ,bicepUI,2);
                    lookForCompensation(notification);
                }else if(notification.gatt.equals("wrist")) {
                    //Log.v(tag, " from the wrist");
                    //find value x, switched to different value coding
                    //Log.v(tag, "Value x from object is " + notification.valueX);
                    //setBicepValue((int) notification.valueX, wristUI, 0);
                    //find value y, switched to different value coding
                    //Log.v(tag, "Value y from object is " + notification.valueY);
                    //setBicepValue((int) notification.valueY, wristUI, 1);
                    //find value z, switched to different value coding
                    //Log.v(tag, "Value z from object is " + notification.valueZ);
                    //setBicepValue((int)notification.valueZ,wristUI,2);
                    textView.setText(Integer.toString((int)notification.valueX));
                    determineStim(notification);
                }
                else if(notification.gatt.equals("hand")){
                    Log.v(tag," from the hand");
                    //find value x, switched to different value coding
                    Log.v(tag, "Value x from object is "+notification.valueX);
                    //setBicepValue((int)notification.valueX,handUI,0);
                    //find value y, switched to different value coding
                    Log.v(tag, "Value y from object is "+notification.valueY);
                    //setBicepValue((int)notification.valueY,handUI,1);
                    //find value z, switched to different value coding
                    Log.v(tag, "Value z from object is "+notification.valueZ);
                    //setBicepValue((int)notification.valueZ,handUI,2);
                }

            }
        }
    };
    public void lookForCompensation(BleNotification notif){
        if(notif.valueX>seekCompXPos.getProgress()||notif.valueX<-1*seekCompXNeg.getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notif.valueY>seekCompYPos.getProgress()||notif.valueY<-1*seekCompYNeg.getProgress()){
            constraintLayout.setBackgroundColor(Color.parseColor("#cc0000"));
            compensating=true;
        }else if(notif.valueZ>seekCompZ.getProgress()){//||notif.valueZ<seekCompZ.getProgress()){//one day will need to have 2 seekbars? or range or something idk
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
        setSensorStatusX("X axis is "+notif.valueX+"should be "+seekCompXPos.getProgress()+" to "+-1*seekCompXNeg.getProgress());
        setSensorStatusY("Y axis is "+notif.valueY+"should be "+seekCompYPos.getProgress()+" to "+-1*seekCompYNeg.getProgress());
        setSensorStatusZ("Z axis is "+notif.valueZ+"should be less than"+seekCompZ.getProgress());
        if(notif.valueX>0){
            progCompXPos.setProgress((int)notif.valueX);
            progCompXNeg.setProgress(0);
        }else{
            progCompXNeg.setProgress(-1*(int)notif.valueX);
            progCompXPos.setProgress(0);
        }
        if(notif.valueY>0){
            progCompYPos.setProgress((int)notif.valueY);
            progCompYNeg.setProgress(0);
        }else{
            progCompYNeg.setProgress(-1*(int)notif.valueY);
            progCompYPos.setProgress(0);
        }
        progCompZ.setProgress((int)notif.valueZ);
    }
    public void determineStim(BleNotification notif){
        if(notif.valueX>0){
            progressBarPos.setProgress((int)notif.valueX);
            progressBarNeg.setProgress(0);
        }else{
            progressBarNeg.setProgress(-1*(int)notif.valueX);
            progressBarPos.setProgress(0);
        }
        if(!compensating){
            if(notif.valueX>0&&notif.valueX>seekBarPos.getProgress()){
                constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                stimming=true;
            } else if(notif.valueX<0&&notif.valueX<-1*seekBarNeg.getProgress()){
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

                sensorStatusX.setText(message);

            }
        });
    }
    public void setSensorStatusY(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatusY.setText(message);

            }
        });
    }
    public void setSensorStatusZ(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatusZ.setText(message);

            }
        });
    }
    public void returnToMain(View v){
        finish();
    }
    public void toSupination(View v){
        Intent intent=new Intent(this,SupinationMeasurement.class);
        startActivity(intent);
    }
}
