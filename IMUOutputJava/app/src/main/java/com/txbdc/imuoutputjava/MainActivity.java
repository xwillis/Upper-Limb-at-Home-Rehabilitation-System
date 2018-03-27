package com.txbdc.imuoutputjava;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    BleService bleService;
    boolean isBound = false;
    ArrayList<String> chestData = new ArrayList<String>();
    int scanCount = 20;
    int chestClickCount = 0;
    Handler timerHandler = new Handler();
    Status statusVariables = new Status();
    SensorUI chestUI;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static Context context;
    private TextView sensorStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState!= null){

        }
        else {
            // all UI components for main activity
            setContentView(R.layout.activity_main);

            chestUI = new SensorUI(R.id.chestButton, R.id.progressBarTopRight, R.id.progressBarTopRightY, R.id.progressBarTopRightZ, R.id.progressBarTopLeft, R.id.progressBarTopLeftY,
                    R.id.progressBarTopLeftZ, R.id.seekBarChestXRight, R.id.seekBarChestYRight, R.id.seekBarChestZRight, R.id.seekBarChestXLeft, R.id.seekBarChestYLeft, R.id.seekBarChestZLeft,
                    R.id.chestAngleXRight, R.id.chestAngleXLeft, R.id.chestAngleYRight, R.id.chestAngleYLeft, R.id.chestAngleZRight, R.id.chestAngleZLeft, R.id.relativeHip, this);
            chestUI.leftPB.setRotation(180);

            chestUI.green = R.drawable.chestgreen;
            chestUI.yellow = R.drawable.chestyellow;
            chestUI.white = R.drawable.chestwhite;
            sensorStatus=findViewById(R.id.SensorStatus);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Log.v("Cole", "permission granted");
            }
            registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
        }
    }
    //open the ble binder
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.BleBinder binder = (BleService.BleBinder) service;
            bleService = binder.getService();
            isBound = true;
            bleService.initializeBle();
            //bleService.scanner.startScan(bleService.mScanCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
            isBound = false;
        }
    };
    //close BT connections on destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bleService.chestGatt != null) {
            bleService.chestGatt.disconnect();
            bleService.chestGatt.close();
            bleService.chestGatt = null;
        }
        Log.v("onDestroy", "DESTROYED");
    }
    //log that it stopped
    @Override
    protected void onStop() {

        super.onStop();
        Log.v("onStop", "STOPPED");
    }
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        int restore = 42;
        savedInstanceState.putInt("SOMETHING", restore);
    }
    //handling permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("Cole", "coarse location permission granted");
                    Intent bleIntent = new Intent(this, BleService.class);
                    startService(bleIntent);
                    bindService(bleIntent, mServiceConnection, this.BIND_AUTO_CREATE);
                }
                else
                {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {

                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                            //add code to handle dismiss
                        }

                    });
                    builder.show();

                }
                return;
            }
        }
    }
    public static Context getAppContext(){
        return MainActivity.context;
    }
    //set progress bar for X axis
    public void setGaugeValueX(final int value, final SensorUI sensor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(value < 0 & value > -90) {
                    sensor.leftPB.setProgress(-1*value);
                    sensor.rightPB.setProgress(0);

                    if(sensor == chestUI){
                        chestData.add( Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                else if(value > 0 & value < 90 ){
                    sensor.leftPB.setProgress(0);
                    sensor.rightPB.setProgress(value);
                    if(sensor == chestUI){
                        chestData.add( Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                if (value > sensor.rightSB.getProgress() & value < 90 ){
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));


                }
                if ( (value*-1) > sensor.leftSB.getProgress() & (value*-1) < 90 ){
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));

                }

                if (value < sensor.rightSB.getProgress() & (value*-1) < sensor.leftSB.getProgress()){

                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));}
                if(value >= 0 ){
                    sensor.rightTV.setText(Integer.toString(value) + "/" + Integer.toString(sensor.rightSB.getProgress()));
                    sensor.leftTV.setText("0/"+ Integer.toString(sensor.leftSB.getProgress()));
                }
                if(value <= 0) {
                    sensor.leftTV.setText(Integer.toString(-1*value) + "/" + Integer.toString(sensor.leftSB.getProgress()));
                    sensor.rightTV.setText("0/" + Integer.toString(sensor.rightSB.getProgress()));
                }
            }
        });
    }

    //for y axis
    public void setGaugeValueY(final int value, final SensorUI sensor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(value < 0 & value > -90) {
                    sensor.leftPBY.setProgress(-1*value);
                    sensor.rightPBY.setProgress(0);

                    if(sensor == chestUI){
                        chestData.add( Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                else if(value > 0 & value < 90 ){
                    sensor.leftPBY.setProgress(0);
                    sensor.rightPBY.setProgress(value);
                    if(sensor == chestUI){
                        chestData.add( Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                if (value > sensor.rightSBY.getProgress() & value < 90 ){
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));


                }
                if ( (value*-1) > sensor.leftSB.getProgress() & (value*-1) < 90 ){
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));


                }

                if (value < sensor.rightSBY.getProgress() & (value*-1) < sensor.leftSBY.getProgress()){

                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));
                }
                if(value >= 0 ){
                    sensor.rightTVY.setText(Integer.toString(value) + "/" + Integer.toString(sensor.rightSBY.getProgress()));
                    sensor.leftTVY.setText("0/"+ Integer.toString(sensor.leftSBY.getProgress()));
                }
                if(value <= 0) {
                    sensor.leftTVY.setText(Integer.toString(-1*value) + "/" + Integer.toString(sensor.leftSBY.getProgress()));
                    sensor.rightTVY.setText("0/" + Integer.toString(sensor.rightSBY.getProgress()));
                }
            }
        });
    }
    //end y

    //for z axis
    public void setGaugeValueZ(final int value, final SensorUI sensor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(value < 0 & value > -90) {
                    sensor.leftPBZ.setProgress(-1*value);
                    sensor.rightPBZ.setProgress(0);

                    if(sensor == chestUI){
                        chestData.add( Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                else if(value > 0 & value < 90 ){
                    sensor.leftPBZ.setProgress(0);
                    sensor.rightPBZ.setProgress(value);
                    if(sensor == chestUI){
                        chestData.add( Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                if (value > sensor.rightSBZ.getProgress() & value < 90 ){
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));


                }
                if ( (value*-1) > sensor.leftSBZ.getProgress() & (value*-1) < 90 ){
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));


                }

                if (value < sensor.rightSBZ.getProgress() & (value*-1) < sensor.leftSBZ.getProgress()){

                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));
                }
                if(value >= 0 ){
                    sensor.rightTVZ.setText(Integer.toString(value) + "/" + Integer.toString(sensor.rightSBZ.getProgress()));
                    sensor.leftTVZ.setText("0/"+ Integer.toString(sensor.leftSBZ.getProgress()));
                }
                if(value <= 0) {
                    sensor.leftTVZ.setText(Integer.toString(-1*value) + "/" + Integer.toString(sensor.leftSBZ.getProgress()));
                    sensor.rightTVZ.setText("0/" + Integer.toString(sensor.rightSBZ.getProgress()));
                }
            }
        });
    }
    //end z
    //SENSOR STATUS TEXT
    public void setSensorStatus(final String message)
    {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatus.setText(message);

            }
        });
    }
    //scan for BT devices function
    Runnable scanStop = new Runnable() {
        @Override
        public void run() {
            if(bleService.scanning){
                if(scanCount > 0){
                    scanCount--;
                    timerHandler.postDelayed(scanStop, 1000);
                }
                else if(scanCount == 0){
                    bleService.scanner.stopScan(bleService.mScanCallback);
                    bleService.scanning = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSensorStatus("Scan Timeout");
                            if(bleService.chestGatt == null){
                                chestUI.connect.setBackgroundResource(R.drawable.chestwhite);}
                         /*   if(bleService.kneeGatt ==  null){kneeUI.connect.setBackgroundResource(R.drawable.armwhite);}
                            if(bleService.ankleGatt ==  null){ankleUI.connect.setBackgroundResource(R.drawable.wristwhite);}*/
                            //    if(bleService.handGatt ==  null){handUI.connect.setBackgroundResource(R.drawable.handwhite);}
                        }
                    });
                }
            }
        }
    };
    //do stuff when the BT broadcast tells it to, this is where the find gauge value function and our problem likely is
    private BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            if (eventType.equals("sensorConnected")) {
                if (extras.getString("gatt").equals("hip")) {
                    connectSensor(chestUI);
                    chestUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            chestUI.calibrateSensor(chestUI);
                            return true;
                        }
                    });
                }
            }
            if (eventType.equals("sensorDisconnected")) {
                if (extras.getString("gatt").equals("hip")) {
                    onSensorDisconnected(chestUI);
                    if (bleService.chestGatt != null) {
                        bleService.chestGatt.close();
                        bleService.chestGatt = null;
                    }

                }
            }
        }
    };
    public void connectChest(View v){
        if(bleService.chestGatt == null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setSensorStatus("Searching");
                    chestUI.connect.setBackgroundResource(R.drawable.chestyellow);
                }
            });
            bleService.searchingChest = true;
            bleService.scanner.startScan(bleService.mScanCallback);
            scanCount = scanCount + 20;
            bleService.scanning = true;
            timerHandler.postDelayed(scanStop, 1000);
        }
        else {
            chestClickCount++;
            timerHandler.postDelayed(doubleClick, 500);
            if (chestClickCount == 2) {
                bleService.chestGatt.disconnect();
                bleService.chestGatt.close();
                bleService.chestGatt = null;
                setSensorStatus("Sensor disconnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chestUI.connect.setBackgroundResource(R.drawable.chestwhite);
                        chestUI.leftTV.setVisibility(View.INVISIBLE);
                        chestUI.rightTV.setVisibility(View.INVISIBLE);
                    }
                });
                chestClickCount = 0;
            }
        }
    }
    private void connectSensor(final SensorUI sensor){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSensorStatus("Sensor Connected");
                sensor.connect.setBackgroundResource(sensor.green);
                sensor.rightTV.setVisibility(View.VISIBLE);
                sensor.leftTV.setVisibility(View.VISIBLE);
            }
        });
    }
    Runnable doubleClick = new Runnable(){
        @Override
        public void run(){
            chestClickCount = 0;
        }
    };
    private void onSensorDisconnected(final SensorUI sensor){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensor.leftPB.setProgress(0);
                sensor.rightPB.setProgress(0);
                sensor.rightTV.setVisibility(View.INVISIBLE);
                sensor.leftTV.setVisibility(View.INVISIBLE);
                sensor.connect.setBackgroundResource(sensor.white);
                sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));
            }
        });
        setSensorStatus("Sensor Disconnected");
        Log.v("BLUETOOTH", "DISCONNECTED");
    }
    //get 10 calibration data points then display the data
    public void findGaugeValueX(final SensorUI sensor, float gyroX){
        if(sensor.calibrate & sensor.calibrateCounter < 10){
            sensor.calibrateCounter++;
            sensor.average = sensor.average + gyroX;


        }
        else if (sensor.calibrate & sensor.calibrateCounter == 10){
            sensor.average = sensor.average/10;
            sensor.calibrateCounter++;
        }
        else if (sensor.calibrate & sensor.calibrateCounter > 10){
            if((sensor.average+90.0) <= 180 & (sensor.average - 90) >= -180){
                setGaugeValueX((int)(gyroX + (-1*sensor.average)), sensor);
                int correctedValue = (int)(gyroX + (-1*sensor.average));

            }
            else if((sensor.average+90) > 180){
                if (gyroX < 0 ){
                    setGaugeValueX((int)((180 - sensor.average) + (gyroX + 180)),sensor);
                    int correctedValue = (int)((180 - sensor.average) + (gyroX + 180));
                }
                else if(gyroX > 0){
                    setGaugeValueX((int)(gyroX + (-1*sensor.average)), sensor);
                    int correctedValue = (int)(gyroX + (-1*sensor.average));
                }



            }
            else if((sensor.average-90) < -180){
                if(gyroX < 0 ){
                    setGaugeValueX((int)(gyroX + (-1*sensor.average)), sensor);
                    int correctedValue = (int)(gyroX + (-1*sensor.average));
                }
                if(gyroX > 0){
                    setGaugeValueX((int)((-180 - sensor.average) + (gyroX - 180)), sensor);
                    int correctedValue = (int)((-180 - sensor.average) + (gyroX - 180));
                }


            }
        }
    }

    //find gyroY
    public void findGaugeValueY(final SensorUI sensor, float gyroY){
        if(sensor.calibrate & sensor.calibrateCounter < 10){
            sensor.calibrateCounter++;
            sensor.average = sensor.average + gyroY;


        }
        else if (sensor.calibrate & sensor.calibrateCounter == 10){
            sensor.average = sensor.average/10;
            sensor.calibrateCounter++;
        }
        else if (sensor.calibrate & sensor.calibrateCounter > 10){
            if((sensor.average+90.0) <= 180 & (sensor.average - 90) >= -180){
                setGaugeValueY((int)(gyroY + (-1*sensor.average)), sensor);
                int correctedValue = (int)(gyroY + (-1*sensor.average));

            }
            else if((sensor.average+90) > 180){
                if (gyroY < 0 ){
                    setGaugeValueY((int)((180 - sensor.average) + (gyroY + 180)),sensor);
                    int correctedValue = (int)((180 - sensor.average) + (gyroY + 180));
                }
                else if(gyroY > 0){
                    setGaugeValueY((int)(gyroY + (-1*sensor.average)), sensor);
                    int correctedValue = (int)(gyroY + (-1*sensor.average));
                }



            }
            else if((sensor.average-90) < -180){
                if(gyroY < 0 ){
                    setGaugeValueY((int)(gyroY + (-1*sensor.average)), sensor);
                    int correctedValue = (int)(gyroY + (-1*sensor.average));
                }
                if(gyroY > 0){
                    setGaugeValueY((int)((-180 - sensor.average) + (gyroY - 180)), sensor);
                    int correctedValue = (int)((-180 - sensor.average) + (gyroY - 180));
                }


            }
        }
    }
    //end of gyroY

    //find gyroZ
    public void findGaugeValueZ(final SensorUI sensor, float gyroZ){
        if(sensor.calibrate & sensor.calibrateCounter < 10){
            sensor.calibrateCounter++;
            sensor.average = sensor.average + gyroZ;


        }
        else if (sensor.calibrate & sensor.calibrateCounter == 10){
            sensor.average = sensor.average/10;
            sensor.calibrateCounter++;
        }
        else if (sensor.calibrate & sensor.calibrateCounter > 10){
            if((sensor.average+90.0) <= 180 & (sensor.average - 90) >= -180){
                setGaugeValueZ((int)(gyroZ + (-1*sensor.average)), sensor);
                int correctedValue = (int)(gyroZ + (-1*sensor.average));

            }
            else if((sensor.average+90) > 180){
                if (gyroZ < 0 ){
                    setGaugeValueZ((int)((180 - sensor.average) + (gyroZ + 180)),sensor);
                    int correctedValue = (int)((180 - sensor.average) + (gyroZ + 180));
                }
                else if(gyroZ > 0){
                    setGaugeValueZ((int)(gyroZ + (-1*sensor.average)), sensor);
                    int correctedValue = (int)(gyroZ + (-1*sensor.average));
                }



            }
            else if((sensor.average-90) < -180){
                if(gyroZ < 0 ){
                    setGaugeValueZ((int)(gyroZ + (-1*sensor.average)), sensor);
                    int correctedValue = (int)(gyroZ + (-1*sensor.average));
                }
                if(gyroZ > 0){
                    setGaugeValueZ((int)((-180 - sensor.average) + (gyroZ - 180)), sensor);
                    int correctedValue = (int)((-180 - sensor.average) + (gyroZ - 180));
                }


            }
        }
    }
    //end of gyroZ




}
