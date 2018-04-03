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

public class SupinationMeasurement extends AppCompatActivity {
    String tag="Supination";
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
    TextView supinationValue;
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
        supinationValue=(TextView)findViewById(R.id.bicepValue);

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
                BleNotification notification = intent.getParcelableExtra("notifyObject");
                if (notification.gatt.equals("chest")) {
                    lookForCompensation(notification);

                }else if(notification.gatt.equals("bicep")) {

                    lookForCompensation(notification);
                }else if(notification.gatt.equals("wrist")) {

                    supinationValue.setText(Integer.toString((int)notification.valueY));
                    determineStim(notification);
                }
                else if(notification.gatt.equals("hand")){

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
        if(notif.valueY>0){
            progressBarPos.setProgress((int)notif.valueY);
            progressBarNeg.setProgress(0);
        }else{
            progressBarNeg.setProgress(-1*(int)notif.valueY);
            progressBarPos.setProgress(0);
        }
        if(!compensating){
            if(notif.valueY>0&&notif.valueY>seekBarPos.getProgress()){
                constraintLayout.setBackgroundColor(Color.parseColor("#66ff33"));
                stimming=true;
            } else if(notif.valueY<0&&notif.valueY<-1*seekBarNeg.getProgress()){
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
 /*   public void setBicepValue(int value){

    }
    //new added stuff
    // glow red outside +/- 5 degrees
                if( (sensor.progressBars[0][axis].getProgress() >= 5 | sensor.progressBars[0][axis].getProgress() <= 5) & sensor == chestUI){
        Log.v(tag,"Chest IMU outside range +/- 5 degrees");
        if( (sensor.progressBars[0][axis].getProgress() >= 5 | sensor.progressBars[0][axis].getProgress() <= 5) & sensor == bicepUI) {
            Log.v(tag,"Bicep IMU outside range +/- 5 degrees");
            sensor.setSensorBackgroundColor("red");
        }
    }
    // glow green if within range & wrist greater than seekbar set value
                else if( !((sensor.progressBars[0][axis].getProgress() >= 5 | sensor.progressBars[0][axis].getProgress() <= 5)) & sensor == chestUI){
        //chest and bicep IMU not outside range
        //check if current wrist value greater than set seekbar value
        if( !((sensor.progressBars[0][axis].getProgress() >= 5 | sensor.progressBars[0][axis].getProgress() <= 5)) & sensor == bicepUI) {
            Log.v(tag,"User within compensation values, stim");
            sensor.setSensorBackgroundColor("#008542"); //flash green
        }

    }
//end of new added stuf,
//check if bicep and chest are outside +/- 5 degrees
// flash red to indicate outside range
public void flashRed(final int value, final SensorUI sensor, final int axis){

    //check Chest IMU
    if(sensor == chestUI){
        if(value >= 5 | value <= 5){

        }

    }

}*/
}
