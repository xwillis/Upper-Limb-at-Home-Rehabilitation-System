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
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    BleService bleService;
    boolean isBound = false;
    boolean fileCreated = false;
    boolean writeDebounce = false;
    String fileName = "trialData_";
    String randomID = "0001_";
    String path = "/storage/emulated/0/testData/";
    String string = "Hello World!";
    String dateTime = DateFormat.getDateTimeInstance().format(new Date());
    String fileType = ".txt";
    ArrayList<String> chestData = new ArrayList<String>();
    String fullPath = path + fileName + randomID + dateTime + fileType;
    int scanCount = 20;
    int chestClickCount = 0;
    boolean fireflyFound = false;
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

            chestUI = new SensorUI(R.id.chestButton, R.id.progressBarTopRight, R.id.progressBarTopRightY, R.id.progressBarTopRightZ, R.id.progressBarTopLeft, R.id.progressBarTopLeftY,
                    R.id.progressBarTopLeftZ, R.id.seekBarChestXRight, R.id.seekBarChestYRight, R.id.seekBarChestZRight, R.id.seekBarChestXLeft, R.id.seekBarChestYLeft, R.id.seekBarChestZLeft,
                    R.id.chestAngleXRight, R.id.chestAngleXLeft, R.id.chestAngleYRight, R.id.chestAngleYLeft, R.id.chestAngleZRight, R.id.chestAngleZLeft, R.id.relativeHip, this);
            chestUI.leftPB.setRotation(180);

            chestUI.green = R.drawable.chestgreen;
            chestUI.yellow = R.drawable.chestyellow;
            chestUI.white = R.drawable.chestwhite;
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
        if (bleService.chestGatt != null) {
            bleService.chestGatt.disconnect();
            bleService.chestGatt.close();
            bleService.chestGatt = null;
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

    //set progress bar for X axis
    public void setGaugeValueX(final int value, final SensorUI sensor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v(tag,"Set Gauge value x, value is "+value);
                if (value < 0 & value > -90) {
                    sensor.leftPB.setProgress(-1 * value);
                    sensor.rightPB.setProgress(0);

                    if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                    }

                } else if (value > 0 & value < 90) {
                    sensor.leftPB.setProgress(0);
                    sensor.rightPB.setProgress(value);
                    if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                    }
                }
                //cool glowy you did it color
                if (value > sensor.rightSB.getProgress() & value < 90) {
                    Log.v(tag,"Activate stim, value higher than goals set positive");
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));
                    if (!writeDebounce) {
                        //writeFile(value, sensor);
                        writeDebounce = true;
                        timerHandler.postDelayed(debounceWrite, 1000);
                    }

                }
                //cool glowy you did it color
                if ((value * -1) > sensor.leftSB.getProgress() & (value * -1) < 90) {
                    Log.v(tag,"Activate stim, value higher than goals set negative");
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));
                    if (!writeDebounce) {
                        writeDebounce = true;
                        timerHandler.postDelayed(debounceWrite, 1000);
                    }

                }
//normal color
                if (value < sensor.rightSB.getProgress() & (value * -1) < sensor.leftSB.getProgress()) {
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));

                }
                //now display values
                if (value >= 0) {
                    sensor.rightTV.setText(Integer.toString(value) + "/" + Integer.toString(sensor.rightSB.getProgress()));
                    sensor.leftTV.setText("0/" + Integer.toString(sensor.leftSB.getProgress()));
                }
                if (value <= 0) {
                    sensor.leftTV.setText(Integer.toString(-1 * value) + "/" + Integer.toString(sensor.leftSB.getProgress()));
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
                Log.v(tag,"Set Gauge value y, value is "+value);
                if (value < 0 & value > -90) {
                    sensor.leftPBY.setProgress(-1 * value);
                    sensor.rightPBY.setProgress(0);

                    if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }

                } else if (value > 0 & value < 90) {
                    sensor.leftPBY.setProgress(0);
                    sensor.rightPBY.setProgress(value);
                    if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                if (value > sensor.rightSBY.getProgress() & value < 90) {
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));
                    if (!writeDebounce) {
                        //writeFile(value, sensor);
                        writeDebounce = true;
                        timerHandler.postDelayed(debounceWrite, 1000);
                    }

                }
                if ((value * -1) > sensor.leftSB.getProgress() & (value * -1) < 90) {
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));
                    if (!writeDebounce) {
                        //writeFile(value, sensor);
                        writeDebounce = true;
                        timerHandler.postDelayed(debounceWrite, 1000);
                    }

                }

                if (value < sensor.rightSBY.getProgress() & (value * -1) < sensor.leftSBY.getProgress()) {

                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));

                }
                if (value >= 0) {
                    sensor.rightTVY.setText(Integer.toString(value) + "/" + Integer.toString(sensor.rightSBY.getProgress()));
                    sensor.leftTVY.setText("0/" + Integer.toString(sensor.leftSBY.getProgress()));
                }
                if (value <= 0) {
                    sensor.leftTVY.setText(Integer.toString(-1 * value) + "/" + Integer.toString(sensor.leftSBY.getProgress()));
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
                Log.v(tag,"Set Gauge value z, value is "+value);
                if (value < 0 & value > -90) {
                    sensor.leftPBZ.setProgress(-1 * value);
                    sensor.rightPBZ.setProgress(0);

                    if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                } else if (value > 0 & value < 90) {
                    sensor.leftPBZ.setProgress(0);
                    sensor.rightPBZ.setProgress(value);
                    if (sensor == chestUI) {
                        chestData.add(Integer.toString(value) + " ");
                        //chestCount++;
                        chestData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //chestCount++;
                    }
                }
                if (value > sensor.rightSBZ.getProgress() & value < 90) {
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));
                    if (!writeDebounce) {
                        //writeFile(value, sensor);
                        writeDebounce = true;
                        timerHandler.postDelayed(debounceWrite, 1000);
                    }

                }
                if ((value * -1) > sensor.leftSBZ.getProgress() & (value * -1) < 90) {
                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#008542"));
                    if (!writeDebounce) {
                        //writeFile(value, sensor);
                        writeDebounce = true;
                        timerHandler.postDelayed(debounceWrite, 1000);
                    }

                }

                if (value < sensor.rightSBZ.getProgress() & (value * -1) < sensor.leftSBZ.getProgress()) {

                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));

                }
                if (value >= 0) {
                    sensor.rightTVZ.setText(Integer.toString(value) + "/" + Integer.toString(sensor.rightSBZ.getProgress()));
                    sensor.leftTVZ.setText("0/" + Integer.toString(sensor.leftSBZ.getProgress()));
                }
                if (value <= 0) {
                    sensor.leftTVZ.setText(Integer.toString(-1 * value) + "/" + Integer.toString(sensor.leftSBZ.getProgress()));
                    sensor.rightTVZ.setText("0/" + Integer.toString(sensor.rightSBZ.getProgress()));
                }
            }
        });
    }
    //end z


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


    //scan for BT devices function
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
                            if (bleService.chestGatt == null) {
                                chestUI.connect.setBackgroundResource(R.drawable.chestwhite);
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
    Runnable doubleClick = new Runnable() {
        @Override
        public void run() {
            chestClickCount = 0;
        }
    };

    public void connectThigh(View v){
        Log.v(tag,"Connecting Chest IMU, or disconnecting");
        if(bleService.chestGatt == null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setSensorStatus("Searching");
                    chestUI.connect.setBackgroundResource(R.drawable.chestyellow);
                }
            });
            bleService.searchingHip = true;
            bleService.searchingKnee = false;
            bleService.searchingAnkle = false;
            bleService.searchingPCM = false;
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
//do stuff when the BT broadcast tells it to, this is where the find gauge value function and our problem likely is
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(tag,"Got bundle of things");
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            Log.v(tag,"Event type is "+eventType);
            if (eventType.equals("sensorConnected")) {
                Log.v(tag,"Sensor is connected event");
                if (extras.getString("gatt").equals("hip")) {
                    Log.v(tag,"Hip/chest connected");
                    connectSensor(chestUI);
                    chestUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            chestUI.calibrateSensor(chestUI);
                            return true;
                        }
                    });
                }

                if (extras.getString("gatt").equals("unknown")) {
                    Log.v(tag, "unknown BT connected");
                }
                Log.v("bleService", "connected message sent");
            }
            if (eventType.equals("sensorDisconnected")) {
                Log.v(tag, "Disconnected sensor event");
                if (extras.getString("gatt").equals("hip")) {
                    Log.v(tag, "Hip/chest disconnected");
                    onSensorDisconnected(chestUI);
                    if (bleService.chestGatt != null) {
                        bleService.chestGatt.close();
                        bleService.chestGatt = null;
                    }

                }
            }

                if (eventType.equals("notification")) {
                    Log.v(tag,"You have mail event");
                    BleNotification notification = intent.getParcelableExtra("notifyObject");
                    if (notification.gatt.equals("hip")) {
                        Log.v(tag,"You have mail from the hip/chest");
                        //find value x, switched to different value coding
                        Log.v(tag, "Value x from object is "+notification.valueX);
                        findGaugeValue(chestUI, notification.valueX,1);
                        //find value y, switched to different value coding
                        Log.v(tag, "Value y from object is "+notification.valueY);
                        findGaugeValue(chestUI, notification.valueY,2);
                        //find value z, switched to different value coding
                        Log.v(tag, "Value z from object is "+notification.valueZ);
                        findGaugeValue(chestUI, notification.valueZ,3);

                    }

                    if (extras.getString("gatt").equals("hip")) {
                        Log.v(tag,"Reading from the strings sent in extras");
                        float valueX = extras.getFloat("valueX");
                        Log.v(tag, "Value x from string is "+valueX);
                        float valueY = extras.getFloat("valueY");
                        Log.v(tag, "Value x from string is "+valueY);
                        float valueZ = extras.getFloat("valueZ");
                        Log.v(tag, "Value x from string is "+valueZ);
                        findGaugeValue(chestUI, valueX,1);
                        findGaugeValue(chestUI, valueY,2);
                        findGaugeValue(chestUI, valueZ,3);
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
                    sensor.rightTV.setVisibility(View.VISIBLE);
                    sensor.leftTV.setVisibility(View.VISIBLE);
                }
            });
        }

        private void onSensorDisconnected(final SensorUI sensor) {
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


   //find ALL gyro values and call the set them method
//get 10 calibration data points then display the data
      //why calibrate though?  isn't the chip supposed to not need it? other team said it drifted, maybe this is why
      //axis =1 is x, 2 is y, 3 is z
        public void findGaugeValue(final SensorUI sensor, float gyroX, int axis) {
            if (sensor.calibrate & sensor.calibrateCounter < 10) {
                sensor.calibrateCounter++;
                sensor.average = sensor.average + gyroX;


            } else if (sensor.calibrate & sensor.calibrateCounter == 10) {
                sensor.average = sensor.average / 10;
                sensor.calibrateCounter++;
            } else if (sensor.calibrate & sensor.calibrateCounter > 10) {
                int correctedValue=0;
                if ((sensor.average + 90.0) <= 180 & (sensor.average - 90) >= -180) {
                    correctedValue = (int) (gyroX + (-1 * sensor.average));
                    //setGaugeValueX(correctedValue, sensor);

                } else if ((sensor.average + 90) > 180) {
                    if (gyroX < 0) {
                        correctedValue = (int) ((180 - sensor.average) + (gyroX + 180));
                        //setGaugeValueX(correctedValue, sensor);
                    } else if (gyroX > 0) {
                        correctedValue = (int) (gyroX + (-1 * sensor.average));
                        //setGaugeValueX(correctedValue, sensor);

                    }


                } else if ((sensor.average - 90) < -180) {//should really be an else, not an elseif
                    if (gyroX < 0) {
                        correctedValue = (int) (gyroX + (-1 * sensor.average));
                        //setGaugeValueX(correctedValue, sensor);

                    }
                    else if (gyroX > 0) {
                        correctedValue = (int) ((-180 - sensor.average) + (gyroX - 180));
                        //setGaugeValueX(correctedValue, sensor);

                    }


                }
                if(axis==1) {
                    setGaugeValueX(correctedValue, sensor);
                } else if(axis==2){
                    setGaugeValueY(correctedValue, sensor);
                } else {
                    setGaugeValueZ(correctedValue, sensor);
                }
            }
        }


        //log data values and etc in file, but in trycatch just in case
        public void writeFile() {
            try {
                if (!fileCreated) {
                    FileOutputStream outputStream = new FileOutputStream(fullPath);
                    string = "file created on: " + dateTime + "\n";
                    outputStream.write(string.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    fileCreated = true;
                } else {
                    FileOutputStream outputStream = new FileOutputStream(fullPath, true);
                    outputStream.write(dateTime.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    MediaScannerConnection.scanFile(getAppContext(), new String[]{fullPath}, null, null);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //flush stuff at close
        public void writeFileAtStop(String string, SensorUI sensor) {
            try {
                FileOutputStream outputStream = new FileOutputStream(fullPath, true);
                String data = string;
                if (sensor == chestUI) {
                    data = data.concat(chestData.toString() + ";\n");
                }
              /*  if(sensor == kneeUI){
                        data = data.concat(kneeData.toString() + ";\n");
                }
                if(sensor == ankleUI){
                        data = data.concat(ankleData.toString() + ";\n");
                }*/
                outputStream.write(data.getBytes());
                outputStream.flush();
                outputStream.close();
                MediaScannerConnection.scanFile(getAppContext(), new String[]{fullPath}, null, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

// Arm CAL BEGIN //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    }
