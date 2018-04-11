package com.example.nutri_000.testinggauge;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    public BleService bleService;
    boolean isBound = false;
    //boolean fileCreated = false;
    boolean writeDebounce = false;
    String fileName = "trialData_";
    String randomID = "0001_";
    String path = "/storage/emulated/0/testData/";
    String string = "Hello World!";
    String dateTime = DateFormat.getDateTimeInstance().format(new Date());
    String fileType = ".txt";
    //ArrayList<String> chestData = new ArrayList<String>();
    String fullPath = path + fileName + randomID + dateTime + fileType;
    int scanCount = 20;
    int[] clickCount = {0,0,0,0};
    //boolean fireflyFound = false;
    final static String tag="MainActivity";

    //open the ble binder

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(tag,"Opening BLE Binder");
            BleService.BleBinder binder = (BleService.BleBinder) service;
            bleService = binder.getService();
            isBound = true;
            bleService.initializeBle();
            Log.v(tag,"BLE binder initialized");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(tag,"Disconnecting BLE binder");
            bleService = null;
            isBound = false;
        }
    };
//some "global' variables and stuff
    Handler timerHandler = new Handler();
    Status statusVariables = new Status();
    SensorUI chestUI;
    SensorUI bicepUI;
    SensorUI wristUI;
    SensorUI handUI;
    private static Context context;
    private TextView sensorStatus;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    //auto android stuff, link GUI elements to code functions, start stuff, request permissions, etc
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(tag,"Opening app");
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

        } else {
            Log.v(tag,"Start from scratch, assign all GUI elements");
            // all UI components for main activity
            setContentView(R.layout.activity_main);
            int[] rightPBChest={ R.id.progressBarChestXRight, R.id.progressBarChestRightY, R.id.progressBarChestRightZ};
            int[] leftPBChest={R.id.progressBarChestXLeft, R.id.progressBarChestLeftY, R.id.progressBarChestLeftZ};
            int[] rightSBChest={R.id.seekBarChestXRight, R.id.seekBarChestYRight, R.id.seekBarChestZRight};
            int[] leftSBChest={R.id.seekBarChestXLeft, R.id.seekBarChestYLeft, R.id.seekBarChestZLeft};
            int[] rightTVChest={R.id.chestAngleXRight,R.id.chestAngleYRight,R.id.chestAngleZRight};
            int[] leftTVChest={ R.id.chestAngleXLeft,  R.id.chestAngleYLeft,  R.id.chestAngleZLeft};
            chestUI = new SensorUI(R.id.chestButton,rightPBChest, leftPBChest, rightSBChest , leftSBChest ,rightTVChest, leftTVChest,R.id.relativeHip,  this);
            chestUI.green = R.drawable.chestgreen;
            chestUI.yellow = R.drawable.chestyellow;
            chestUI.white = R.drawable.chestwhite;

            int[] rightPBBicep={R.id.progressBarBicepXRight, R.id.progressBarBicepRightY, R.id.progressBarBicepRightZ};
            int[] leftPBBicep={R.id.progressBarBicepXLeft, R.id.progressBarBicepLeftY, R.id.progressBarBicepLeftZ};
            int[] rightSBBicep={R.id.seekBarBicepXRight, R.id.seekBarBicepYRight, R.id.seekBarBicepZRight};
            int[] leftSBBicep={R.id.seekBarBicepXLeft, R.id.seekBarBicepYLeft, R.id.seekBarBicepZLeft};
            int[] rightTVBicep={R.id.bicepAngleXRight, R.id.bicepAngleYRight, R.id.bicepAngleZRight};
            int[] leftTVBicep={R.id.bicepAngleXLeft, R.id.bicepAngleYLeft, R.id.bicepAngleZLeft};
            bicepUI=new SensorUI(R.id.bicepButton, rightPBBicep, leftPBBicep, rightSBBicep, leftSBBicep, rightTVBicep, leftTVBicep, R.id.relativeHip, this);
            bicepUI.green = R.drawable.bicepgreen;
            bicepUI.yellow = R.drawable.bicepyellow;
            bicepUI.white = R.drawable.bicepwhite;

            int[] rightPBWrist={R.id.progressBarWristXRight, R.id.progressBarWristRightY, R.id.progressBarWristRightZ};
            int[] leftPBWrist={R.id.progressBarWristXLeft, R.id.progressBarWristLeftY, R.id.progressBarWristLeftZ};
            int[] rightSBWrist={R.id.seekBarWristXRight, R.id.seekBarWristYRight, R.id.seekBarWristZRight};
            int[] leftSBWrist={R.id.seekBarWristXLeft, R.id.seekBarWristYLeft, R.id.seekBarWristZLeft};
            int[] rightTVWrist={R.id.wristAngleXRight, R.id.wristAngleYRight, R.id.wristAngleZRight};
            int[] leftTVWrist={R.id.wristAngleXLeft, R.id.wristAngleYLeft, R.id.wristAngleZLeft};
            wristUI=new SensorUI(R.id.wristButton, rightPBWrist, leftPBWrist, rightSBWrist, leftSBWrist, rightTVWrist, leftTVWrist, R.id.relativeHip, this);
            wristUI.green = R.drawable.wristgreen;
            wristUI.yellow = R.drawable.wristyellow;
            wristUI.white = R.drawable.wristwhite;

            int[] rightPBHand={R.id.progressBarHandXRight, R.id.progressBarHandRightY, R.id.progressBarHandRightZ};
            int[] leftPBHand={R.id.progressBarHandXLeft, R.id.progressBarHandLeftY, R.id.progressBarHandLeftZ};
            int[] rightSBHand={R.id.seekBarHandXRight, R.id.seekBarHandYRight, R.id.seekBarHandZRight};
            int[] leftSBHand={R.id.seekBarHandXLeft, R.id.seekBarHandYLeft, R.id.seekBarHandZLeft};
            int[] rightTVHand={R.id.handAngleXRight, R.id.handAngleYRight, R.id.handAngleZRight};
            int[] leftTVHand={R.id.handAngleXLeft, R.id.handAngleYLeft, R.id.handAngleZLeft};
            handUI=new SensorUI(R.id.handButton, rightPBHand, leftPBHand, rightSBHand, leftSBHand, rightTVHand, leftTVHand, R.id.relativeHip, this);
            handUI.green = R.drawable.handgreen;
            handUI.yellow = R.drawable.handyellow;
            handUI.white = R.drawable.handwhite;

            sensorStatus = (TextView) findViewById(R.id.SensorStatus);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(tag, "File writing permission granted");
                //writeFile();
            }
            Log.v(tag,"Make the BLE thing available to do stuff");
            registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
        }

    }

    //close BT connections on destroy
    @Override
    protected void onDestroy() {
        Log.v(tag,"Close app, destroy stuff, close BT connections");
        super.onDestroy();
        for(int i=0;i<bleService.gattArray.length;i++) {
            if (bleService.gattArray[i] != null) {
                bleService.gattArray[i].disconnect();
                bleService.gattArray[i].close();
                bleService.gattArray[i] = null;
            }
        }
        Log.v(tag, "DESTROYED");
    }

    //log that it stopped
    @Override
    protected void onStop() {
        super.onStop();
        Log.v(tag, "on stop, STOPPED");
    }

    //deleted extraneous methods
    //something about restoring
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        int restore = 42;
        savedInstanceState.putInt("SOMETHING", restore);
    }

    //handling permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(tag, "coarse location permission granted");
                    Log.v(tag,"Start the BT scanner fr this time");
                    Intent bleIntent = new Intent(this, BleService.class);
                    startService(bleIntent);
                    bindService(bleIntent, mServiceConnection, this.BIND_AUTO_CREATE);
                } else {
                    Log.v(tag,"didn't give us BT permissions, how clever");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //add code to handle dismiss
                        }

                    });
                    builder.show();

                }
                return;
            }
        }
    }

    //is this necessary?
    public static Context getAppContext() {
        Log.v(tag,"You wanted context you got context");
        return MainActivity.context;
    }
    //GAUGE

    //set progress bar axis
    public void setGaugeValue(final int value, final SensorUI sensor, final int axis) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Log.v(tag,"Set Gauge value, value is "+value+" and the axis is "+axis);
                if (value < 0 & value > -90) {
                    sensor.progressBars[1][axis].setProgress(-1*value);
                    sensor.progressBars[0][axis].setProgress(0);

                    /*if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                    }*/

                } else if (value > 0 & value < 90) {
                    sensor.progressBars[1][axis].setProgress(0);
                    sensor.progressBars[0][axis].setProgress(value);
                    /*if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                    }*/
                }
                /*//cool glowy you did it color
                if (value > sensor.seekBars[0][axis].getProgress() & value < 90) {
                    Log.v(tag,"Activate stim, value higher than goals set positive");
                    sensor.setSensorBackgroundColor("#008542");

                }

                //cool glowy you did it color
                if ((value * -1) > sensor.seekBars[1][axis].getProgress() & (value * -1) < 90) {
                    Log.v(tag,"Activate stim, value higher than goals set negative");
                    sensor.setSensorBackgroundColor("#008542");

                }
//normal color
                if (value < sensor.seekBars[0][axis].getProgress() & (value * -1) < sensor.seekBars[1][axis].getProgress()) {
                    sensor.setSensorBackgroundColor("#404040");

                }*/
                //now display values
                //ignore seekbar values now
                if (value >= 0) {
                    sensor.textViews[0][axis].setText(Integer.toString(value));// + "/" + Integer.toString(sensor.seekBars[0][axis].getProgress()));
                    sensor.textViews[1][axis].setText("0/");// + Integer.toString(sensor.seekBars[1][axis].getProgress()));
                }
                if (value <= 0) {
                    sensor.textViews[1][axis].setText(Integer.toString(-1 * value));// + "/" + Integer.toString(sensor.seekBars[1][axis].getProgress()));
                    sensor.textViews[0][axis].setText("0/" );//+ Integer.toString(sensor.seekBars[0][axis].getProgress()));
                }
            }
        });
    }


    //SENSOR STATUS TEXT
    public void setSensorStatus(final String message) {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatus.setText(message);

            }
        });
    }

    //scan for BT devices function, store connections in bleservice object
    Runnable scanStop = new Runnable() {
        @Override
        public void run() {
            if (bleService.scanning) {
                Log.v(tag,"Scanning for BT atm");
                if (scanCount > 0) {
                    scanCount--;
                    timerHandler.postDelayed(scanStop, 1000);
                } else if (scanCount == 0) {
                    bleService.scanner.stopScan(bleService.mScanCallback);
                    bleService.scanning = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSensorStatus("Scan Timeout");
                            if (bleService.gattArray[0] == null) {
                                chestUI.connect.setBackgroundResource(R.drawable.chestwhite);
                            }
                            if (bleService.gattArray[1] == null) {
                                bicepUI.connect.setBackgroundResource(R.drawable.chestwhite);
                            }
                            if (bleService.gattArray[2] == null) {
                                wristUI.connect.setBackgroundResource(R.drawable.chestwhite);
                            }
                            if (bleService.gattArray[3] == null) {
                                handUI.connect.setBackgroundResource(R.drawable.handwhite);
                            }
                        }
                    });
                }
            }
        }
    };

    Runnable debounceWrite = new Runnable() {
        @Override
        public void run() {
            writeDebounce = false;
        }
    };
    //todo update for all sensors.  not a big problem for now.
    Runnable doubleClick = new Runnable() {
        @Override
        public void run() {
            clickCount[0] = 0;
        }
    };
    public void connectChest(View v){
        if(bleService.gattArray[0]==null) {
            searchForSensor(chestUI, 0);
        }else{
            disconnectSensor(chestUI,0);
        }
    }
    public void connectBicep(View v){
        if(bleService.gattArray[1]==null) {
            searchForSensor(chestUI, 1);
        }else{
            disconnectSensor(chestUI,1);
        }
    }
    public void connectWrist(View v){
        if(bleService.gattArray[2]==null) {
            searchForSensor(wristUI, 2);
        }else{
            disconnectSensor(wristUI,2);
        }
    }
    public void connectHand(View v){
        if(bleService.gattArray[3]==null) {
            searchForSensor(handUI, 3);
        }else{
            disconnectSensor(handUI,3);
        }
    }
    //Sends to bicep flex screen
    public void startBicepFlex(View v){
        Intent intent=new Intent(this,BicepFlexMeasurement.class);
        startActivity(intent);

    }
    public void startShoulderAbduction(View v){
        Intent intent=new Intent(this,ShoulderAbduction.class);
        startActivity(intent);

    }
    public void startShoulderFlexion(View v){
        Intent intent=new Intent(this,ShoulderFlexion.class);
        startActivity(intent);

    }
    public void startShoulderRotation(View v){
        Intent intent=new Intent(this,ShoulderRotation.class);
        startActivity(intent);

    }
    public void startWristSupination(View v){
        Intent intent=new Intent(this,SupinationMeasurement.class);
        startActivity(intent);

    }
    public void startWristDeflection(View v){
        Intent intent=new Intent(this,WristDeflection.class);
        startActivity(intent);

    }
    public void startWristFlexion(View v){
        Intent intent=new Intent(this,WristFlexion.class);
        startActivity(intent);

    }

    public void searchForSensor(SensorUI sensor,int position){
        Log.v(tag,"Looking For Sensor");
        setSensorStatus("Searching");
        bleService.searchingChest = false;
        bleService.searchingBicep = false;
        bleService.searchingWrist = false;
        bleService.searchingPCM = false;
        if(position==0){
            chestUI.connect.setBackgroundResource(R.drawable.chestyellow);
            bleService.searchingChest = true;
        }else if(position==1){
            bicepUI.connect.setBackgroundResource(R.drawable.bicepyellow);
            bleService.searchingBicep = true;
        }else if(position==2){
            wristUI.connect.setBackgroundResource(R.drawable.wristyellow);
            bleService.searchingWrist = true;
        }else if(position==3){
            handUI.connect.setBackgroundResource(R.drawable.handyellow);
            bleService.searchingHand = true;
        }
        bleService.scanner.startScan(bleService.mScanCallback);
        scanCount = scanCount + 20;
        bleService.scanning = true;
        timerHandler.postDelayed(scanStop, 1000);
    }

    private void disconnectSensor(SensorUI sensor, int position){
        clickCount[position]++;
        timerHandler.postDelayed(doubleClick, 500);
        if (clickCount[position] >= 2) {
            bleService.gattArray[position].disconnect();
            bleService.gattArray[position].close();
            bleService.gattArray[position] = null;
            setSensorStatus("Sensor disconnected");
            sensor.connect.setBackgroundResource(R.drawable.chestwhite);
            clickCount[position] = 0;
        }
    }
//do stuff when the BT broadcast tells it to
    //z axis goes from 0 to 360, x and y from -180 to 180 I think
    //make a clone of this in the flex measurement
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.v(tag,"Got bundle of things");
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            //Log.v(tag,"Event type is "+eventType);
            if (eventType.equals("sensorConnected")) {
                //Log.v(tag,"Sensor is connected event");
                if (extras.getString("gatt").equals("chest")) {
                    //Log.v(tag,"Chest connected");
                    connectSensor(chestUI);
                    chestUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            chestUI.initializeSensor();
                            return true;
                        }
                    });
                }else if(extras.getString("gatt").equals("bicep")){
                    //Log.v(tag, "Bicep connected");
                    connectSensor(bicepUI);
                    bicepUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            bicepUI.initializeSensor();
                            return true;
                        }
                    });
                }else if(extras.getString("gatt").equals("wrist")){
                    //Log.v(tag, "Wrist connected");
                    connectSensor(wristUI);
                    wristUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            wristUI.initializeSensor();
                            return true;
                        }
                    });
                }else if(extras.getString("gatt").equals("hand")){
                    //Log.v(tag, "Hand connected");
                    connectSensor(handUI);
                    handUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            handUI.initializeSensor();
                            return true;
                        }
                    });
                }

                if (extras.getString("gatt").equals("unknown")) {
                    //Log.v(tag, "unknown BT connected");
                }
                //Log.v("bleService", "connected message sent");
            }
            if (eventType.equals("sensorDisconnected")) {
               // Log.v(tag, "Disconnected sensor event");
                if (extras.getString("gatt").equals("chest")) {
                    Log.v(tag, "Chest disconnected");
                    onSensorDisconnected(chestUI);
                    if (bleService.gattArray[0] != null) {
                        bleService.gattArray[0].close();
                        bleService.gattArray[0] = null;
                    }

                }else if(extras.getString("gatt").equals("bicep")) {
                   // Log.v(tag, "Bicep disconnected");
                    onSensorDisconnected(bicepUI);
                    if (bleService.gattArray[1] != null) {
                        bleService.gattArray[1].close();
                        bleService.gattArray[1] = null;
                    }

                }else if(extras.getString("gatt").equals("wrist")) {
                    //Log.v(tag, "Wrist disconnected");
                    onSensorDisconnected(wristUI);
                    if (bleService.gattArray[2] != null) {
                        bleService.gattArray[2].close();
                        bleService.gattArray[2] = null;
                    }

                }else if(extras.getString("gatt").equals("hand")) {
                    //Log.v(tag, "Hand disconnected");
                    onSensorDisconnected(handUI);
                    if (bleService.gattArray[3] != null) {
                        bleService.gattArray[3].close();
                        bleService.gattArray[3] = null;
                    }

                }
            }

                if (eventType.equals("notification")) {
                    //Log.v(tag,"You have mail event");
                    BleNotification notification = intent.getParcelableExtra("notifyObject");
                    //notification object is null for wrist, but works normally for chest...
                    // Log.v(tag, "notification gatt is "+notification.gatt);
                    if (notification.gatt.equals("chest")) {
                       // Log.v(tag," from the chest");
                        //find value x, switched to different value coding
                       // Log.v(tag, "Value x from object is "+notification.valueX);
                        setGaugeValue((int)notification.valueX,chestUI,0);
                        //find value y, switched to different value coding
                      //  Log.v(tag, "Value y from object is "+notification.valueY);
                        setGaugeValue((int)notification.valueY,chestUI,1);
                        //find value z, switched to different value coding
                      //  Log.v(tag, "Value z from object is "+notification.valueZ);
                        setGaugeValue((int)notification.valueZ,chestUI,2);

                    }else if(notification.gatt.equals("bicep")) {
                      //  Log.v(tag, " from the bicep");
                        //find value x, switched to different value coding
                      //  Log.v(tag, "Value x from object is " + notification.valueX);
                        setGaugeValue((int) notification.valueX, bicepUI, 0);
                        //find value y, switched to different value coding
                       // Log.v(tag, "Value y from object is " + notification.valueY);
                        setGaugeValue((int) notification.valueY, bicepUI, 1);
                        //find value z, switched to different value coding
                      //  Log.v(tag, "Value z from object is " + notification.valueZ);
                        setGaugeValue((int)notification.valueZ,bicepUI,2);
                    }else if(notification.gatt.equals("wrist")) {
                       // Log.v(tag, " from the wrist");
                        //find value x, switched to different value coding
                      //  Log.v(tag, "Value x from object is " + notification.valueX);
                        setGaugeValue((int) notification.valueX, wristUI, 0);
                        //find value y, switched to different value coding
                      //  Log.v(tag, "Value y from object is " + notification.valueY);
                        setGaugeValue((int) notification.valueY, wristUI, 1);
                        //find value z, switched to different value coding
                       // Log.v(tag, "Value z from object is " + notification.valueZ);
                        setGaugeValue((int)notification.valueZ,wristUI,2);
                    }
                    else if(notification.gatt.equals("hand")){
                       // Log.v(tag," from the hand");
                        //find value x, switched to different value coding
                       // Log.v(tag, "Value x from object is "+notification.valueX);
                        setGaugeValue((int)notification.valueX,handUI,0);
                        //find value y, switched to different value coding
                       // Log.v(tag, "Value y from object is "+notification.valueY);
                        setGaugeValue((int)notification.valueY,handUI,1);
                        //find value z, switched to different value coding
                       // Log.v(tag, "Value z from object is "+notification.valueZ);
                        setGaugeValue((int)notification.valueZ,handUI,2);
                    }

                }
            }
        };

        private void connectSensor(final SensorUI sensor) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setSensorStatus("Sensor Connected");
                    sensor.connect.setBackgroundResource(sensor.green);
                }
            });
        }

        private void onSensorDisconnected(final SensorUI sensor) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0;i<sensor.progressBars.length;i++){
                        for(int ii=0;ii<sensor.progressBars[0].length;ii++){
                            sensor.progressBars[i][ii].setProgress(0);
                        }
                    }
                    sensor.connect.setBackgroundResource(sensor.white);
                    sensor.setSensorBackgroundColor("#404040");
                }
            });
            setSensorStatus("Sensor Disconnected");
            //Log.v("BLUETOOTH", "DISCONNECTED");
        }
    }
