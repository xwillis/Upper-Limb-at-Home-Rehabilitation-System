package com.example.nutri_000.testinggauge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BicepFlexMeasurement extends AppCompatActivity {
String tag="BicepFlex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bicep_flex_measurement);
        //set up bicepUI
        registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(tag,"Got bundle of things");
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            Log.v(tag,"Event type is "+eventType);

            if (eventType.equals("notification")) {
                Log.v(tag,"You have mail event");
                BleNotification notification = intent.getParcelableExtra("notifyObject");
                //notification object is null for wrist, but works normally for chest...
                // Log.v(tag, "notification gatt is "+notification.gatt);
                if (notification.gatt.equals("chest")) {
                    Log.v(tag," from the chest");
                    //find value x, switched to different value coding
                    Log.v(tag, "Value x from object is "+notification.valueX);
                   // setBicepValue((int)notification.valueX,chestUI,0);
                    //find value y, switched to different value coding
                    Log.v(tag, "Value y from object is "+notification.valueY);
                    //setBicepValue((int)notification.valueY,chestUI,1);
                    //find value z, switched to different value coding
                    Log.v(tag, "Value z from object is "+notification.valueZ);
                    //setBicepValue((int)notification.valueZ,chestUI,2);

                }else if(notification.gatt.equals("bicep")) {
                    Log.v(tag, " from the bicep");
                    //find value x, switched to different value coding
                    Log.v(tag, "Value x from object is " + notification.valueX);
                    //setBicepValue((int) notification.valueX, bicepUI, 0);
                    //find value y, switched to different value coding
                    Log.v(tag, "Value y from object is " + notification.valueY);
                    //setBicepValue((int) notification.valueY, bicepUI, 1);
                    //find value z, switched to different value coding
                    Log.v(tag, "Value z from object is " + notification.valueZ);
                    //setBicepValue((int)notification.valueZ,bicepUI,2);
                }else if(notification.gatt.equals("wrist")) {
                    Log.v(tag, " from the wrist");
                    //find value x, switched to different value coding
                    Log.v(tag, "Value x from object is " + notification.valueX);
                    //setBicepValue((int) notification.valueX, wristUI, 0);
                    //find value y, switched to different value coding
                    Log.v(tag, "Value y from object is " + notification.valueY);
                    //setBicepValue((int) notification.valueY, wristUI, 1);
                    //find value z, switched to different value coding
                    Log.v(tag, "Value z from object is " + notification.valueZ);
                    //setBicepValue((int)notification.valueZ,wristUI,2);
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
