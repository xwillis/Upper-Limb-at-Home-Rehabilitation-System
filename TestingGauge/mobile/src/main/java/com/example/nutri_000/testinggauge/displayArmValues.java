package com.example.nutri_000.testinggauge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class displayArmValues extends AppCompatActivity {
 private ArmCalculator armCalculator;
 private boolean first=true;
 double[] armAngles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_arm_values);
        bindViews();
    }

    private void bindViews() {
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.v(tag,"Got bundle of things");
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            //Log.v(tag,"Event type is "+eventType);
            //should never see sensor connected events here
            if (eventType.equals("sensorDisconnected")) {
                //display text on screen to show sensor has disconnected
                //possibly finish activity to force return to main screen
            }

            if (eventType.equals("notification")) {
                if(first){
                    armCalculator=new ArmCalculator();
                    first=false;
                }
                BleNotification notification = intent.getParcelableExtra("notifyObject");

                if (notification.gatt.equals("chest")) {
                    armAngles=armCalculator.updateChest(notification);

                }else if(notification.gatt.equals("bicep")) {
                    armAngles=armCalculator.updateBicep(notification);

                }else if(notification.gatt.equals("wrist")) {
                    armAngles=armCalculator.updateWrist(notification);

                }
                else if(notification.gatt.equals("hand")){
                    armAngles=armCalculator.updateHand(notification);
                }

            }
        }
    };
    /*//SENSOR STATUS TEXT
    public void setSensorStatus(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatus.setText(message);

            }
        });
    }*/
}
