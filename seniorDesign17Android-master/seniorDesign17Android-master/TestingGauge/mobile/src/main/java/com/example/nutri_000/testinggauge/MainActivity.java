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
    ArrayList<String> hipData = new ArrayList<String>();
    ArrayList<String> kneeData = new ArrayList<String>();
    ArrayList<String> ankleData = new ArrayList<String>();
    ArrayList<String> handData = new ArrayList<String>();

    int hipCount = 0;
    int kneeCount = 0;
    int ankleCount = 0;

    String fullPath = path + fileName + randomID + dateTime + fileType;
    int scanCount = 20;
    int footClickCount = 0;
    int kneeClickCount = 0;
    int hipClickCount = 0;
    int handClickCount = 0;

    boolean fireflyFound = false;


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


    private static final String TAG = "Cole";

    Handler timerHandler = new Handler();
    Status statusVariables = new Status();
    FireflyCommands fireflyCommands = new FireflyCommands();
    SensorUI hipUI;
    SensorUI kneeUI;
    SensorUI ankleUI;
    SensorUI handUI;

    private static Context context;


    //BLE connections for the firefly

    private FloatingActionButton stimButton;

    //ble connections for the sensor

    private TextView sensorStatus;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

//auto android stuff, link GUI elements to code functions, start stuff, request permissions, etc

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
        } else {

            // all UI components for main activity

            setContentView(R.layout.activity_main);


            hipUI = new SensorUI(R.id.upperLegButton, R.id.progressBarTopRight, R.id.progressBarTopRightY, R.id.progressBarTopRightZ, R.id.progressBarTopLeft, R.id.progressBarTopLeftY,
                    R.id.progressBarTopLeftZ, R.id.seekBarTopRight, R.id.seekBarTopRightY, R.id.seekBarTopRightZ, R.id.seekBarTopLeft, R.id.seekBarTopLeftY, R.id.seekBarTopLeftZ,
                    R.id.topAngle, R.id.topAngleL, R.id.topAngleY, R.id.topAngleLY, R.id.topAngleZ, R.id.topAngleLZ, R.id.relativeHip, this);

            hipUI.leftPB.setRotation(180);


            hipUI.green = R.drawable.chestgreen;
            hipUI.yellow = R.drawable.chestyellow;
            hipUI.white = R.drawable.chestwhite;


            kneeUI = new SensorUI(R.id.lowerLegButton, R.id.progressBarMidRight, R.id.progressBarMidRightY, R.id.progressBarMidRightZ, R.id.progressBarMidLeft, R.id.progressBarMidLeftY,
                    R.id.progressBarMidLeftZ, R.id.seekBarMidRight, R.id.seekBarMidRightY, R.id.seekBarMidRightZ, R.id.seekBarMidLeft, R.id.seekBarMidLeftY, R.id.seekBarMidLeftZ,
                    R.id.midAngle, R.id.midAngleL, R.id.midAngleY, R.id.midAngleLY, R.id.midAngleZ, R.id.midAngleLZ, R.id.relativeKnee, this);

            kneeUI.leftPB.setRotation(180);


            kneeUI.green = R.drawable.armgreen;
            kneeUI.yellow = R.drawable.armyellow;
            kneeUI.white = R.drawable.armwhite;



            ankleUI = new SensorUI(R.id.footButton,R.id.progressBarBottomRight,R.id.progressBarBottomRightY,R.id.progressBarBottomRightZ,R.id.progressBarBottomLeft,R.id.progressBarBottomLeftY,
                    R.id.progressBarBottomLeftZ,R.id.seekBarBottomRight,R.id.seekBarBottomRightY,R.id.seekBarBottomRightZ,R.id.seekBarBottomLeft,R.id.seekBarBottomLeftY,R.id.seekBarBottomLeftZ,
                    R.id.bottomAngle,R.id.bottomAngleL, R.id.bottomAngleY,R.id.bottomAngleLY, R.id.bottomAngleZ,R.id.bottomAngleLZ,R.id.relativeAnkle, this);

            ankleUI.leftPB.setRotation(180);


            ankleUI.green = R.drawable.wristgreen;
            ankleUI.yellow = R.drawable.wristyellow;
            ankleUI.white = R.drawable.wristwhite;


/*
            handUI = new SensorUI(R.id.handButton,R.id.progressBarBottom2Right,R.id.progressBarBottom2RightY,R.id.progressBarBottom2RightZ,R.id.progressBarBottom2Left,R.id.progressBarBottom2LeftY,
                    R.id.progressBarBottom2LeftZ,R.id.seekBarBottom2Right,R.id.seekBarBottom2RightY,R.id.seekBarBottom2RightZ,R.id.seekBarBottom2Left,R.id.seekBarBottom2LeftY,R.id.seekBarBottom2LeftZ,
                    R.id.bottomAngle2,R.id.bottomAngleL2,R.id.bottomAngle2Y,R.id.bottomAngleL2Y,R.id.bottomAngle2Z,R.id.bottomAngleL2Z,R.id.relativeHand, this);

            handUI.leftPB.setRotation(180);


            handUI.green = R.drawable.handgreen;
            handUI.yellow = R.drawable.handyellow;
            handUI.white = R.drawable.handwhite;
*/



            stimButton = (FloatingActionButton) findViewById(R.id.stim_buton);
            sensorStatus = (TextView) findViewById(R.id.SensorStatus);

            stimButton.bringToFront();
            stimButton.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (bleService.fireflyFound) {
                        //bleService.sendMessageForPCM("disconnect pcm");
                        bleService.fireflyGatt.disconnect();
                        bleService.fireflyGatt.close();
                        bleService.fireflyGatt = null;
                        setSensorStatus("PCM Disconnected");
                        stimButton.setImageResource(R.drawable.ic_flash_off_black_24dp);
                        bleService.fireflyFound = false;
                    }

                    return true;

                }

            });


            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Log.v(TAG, "permission granted");
                writeFile();
            }
            registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
        }


    }

//close BT connections on destroy

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleService.fireflyGatt != null) {
            bleService.fireflyGatt.disconnect();
            bleService.fireflyGatt.close();
            bleService.fireflyGatt = null;
        }

        if (bleService.hipGatt != null) {
            bleService.hipGatt.disconnect();
            bleService.hipGatt.close();
            bleService.hipGatt = null;
        }

        if (bleService.kneeGatt != null) {
            bleService.kneeGatt.disconnect();
            bleService.kneeGatt.close();
            bleService.kneeGatt = null;
        }

        if (bleService.ankleGatt != null) {
            bleService.ankleGatt.disconnect();
            bleService.ankleGatt.close();
            bleService.ankleGatt = null;
        }

        if (bleService.handGatt != null) {
            bleService.handGatt.disconnect();
            bleService.handGatt.close();
            bleService.handGatt = null;
        }

        Log.v("onDestroy", "DESTROYED");
    }

    //log that it stopped

    @Override
    protected void onStop() {

        super.onStop();
        writeFileAtStop("hip = ", hipUI);
        writeFileAtStop("knee = ", kneeUI);
        writeFileAtStop("ankle = ", ankleUI);
        writeFileAtStop("ankle = ", handUI);
        Log.v("onStop", "STOPPED");
    }

//deleted extraneous methods

    //something about restoring

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        int restore = 42;
        savedInstanceState.putInt("SOMETHING", restore);
    }

    //stim button clicked

    public void stimClicked(View v)
    {
        //todo bleService.fireflyGatt will always be null now the fireflyGatt instance is only in the pcmService
        if (bleService.fireflyGatt != null) {
            if (!statusVariables.stimming) {
                statusVariables.stimming = true;
                triggerFirefly(fireflyCommands.startStim);
                timerHandler.postDelayed(fireflyStop, 1000);
                timerHandler.postDelayed(fireflyDebounce, 5000);
                //bleService.sendMessageForPCM("stimulate");
                writeFile();
            }

        } else {
            // bleService.sendMessageForPCM("button clicked");
            bleService.searchingHip = false;
            bleService.searchingKnee = false;
            bleService.searchingAnkle = false;
            bleService.searchingHand = false;
            bleService.searchingPCM = true;
            setSensorStatus("Searching for PCM");
            bleService.scanner.startScan(bleService.mScanCallback);
            scanCount = 20;
            bleService.scanning = true;
            timerHandler.postDelayed(scanStop, 1000);
        }

    }

//handling permissions

    @Override

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "coarse location permission granted");
                    Intent bleIntent = new Intent(this, BleService.class);
                    startService(bleIntent);
                    bindService(bleIntent, mServiceConnection, this.BIND_AUTO_CREATE);
                } else
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

//is this necessary?

    public static Context getAppContext() {
        return MainActivity.context;
    }
    //check if stimulation is needed or not
    public void checkValue(final int value, final SensorUI sensor) {
        if (value > sensor.rightSB.getProgress() | (value * -1) > sensor.leftSB.getProgress()) {
            if (!statusVariables.stimming) {
                statusVariables.stimming = true;
                Log.v(TAG, "Start command");
                triggerFirefly(fireflyCommands.startStim);
                timerHandler.postDelayed(fireflyStop, 1000);
                timerHandler.postDelayed(fireflyDebounce, 5000);
            }
        }

        //check Y
        if (value > sensor.rightSBY.getProgress() | (value * -1) > sensor.leftSBY.getProgress()) {
            if (!statusVariables.stimming) {
                statusVariables.stimming = true;
                Log.v(TAG, "Start command");
                triggerFirefly(fireflyCommands.startStim);
                timerHandler.postDelayed(fireflyStop, 1000);
                timerHandler.postDelayed(fireflyDebounce, 5000);
            }
        }

        //end y


        //check Z

        if (value > sensor.rightSBZ.getProgress() | (value * -1) > sensor.leftSBZ.getProgress()) {
            if (!statusVariables.stimming) {
                statusVariables.stimming = true;
                Log.v(TAG, "Start command");
                triggerFirefly(fireflyCommands.startStim);
                timerHandler.postDelayed(fireflyStop, 1000);
                timerHandler.postDelayed(fireflyDebounce, 5000);
            }
        }

        //end z

    }

    //GAUGE


    //set progress bar for X axis

    public void setGaugeValueX(final int value, final SensorUI sensor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value < 0 & value > -90) {
                    sensor.leftPB.setProgress(-1 * value);
                    sensor.rightPB.setProgress(0);
                    if (sensor == hipUI) {
                        hipData.add(Integer.toString(value) + " ");
                        //hipCount++;
                        hipData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //hipCount++;
                    }
                    if (sensor == kneeUI) {
                        kneeData.add(Integer.toString(value) + " ");
                        //kneeCount++;
                        kneeData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //kneeCount++;
                    }
                    if (sensor == ankleUI) {
                        ankleData.add(Integer.toString(value) + " ");
                        //ankleCount++;
                        ankleData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //ankleCount++;
                    }
                    if (sensor == handUI) {
                        handData.add(Integer.toString(value) + " ");
                        //ankleCount++;
                        handData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //ankleCount++;
                    }
                    //writeFile(value, sensor);
                } else if (value > 0 & value < 90) {
                    sensor.leftPB.setProgress(0);
                    sensor.rightPB.setProgress(value);
                    if (sensor == hipUI) {
                        hipData.add(Integer.toString(value) + " ");
                        //hipCount++;
                        hipData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //hipCount++;
                    }
                    if (sensor == kneeUI) {
                        kneeData.add(Integer.toString(value) + " ");
                        //kneeCount++;
                        kneeData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //kneeCount++;
                    }
                    if (sensor == ankleUI) {
                        ankleData.add(Integer.toString(value) + " ");
                        //ankleCount++;
                        ankleData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //ankleCount++;
                    }
                    if (sensor == handUI) {
                        handData.add(Integer.toString(value) + " ");
                        //ankleCount++;

                        handData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //ankleCount++;

                    }

                    //writeFile(value, sensor);

                }

                if (value > sensor.rightSB.getProgress() & value < 90) {

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


                if (value < sensor.rightSB.getProgress() & (value * -1) < sensor.leftSB.getProgress()) {


                    sensor.relativeLayout.setBackgroundColor(Color.parseColor("#404040"));

                    if (sensor == kneeUI) {

                        sensor.relativeLayout.setBackgroundColor(Color.parseColor("#333333"));

                    }


                }

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

                if (value < 0 & value > -90) {

                    sensor.leftPBY.setProgress(-1 * value);

                    sensor.rightPBY.setProgress(0);


                    if (sensor == hipUI) {

                        hipData.add(Integer.toString(value) + " ");

                        //hipCount++;

                        hipData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //hipCount++;

                    }

                    if (sensor == kneeUI) {

                        kneeData.add(Integer.toString(value) + " ");

                        //kneeCount++;

                        kneeData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //kneeCount++;

                    }

                    if (sensor == ankleUI) {

                        ankleData.add(Integer.toString(value) + " ");

                        //ankleCount++;

                        ankleData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //ankleCount++;

                    }

                    if (sensor == handUI) {

                        handData.add(Integer.toString(value) + " ");

                        //ankleCount++;

                        handData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //ankleCount++;

                    }

                    //writeFile(value, sensor);

                } else if (value > 0 & value < 90) {

                    sensor.leftPBY.setProgress(0);

                    sensor.rightPBY.setProgress(value);

                    if (sensor == hipUI) {

                        hipData.add(Integer.toString(value) + " ");

                        //hipCount++;

                        hipData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //hipCount++;

                    }

                    if (sensor == kneeUI) {

                        kneeData.add(Integer.toString(value) + " ");

                        //kneeCount++;

                        kneeData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //kneeCount++;

                    }

                    if (sensor == ankleUI) {

                        ankleData.add(Integer.toString(value) + " ");

                        //ankleCount++;

                        ankleData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //ankleCount++;

                    }

                    if (sensor == handUI) {

                        handData.add(Integer.toString(value) + " ");

                        //ankleCount++;

                        handData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //ankleCount++;

                    }

                    //writeFile(value, sensor);

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
                    if (sensor == kneeUI) {
                        sensor.relativeLayout.setBackgroundColor(Color.parseColor("#333333"));
                    }
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
                if (value < 0 & value > -90) {
                    sensor.leftPBZ.setProgress(-1 * value);
                    sensor.rightPBZ.setProgress(0);
                    if (sensor == hipUI) {
                        hipData.add(Integer.toString(value) + " ");
                        //hipCount++;
                        hipData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //hipCount++;
                    }
                    if (sensor == kneeUI) {
                        kneeData.add(Integer.toString(value) + " ");
                        //kneeCount++;
                        kneeData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //kneeCount++;
                    }
                    if (sensor == ankleUI) {
                        ankleData.add(Integer.toString(value) + " ");
                        //ankleCount++;
                        ankleData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //ankleCount++;
                    }
                    if (sensor == handUI) {
                        handData.add(Integer.toString(value) + " ");
                        //ankleCount++;
                        handData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //ankleCount++;

                    }

                    //writeFile(value, sensor);

                } else if (value > 0 & value < 90) {

                    sensor.leftPBZ.setProgress(0);

                    sensor.rightPBZ.setProgress(value);

                    if (sensor == hipUI) {

                        hipData.add(Integer.toString(value) + " ");

                        //hipCount++;

                        hipData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //hipCount++;

                    }

                    if (sensor == kneeUI) {

                        kneeData.add(Integer.toString(value) + " ");

                        //kneeCount++;

                        kneeData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //kneeCount++;

                    }

                    if (sensor == ankleUI) {

                        ankleData.add(Integer.toString(value) + " ");

                        //ankleCount++;

                        ankleData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //ankleCount++;

                    }

                    if (sensor == handUI) {

                        handData.add(Integer.toString(value) + " ");

                        //ankleCount++;

                        handData.add(Long.toString(System.currentTimeMillis()) + "\n");

                        //ankleCount++;

                    }

                    //writeFile(value, sensor);

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

                    if (sensor == kneeUI) {

                        sensor.relativeLayout.setBackgroundColor(Color.parseColor("#333333"));

                    }


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

    public void triggerFirefly(byte[] onOff)

    {

        if (bleService.fireflyFound) {

            bleService.FIREFLY_CHARACTERISTIC2.setValue(onOff);

            boolean b = bleService.fireflyGatt.writeCharacteristic(bleService.FIREFLY_CHARACTERISTIC2);

            Log.i(TAG, "firefly write status = " + b);

        }


    }

    Runnable fireflyStop = new Runnable() {

        @Override

        public void run() {

            if (bleService.fireflyFound) {

                Log.v(TAG, "Stop command");

                triggerFirefly(fireflyCommands.stopStim);

            }

        }

    };

//scan for BT devices function

    Runnable scanStop = new Runnable() {

        @Override

        public void run() {

            if (bleService.scanning) {

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

                            if (bleService.hipGatt == null) {
                                hipUI.connect.setBackgroundResource(R.drawable.chestwhite);
                            }

                            if (bleService.kneeGatt == null) {
                                kneeUI.connect.setBackgroundResource(R.drawable.armwhite);
                            }

                            if (bleService.ankleGatt == null) {
                                ankleUI.connect.setBackgroundResource(R.drawable.wristwhite);
                            }

                            if (bleService.handGatt == null) {
                                handUI.connect.setBackgroundResource(R.drawable.handwhite);
                            }

                        }

                    });

                }

            }

        }

    };


    Runnable fireflyDebounce = new Runnable() {

        @Override

        public void run() {

            statusVariables.stimming = false;

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

            footClickCount = 0;

            kneeClickCount = 0;

            hipClickCount = 0;

            handClickCount = 0;

        }

    };


    public void connectThigh(View v) {

        if (bleService.hipGatt == null) {

            runOnUiThread(new Runnable() {

                @Override

                public void run() {

                    setSensorStatus("Searching");

                    hipUI.connect.setBackgroundResource(R.drawable.chestyellow);

                    if (bleService.kneeGatt == null) {
                        kneeUI.connect.setBackgroundResource(R.drawable.armwhite);
                    }

                    if (bleService.ankleGatt == null) {
                        ankleUI.connect.setBackgroundResource(R.drawable.wristwhite);
                    }

                    /*if (bleService.handGatt == null) {
                        handUI.connect.setBackgroundResource(R.drawable.handwhite);
                    }*/

                }

            });

            bleService.searchingHip = true;

            bleService.searchingKnee = false;

            bleService.searchingAnkle = false;

            bleService.searchingHand = false;

            bleService.searchingPCM = false;

            bleService.scanner.startScan(bleService.mScanCallback);

            scanCount = scanCount + 20;

            bleService.scanning = true;

            timerHandler.postDelayed(scanStop, 1000);

        } else {

            hipClickCount++;

            timerHandler.postDelayed(doubleClick, 500);

            if (hipClickCount == 2) {

                bleService.hipGatt.disconnect();

                bleService.hipGatt.close();

                bleService.hipGatt = null;

                setSensorStatus("Sensor disconnected");

                runOnUiThread(new Runnable() {

                    @Override

                    public void run() {

                        hipUI.connect.setBackgroundResource(R.drawable.chestwhite);

                        hipUI.leftTV.setVisibility(View.INVISIBLE);

                        hipUI.rightTV.setVisibility(View.INVISIBLE);

                    }

                });

                hipClickCount = 0;

            }

        }

    }

    public void connectLowerLeg(View v) {

        if (bleService.kneeGatt == null) {

            runOnUiThread(new Runnable() {

                @Override

                public void run() {

                    setSensorStatus("Searching");

                    kneeUI.connect.setBackgroundResource(R.drawable.armyellow);

                    if (bleService.ankleGatt == null) {
                        ankleUI.connect.setBackgroundResource(R.drawable.wristwhite);
                    }

                    if (bleService.hipGatt == null) {
                        hipUI.connect.setBackgroundResource(R.drawable.chestwhite);
                    }

                    /*if (bleService.handGatt == null) {
                        handUI.connect.setBackgroundResource(R.drawable.handwhite);
                    }*/

                }

            });

            bleService.searchingKnee = true;

            bleService.searchingHip = false;

            bleService.searchingAnkle = false;

            bleService.searchingHand = false;

            bleService.searchingPCM = false;

            bleService.scanner.startScan(bleService.mScanCallback);

            scanCount = scanCount + 20;

            bleService.scanning = true;

            timerHandler.postDelayed(scanStop, 1000);

        } else {

            kneeClickCount++;

            timerHandler.postDelayed(doubleClick, 500);

            if (kneeClickCount == 2) {

                bleService.kneeGatt.disconnect();

                bleService.kneeGatt.close();

                bleService.kneeGatt = null;

                setSensorStatus("Sensor disconnected");

                runOnUiThread(new Runnable() {

                    @Override

                    public void run() {

                        kneeUI.connect.setBackgroundResource(R.drawable.armwhite);

                        kneeUI.leftTV.setVisibility(View.INVISIBLE);

                        kneeUI.rightTV.setVisibility(View.INVISIBLE);

                    }

                });

                kneeClickCount = 0;

            }

        }


    }


    public void connectFoot(View v) {

        if (bleService.ankleGatt == null) {

            runOnUiThread(new Runnable() {

                @Override

                public void run() {

                    setSensorStatus("Searching");

                    ankleUI.connect.setBackgroundResource(R.drawable.wristyellow);

                    if (bleService.hipGatt == null) {
                        hipUI.connect.setBackgroundResource(R.drawable.chestwhite);
                    }

                    if (bleService.kneeGatt == null) {
                        kneeUI.connect.setBackgroundResource(R.drawable.armwhite);
                    }

                    /*if (bleService.handGatt == null) {
                        kneeUI.connect.setBackgroundResource(R.drawable.handwhite);
                    }*/

                }

            });

            bleService.searchingHip = false;

            bleService.searchingKnee = false;

            bleService.searchingAnkle = true;

            bleService.searchingHand = false;

            bleService.searchingPCM = false;

            bleService.scanner.startScan(bleService.mScanCallback);

            scanCount = scanCount + 20;

            bleService.scanning = true;

            timerHandler.postDelayed(scanStop, 1000);

        } else {

            footClickCount++;

            timerHandler.postDelayed(doubleClick, 500);

            if (footClickCount == 2) {

                bleService.ankleGatt.disconnect();

                bleService.ankleGatt.close();

                bleService.ankleGatt = null;

                setSensorStatus("Sensor disconnected");

                runOnUiThread(new Runnable() {

                    @Override

                    public void run() {

                        ankleUI.connect.setBackgroundResource(R.drawable.wristwhite);

                        ankleUI.leftTV.setVisibility(View.INVISIBLE);

                        ankleUI.rightTV.setVisibility(View.INVISIBLE);

                    }

                });

                footClickCount = 0;

            }

        }

    }


    public void connectHand(View v) {

        if (bleService.handGatt == null) {

            runOnUiThread(new Runnable() {

                @Override

                public void run() {

                    setSensorStatus("Searching");

                    handUI.connect.setBackgroundResource(R.drawable.handyellow);

                    if (bleService.hipGatt == null) {
                        hipUI.connect.setBackgroundResource(R.drawable.chestwhite);
                    }

                    if (bleService.kneeGatt == null) {
                        kneeUI.connect.setBackgroundResource(R.drawable.armwhite);
                    }

                    if (bleService.ankleGatt == null) {
                        ankleUI.connect.setBackgroundResource(R.drawable.wristwhite);
                    }

                }

            });

            bleService.searchingHip = false;

            bleService.searchingKnee = false;

            bleService.searchingAnkle = false;

            bleService.searchingHand = true;

            bleService.searchingPCM = false;

            bleService.scanner.startScan(bleService.mScanCallback);

            scanCount = scanCount + 20;

            bleService.scanning = true;

            timerHandler.postDelayed(scanStop, 1000);

        } else {

            handClickCount++;

            timerHandler.postDelayed(doubleClick, 500);

            if (handClickCount == 2) {

                bleService.handGatt.disconnect();

                bleService.handGatt.close();

                bleService.handGatt = null;

                setSensorStatus("Sensor disconnected");

                runOnUiThread(new Runnable() {

                    @Override

                    public void run() {

                        handUI.connect.setBackgroundResource(R.drawable.handwhite);

                        handUI.leftTV.setVisibility(View.INVISIBLE);

                        handUI.rightTV.setVisibility(View.INVISIBLE);

                    }

                });

                handClickCount = 0;

            }

        }

    }

//do stuff when the BT broadcast tells it to, this is where the find gauge value function and our problem likely is

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            if (eventType.equals("sensorConnected")) {
                if (extras.getString("gatt").equals("hip")) {
                    connectSensor(hipUI);
                    hipUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            hipUI.calibrateSensor(hipUI);
                            return true;
                        }
                    });
                }
                if (extras.getString("gatt").equals("knee")) {
                    connectSensor(kneeUI);
                    kneeUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            kneeUI.calibrateSensor(kneeUI);
                            return true;
                        }
                    });
                }

                if (extras.getString("gatt").equals("ankle")) {
                    connectSensor(ankleUI);
                    ankleUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ankleUI.calibrateSensor(ankleUI);
                            return true;
                        }
                    });
                }
                if (extras.getString("gatt").equals("hand")) {
                    connectSensor(handUI);
                    handUI.connect.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            handUI.calibrateSensor(handUI);
                            return true;
                        }
                    });
                }

                if (extras.getString("gatt").equals("firefly")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stimButton.setVisibility(View.VISIBLE);
                            setSensorStatus("PCM Connected");
                            stimButton.setImageResource(R.drawable.ic_flash_on_24dp);
                        }
                    });
                }
                if (extras.getString("gatt").equals("unknown")) {
                    Log.v(TAG, "unknown gatt");
                }
                Log.v("bleService", "connected message sent");
            }
            if (eventType.equals("sensorDisconnected")) {
                if (extras.getString("gatt").equals("hip")) {
                    onSensorDisconnected(hipUI);
                    if (bleService.hipGatt != null) {
                        bleService.hipGatt.close();
                        bleService.hipGatt = null;
                    }
                }
                if (extras.getString("gatt").equals("knee")) {
                    onSensorDisconnected(kneeUI);
                    if (bleService.kneeGatt != null) {
                        bleService.kneeGatt.close();
                        bleService.kneeGatt = null;
                    }
                }
                if (extras.getString("gatt").equals("ankle")) {
                    onSensorDisconnected(ankleUI);
                    if (bleService.ankleGatt != null) {
                        bleService.ankleGatt.close();
                        bleService.ankleGatt = null;
                    }
                }
                if (extras.getString("gatt").equals("firefly")) {
                    setSensorStatus("PCM Disconnected");
                    if (bleService.fireflyGatt != null) {
                        bleService.fireflyGatt.close();
                        bleService.fireflyGatt = null;
                        stimButton.setImageResource(R.drawable.ic_flash_off_black_24dp);

                    }

                }

            }

            if (eventType.equals("notification")) {
                BleNotification notification = intent.getParcelableExtra("notifyObject");
                if (notification.gatt.equals("hip")) {
                    //find value x, switched to different value coding
                    findGaugeValueX(hipUI, notification.valueX);
                    //find value y, switched to different value coding
                    findGaugeValueY(hipUI, notification.valueY);
                    //find value z, switched to different value coding
                    findGaugeValueZ(hipUI, notification.valueZ);
                } else if (notification.gatt.equals("knee")) {
                    //find value x
                    findGaugeValueX(kneeUI, notification.valueX);
                    //find value y
                    findGaugeValueY(kneeUI, notification.valueY);
                    //find value z
                    findGaugeValueZ(kneeUI, notification.valueZ);
                } else if (notification.gatt.equals("ankle")) {
                    //find value x
                    findGaugeValueX(ankleUI, notification.valueX);
                    //find value y
                    findGaugeValueY(ankleUI, notification.valueY);
                    //find value z
                    findGaugeValueZ(ankleUI, notification.valueZ);
                }
                /*if(extras.getString("gatt").equals("hip")){
                    Float value = extras.getFloat("value");
                    findGaugeValue(hipUI,value);
                }
                if(extras.getString("gatt").equals("knee")){
                    Float value = extras.getFloat("value");
                    findGaugeValue(kneeUI,value);
                }
                if(extras.getString("gatt").equals("ankle")){
                    Float value = extras.getFloat("value");
                    findGaugeValue(ankleUI,value);
                }*/

            }

            if (eventType.equals("fireflyConnected")) {

                runOnUiThread(new Runnable() {

                    @Override

                    public void run() {

                        stimButton.setVisibility(View.VISIBLE);

                        setSensorStatus("PCM Connected");

                        stimButton.setImageResource(R.drawable.ic_flash_on_24dp);

                    }

                });

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


/*
    public void startUart(View v) {

        //opens Adafruit's App with only UART capabilities

        Intent intent = new Intent(this, MainActivityA.class);

        startActivity(intent);



        //Discover the available device name



    }
    */

//display device addresses in the details page if applicable

    public void startDetails(View v) {

        Intent intent = new Intent(this, DetailsActivity.class);

        if (bleService.ankleGatt != null) {

            String ankleDeviceAddress = bleService.ankleGatt.getDevice().getAddress().toString();

            intent.putExtra("ankleDeviceAddress", ankleDeviceAddress);

        } else {

            String string = "not connected";

            intent.putExtra("ankleDeviceAddress", string);

        }

        if (bleService.kneeGatt != null) {

            String kneeDeviceAddress = bleService.kneeGatt.getDevice().getAddress().toString();

            intent.putExtra("kneeDeviceAddress", kneeDeviceAddress);

        } else {

            String string = "not connected";

            intent.putExtra("kneeDeviceAddress", string);

        }

        if (bleService.hipGatt != null) {

            String hipDeviceAddress = bleService.hipGatt.getDevice().getAddress().toString();

            intent.putExtra("hipDeviceAddress", hipDeviceAddress);

        } else {

            String string = "not connected";

            intent.putExtra("hipDeviceAddress", string);

        }

        startActivity(intent);

    }

//get 10 calibration data points then display the data

    public void findGaugeValueX(final SensorUI sensor, float gyroX) {

        if (sensor.calibrate & sensor.calibrateCounter < 10) {

            sensor.calibrateCounter++;

            sensor.average = sensor.average + gyroX;


        } else if (sensor.calibrate & sensor.calibrateCounter == 10) {

            sensor.average = sensor.average / 10;

            sensor.calibrateCounter++;

        } else if (sensor.calibrate & sensor.calibrateCounter > 10) {

            if ((sensor.average + 90.0) <= 180 & (sensor.average - 90) >= -180) {

                checkValue((int) (gyroX + (-1 * sensor.average)), sensor);

                setGaugeValueX((int) (gyroX + (-1 * sensor.average)), sensor);

                int correctedValue = (int) (gyroX + (-1 * sensor.average));


            } else if ((sensor.average + 90) > 180) {

                if (gyroX < 0) {

                    checkValue((int) ((180 - sensor.average) + (gyroX + 180)), sensor);

                    setGaugeValueX((int) ((180 - sensor.average) + (gyroX + 180)), sensor);

                    int correctedValue = (int) ((180 - sensor.average) + (gyroX + 180));

                } else if (gyroX > 0) {

                    checkValue((int) (gyroX + (-1 * sensor.average)), sensor);

                    setGaugeValueX((int) (gyroX + (-1 * sensor.average)), sensor);

                    int correctedValue = (int) (gyroX + (-1 * sensor.average));

                }


            } else if ((sensor.average - 90) < -180) {

                if (gyroX < 0) {

                    checkValue((int) (gyroX + (-1 * sensor.average)), sensor);

                    setGaugeValueX((int) (gyroX + (-1 * sensor.average)), sensor);

                    int correctedValue = (int) (gyroX + (-1 * sensor.average));

                }

                if (gyroX > 0) {

                    checkValue((int) ((-180 - sensor.average) + (gyroX - 180)), sensor);

                    setGaugeValueX((int) ((-180 - sensor.average) + (gyroX - 180)), sensor);

                    int correctedValue = (int) ((-180 - sensor.average) + (gyroX - 180));

                }


            }

        }

    }


    //find gyroY

    public void findGaugeValueY(final SensorUI sensor, float gyroY) {

        if (sensor.calibrate & sensor.calibrateCounter < 10) {

            sensor.calibrateCounter++;

            sensor.average = sensor.average + gyroY;


        } else if (sensor.calibrate & sensor.calibrateCounter == 10) {

            sensor.average = sensor.average / 10;

            sensor.calibrateCounter++;

        } else if (sensor.calibrate & sensor.calibrateCounter > 10) {

            if ((sensor.average + 90.0) <= 180 & (sensor.average - 90) >= -180) {

                checkValue((int) (gyroY + (-1 * sensor.average)), sensor);

                setGaugeValueY((int) (gyroY + (-1 * sensor.average)), sensor);

                int correctedValue = (int) (gyroY + (-1 * sensor.average));


            } else if ((sensor.average + 90) > 180) {

                if (gyroY < 0) {

                    checkValue((int) ((180 - sensor.average) + (gyroY + 180)), sensor);

                    setGaugeValueY((int) ((180 - sensor.average) + (gyroY + 180)), sensor);

                    int correctedValue = (int) ((180 - sensor.average) + (gyroY + 180));

                } else if (gyroY > 0) {

                    checkValue((int) (gyroY + (-1 * sensor.average)), sensor);

                    setGaugeValueY((int) (gyroY + (-1 * sensor.average)), sensor);

                    int correctedValue = (int) (gyroY + (-1 * sensor.average));

                }


            } else if ((sensor.average - 90) < -180) {

                if (gyroY < 0) {

                    checkValue((int) (gyroY + (-1 * sensor.average)), sensor);

                    setGaugeValueY((int) (gyroY + (-1 * sensor.average)), sensor);

                    int correctedValue = (int) (gyroY + (-1 * sensor.average));

                }

                if (gyroY > 0) {

                    checkValue((int) ((-180 - sensor.average) + (gyroY - 180)), sensor);

                    setGaugeValueY((int) ((-180 - sensor.average) + (gyroY - 180)), sensor);

                    int correctedValue = (int) ((-180 - sensor.average) + (gyroY - 180));

                }


            }

        }

    }

    //end of gyroY


    //find gyroZ

    public void findGaugeValueZ(final SensorUI sensor, float gyroZ) {

        if (sensor.calibrate & sensor.calibrateCounter < 10) {

            sensor.calibrateCounter++;

            sensor.average = sensor.average + gyroZ;


        } else if (sensor.calibrate & sensor.calibrateCounter == 10) {

            sensor.average = sensor.average / 10;

            sensor.calibrateCounter++;

        } else if (sensor.calibrate & sensor.calibrateCounter > 10) {

            if ((sensor.average + 90.0) <= 180 & (sensor.average - 90) >= -180) {

                checkValue((int) (gyroZ + (-1 * sensor.average)), sensor);

                setGaugeValueZ((int) (gyroZ + (-1 * sensor.average)), sensor);

                int correctedValue = (int) (gyroZ + (-1 * sensor.average));


            } else if ((sensor.average + 90) > 180) {

                if (gyroZ < 0) {

                    checkValue((int) ((180 - sensor.average) + (gyroZ + 180)), sensor);

                    setGaugeValueZ((int) ((180 - sensor.average) + (gyroZ + 180)), sensor);

                    int correctedValue = (int) ((180 - sensor.average) + (gyroZ + 180));

                } else if (gyroZ > 0) {

                    checkValue((int) (gyroZ + (-1 * sensor.average)), sensor);

                    setGaugeValueZ((int) (gyroZ + (-1 * sensor.average)), sensor);

                    int correctedValue = (int) (gyroZ + (-1 * sensor.average));

                }


            } else if ((sensor.average - 90) < -180) {

                if (gyroZ < 0) {

                    checkValue((int) (gyroZ + (-1 * sensor.average)), sensor);

                    setGaugeValueZ((int) (gyroZ + (-1 * sensor.average)), sensor);

                    int correctedValue = (int) (gyroZ + (-1 * sensor.average));

                }

                if (gyroZ > 0) {

                    checkValue((int) ((-180 - sensor.average) + (gyroZ - 180)), sensor);

                    setGaugeValueZ((int) ((-180 - sensor.average) + (gyroZ - 180)), sensor);

                    int correctedValue = (int) ((-180 - sensor.average) + (gyroZ - 180));

                }


            }

        }

    }

    //end of gyroZ


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

            if (sensor == hipUI) {

                data = data.concat(hipData.toString() + ";\n");

            }

            if (sensor == kneeUI) {

                data = data.concat(kneeData.toString() + ";\n");

            }

            if (sensor == ankleUI) {

                data = data.concat(ankleData.toString() + ";\n");

            }

            outputStream.write(data.getBytes());

            outputStream.flush();

            outputStream.close();

            MediaScannerConnection.scanFile(getAppContext(), new String[]{fullPath}, null, null);


        } catch (Exception e) {

            e.printStackTrace();

        }

    }


}
// Arm CAL BEGIN //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
