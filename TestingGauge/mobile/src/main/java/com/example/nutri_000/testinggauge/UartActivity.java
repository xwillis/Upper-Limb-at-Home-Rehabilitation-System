package com.example.nutri_000.testinggauge;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nutri_000.testinggauge.ble.BleManager;
import com.example.nutri_000.testinggauge.ble.BleUtils;
import com.example.nutri_000.testinggauge.mqtt.MqttManager;
import com.example.nutri_000.testinggauge.mqtt.MqttSettings;
import com.example.nutri_000.testinggauge.settings.ConnectedSettingsActivity;
import com.example.nutri_000.testinggauge.settings.MqttUartSettingsActivity;
import com.example.nutri_000.testinggauge.settings.PreferencesFragment;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UartActivity extends UartInterfaceActivity implements MqttManager.MqttManagerListener {
    // Log
    private final static String TAG = UartActivity.class.getSimpleName();

    // Configuration
    private final static boolean kUseColorsForData = true;
    public final static int kDefaultMaxPacketsToPaintAsText = 500;
    private final static int kInfoColor = Color.parseColor("#F21625");

    // Activity request codes (used for onActivityResult)
    private static final int kActivityRequestCode_ConnectedSettingsActivity = 0;
    private static final int kActivityRequestCode_MqttSettingsActivity = 1;

    // Constants
    private final static String kPreferences = "UartActivity_prefs";
    private final static String kPreferences_eol = "eol";
    private final static String kPreferences_eolCharactersId = "eolCharactersId";
    private final static String kPreferences_echo = "echo";
    private final static String kPreferences_asciiMode = "ascii";
    private final static String kPreferences_timestampDisplayMode = "timestampdisplaymode";

    // Colors
    private int mTxColor;
    private int mRxColor;

    // UI
    private EditText mBufferTextView;
    private ListView mBufferListView;
    private TimestampListAdapter mBufferListAdapter;
    private EditText mSendEditText;
    private MenuItem mMqttMenuItem;
    private Handler mMqttMenuItemAnimationHandler;
    private TextView mSentBytesTextView;
    private TextView mReceivedBytesTextView;

    // UI TextBuffer (refreshing the text buffer is managed with a timer because a lot of changes can arrive really fast and could stall the main thread)
    private Handler mUIRefreshTimerHandler = new Handler();
    private Runnable mUIRefreshTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUITimerRunning) {
                updateTextDataUI();
                // Log.d(TAG, "updateDataUI");
                mUIRefreshTimerHandler.postDelayed(this, 200);
            }
        }
    };
    private boolean isUITimerRunning = false;

    // Data
    private boolean mShowDataInHexFormat;
    private boolean mIsTimestampDisplayMode;
    private boolean mIsEchoEnabled;
    private boolean mIsEolEnabled;
    private int mEolCharactersId;

    private volatile SpannableStringBuilder mTextSpanBuffer;
    private volatile ArrayList<UartDataChunk> mDataBuffer;
    private volatile int mSentBytes;
    private volatile int mReceivedBytes;

    private DataFragment mRetainedDataFragment;

    private MqttManager mMqttManager;

    private int maxPacketsToPaintAsText;

    //store flex sensor values in array
    int[] flexSensor = {19,22,411,4,41};
    double angle;
    double[] angleArray;
    double[] flexSensorArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);

        mBleManager = BleManager.getInstance(this);
        restoreRetainedDataFragment();

        // Get default theme colors
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        mTxColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorControlActivated, typedValue, true);
        mRxColor = typedValue.data;

        // UI
        mBufferListView = (ListView) findViewById(R.id.bufferListView);
        mBufferListAdapter = new TimestampListAdapter(this, R.layout.layout_uart_datachunkitem);
        mBufferListView.setAdapter(mBufferListAdapter);
        mBufferListView.setDivider(null);

        mBufferTextView = (EditText) findViewById(R.id.bufferTextView);
        if (mBufferTextView != null) {
            mBufferTextView.setKeyListener(null);     // make it not editable
        }

        mSendEditText = (EditText) findViewById(R.id.sendEditText);
        mSendEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    onClickSend(null);
                    return true;
                }

                return false;
            }
        });
        mSendEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    // Dismiss keyboard when sendEditText loses focus
                    dismissKeyboard(view);
                }
            }
        });

        mSentBytesTextView = (TextView) findViewById(R.id.sentBytesTextView);
        mReceivedBytesTextView = (TextView) findViewById(R.id.receivedBytesTextView);

        // Read shared preferences
        maxPacketsToPaintAsText = PreferencesFragment.getUartTextMaxPackets(this);
        //Log.d(TAG, "maxPacketsToPaintAsText: "+maxPacketsToPaintAsText);

        // Read local preferences
        SharedPreferences preferences = getSharedPreferences(kPreferences, MODE_PRIVATE);
        mShowDataInHexFormat = !preferences.getBoolean(kPreferences_asciiMode, true);
        final boolean isTimestampDisplayMode = preferences.getBoolean(kPreferences_timestampDisplayMode, false);
        setDisplayFormatToTimestamp(isTimestampDisplayMode);
        mIsEchoEnabled = preferences.getBoolean(kPreferences_echo, true);
        mIsEolEnabled = preferences.getBoolean(kPreferences_eol, true);
        mEolCharactersId = preferences.getInt(kPreferences_eolCharactersId, 0);
        invalidateOptionsMenu();        // udpate options menu with current values

        // Continue
        onServicesDiscovered();

        // Mqtt init
        mMqttManager = MqttManager.getInstance(this);
        if (MqttSettings.getInstance(this).isConnected()) {
            mMqttManager.connectFromSavedSettings(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Setup listeners
        mBleManager.setBleListener(this);

        mMqttManager.setListener(this);
        updateMqttStatus();

        // Start UI refresh
        //Log.d(TAG, "add ui timer");
        updateUI();

        isUITimerRunning = true;
        mUIRefreshTimerHandler.postDelayed(mUIRefreshTimerRunnable, 0);

    }

    @Override
    public void onPause() {
        super.onPause();

        //Log.d(TAG, "remove ui timer");
        isUITimerRunning = false;
        mUIRefreshTimerHandler.removeCallbacksAndMessages(mUIRefreshTimerRunnable);

        // Save preferences
        SharedPreferences preferences = getSharedPreferences(kPreferences, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(kPreferences_echo, mIsEchoEnabled);
        editor.putBoolean(kPreferences_eol, mIsEolEnabled);
        editor.putInt(kPreferences_eolCharactersId, mEolCharactersId);
        editor.putBoolean(kPreferences_asciiMode, !mShowDataInHexFormat);
        editor.putBoolean(kPreferences_timestampDisplayMode, mIsTimestampDisplayMode);

        editor.apply();
    }

    @Override
    public void onDestroy() {
        // Disconnect mqtt
        if (mMqttManager != null) {
            mMqttManager.disconnect();
        }

        // Retain data
        saveRetainedDataFragment();

        super.onDestroy();
    }

    public void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onClickSend(View view) {
        String data = mSendEditText.getText().toString();
        mSendEditText.setText("");       // Clear editText

        uartSendData(data, false);
    }

    private void uartSendData(String data, boolean wasReceivedFromMqtt) {
        // MQTT publish to TX
        MqttSettings settings = MqttSettings.getInstance(UartActivity.this);
        if (!wasReceivedFromMqtt) {
            if (settings.isPublishEnabled()) {
                String topic = settings.getPublishTopic(MqttUartSettingsActivity.kPublishFeed_TX);
                final int qos = settings.getPublishQos(MqttUartSettingsActivity.kPublishFeed_TX);
                mMqttManager.publish(topic, data, qos);
            }
        }

        // Add eol
        if (mIsEolEnabled) {
            // Add newline character if checked
            data += getEolCharacters();//"\n";
        }

        // Send to uart
        if (!wasReceivedFromMqtt || settings.getSubscribeBehaviour() == MqttSettings.kSubscribeBehaviour_Transmit) {
            sendData(data);
            mSentBytes += data.length();
        }

        // Add to current buffer
        byte[] bytes = new byte[0];

        try {
            bytes = data.getBytes("UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        UartDataChunk dataChunk = new UartDataChunk(System.currentTimeMillis(), UartDataChunk.TRANSFERMODE_TX, bytes);
        mDataBuffer.add(dataChunk);

        final String formattedData = mShowDataInHexFormat ? BleUtils.bytesToHex2(bytes) : BleUtils.bytesToText(bytes, true);
        if (mIsTimestampDisplayMode) {
            final String currentDateTimeString = DateFormat.getTimeInstance().format(new Date(dataChunk.getTimestamp()));
            mBufferListAdapter.add(new TimestampData("[" + currentDateTimeString + "] TX: " + formattedData, mTxColor));
            mBufferListView.setSelection(mBufferListAdapter.getCount());
        }

        // Update UI
        updateUI();
    }

    private String getEolCharacters() {
        switch (mEolCharactersId) {
            case 1:
                return "\r";
            case 2:
                return "\n\r";
            case 3:
                return "\r\n";
            default:
                return "\n";
        }
    }

    private int getEolCharactersStringId() {
        switch (mEolCharactersId) {
            case 1:
                return R.string.uart_eolmode_r;
            case 2:
                return R.string.uart_eolmode_nr;
            case 3:
                return R.string.uart_eolmode_rn;
            default:
                return R.string.uart_eolmode_n;
        }
    }

    public void onClickCopy(View view) {
        String text = mBufferTextView.getText().toString(); // mShowDataInHexFormat ? mHexSpanBuffer.toString() : mAsciiSpanBuffer.toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("UART", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    public void onClickClear(View view) {
        mTextSpanBuffer.clear();
        mDataBufferLastSize = 0;
        mBufferListAdapter.clear();
        mBufferTextView.setText("");

        mDataBuffer.clear();
        mSentBytes = 0;
        mReceivedBytes = 0;
        updateUI();
    }

    public void onClickShare(View view) {
        String textToSend = mBufferTextView.getText().toString(); // (mShowDataInHexFormat ? mHexSpanBuffer : mAsciiSpanBuffer).toString();

        if (textToSend.length() > 0) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToSend);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.uart_share_subject));     // subject will be used if sent to an email app
            sendIntent.setType("text/*");       // Note: don't use text/plain because dropbox will not appear as destination
            // startActivity(sendIntent);
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.uart_sharechooser_title)));      // Always show the app-chooser
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.uart_share_empty))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void setDisplayFormatToTimestamp(boolean enabled) {
        mIsTimestampDisplayMode = enabled;
        mBufferTextView.setVisibility(enabled ? View.GONE : View.VISIBLE);
        mBufferListView.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    // region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_uart, menu);

        // Mqtt
        mMqttMenuItem = menu.findItem(R.id.action_mqttsettings);
        mMqttMenuItemAnimationHandler = new Handler();
        mMqttMenuItemAnimationRunnable.run();

        // DisplayMode
        MenuItem displayModeMenuItem = menu.findItem(R.id.action_displaymode);
        displayModeMenuItem.setTitle(String.format(getString(R.string.uart_action_displaymode_format), getString(mIsTimestampDisplayMode ? R.string.uart_displaymode_timestamp : R.string.uart_displaymode_text)));
        SubMenu displayModeSubMenu = displayModeMenuItem.getSubMenu();
        if (mIsTimestampDisplayMode) {
            MenuItem displayModeTimestampMenuItem = displayModeSubMenu.findItem(R.id.action_displaymode_timestamp);
            displayModeTimestampMenuItem.setChecked(true);
        } else {
            MenuItem displayModeTextMenuItem = displayModeSubMenu.findItem(R.id.action_displaymode_text);
            displayModeTextMenuItem.setChecked(true);
        }

        // DataMode
        MenuItem dataModeMenuItem = menu.findItem(R.id.action_datamode);
        dataModeMenuItem.setTitle(String.format(getString(R.string.uart_action_datamode_format), getString(mShowDataInHexFormat ? R.string.uart_format_hexadecimal : R.string.uart_format_ascii)));
        SubMenu dataModeSubMenu = dataModeMenuItem.getSubMenu();
        if (mShowDataInHexFormat) {
            MenuItem dataModeHexMenuItem = dataModeSubMenu.findItem(R.id.action_datamode_hex);
            dataModeHexMenuItem.setChecked(true);
        } else {
            MenuItem dataModeAsciiMenuItem = dataModeSubMenu.findItem(R.id.action_datamode_ascii);
            dataModeAsciiMenuItem.setChecked(true);
        }

        // Echo
        MenuItem echoMenuItem = menu.findItem(R.id.action_echo);
        echoMenuItem.setTitle(R.string.uart_action_echo);
        echoMenuItem.setChecked(mIsEchoEnabled);

        // Eol
        MenuItem eolMenuItem = menu.findItem(R.id.action_eol);
        eolMenuItem.setTitle(R.string.uart_action_eol);
        eolMenuItem.setChecked(mIsEolEnabled);

        // Eol Characters
        MenuItem eolModeMenuItem = menu.findItem(R.id.action_eolmode);
        eolModeMenuItem.setTitle(String.format(getString(R.string.uart_action_eolmode_format), getString(getEolCharactersStringId())));
        SubMenu eolModeSubMenu = eolModeMenuItem.getSubMenu();
        int selectedEolCharactersSubMenuId;
        switch (mEolCharactersId) {
            case 1:
                selectedEolCharactersSubMenuId = R.id.action_eolmode_r;
                break;
            case 2:
                selectedEolCharactersSubMenuId = R.id.action_eolmode_nr;
                break;
            case 3:
                selectedEolCharactersSubMenuId = R.id.action_eolmode_rn;
                break;
            default:
                selectedEolCharactersSubMenuId = R.id.action_eolmode_n;
                break;
        }
        MenuItem selectedEolCharacterMenuItem = eolModeSubMenu.findItem(selectedEolCharactersSubMenuId);
        selectedEolCharacterMenuItem.setChecked(true);

        return true;
    }

    private Runnable mMqttMenuItemAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            updateMqttStatus();
            mMqttMenuItemAnimationHandler.postDelayed(mMqttMenuItemAnimationRunnable, 500);
        }
    };
    private int mMqttMenuItemAnimationFrame = 0;

    private void updateMqttStatus() {
        if (mMqttMenuItem == null)
            return;      // Hack: Sometimes this could have not been initialized so we don't update icons

        MqttManager mqttManager = MqttManager.getInstance(this);
        MqttManager.MqqtConnectionStatus status = mqttManager.getClientStatus();

        if (status == MqttManager.MqqtConnectionStatus.CONNECTING) {
            final int kConnectingAnimationDrawableIds[] = {R.drawable.mqtt_connecting1, R.drawable.mqtt_connecting2, R.drawable.mqtt_connecting3};
            mMqttMenuItem.setIcon(kConnectingAnimationDrawableIds[mMqttMenuItemAnimationFrame]);
            mMqttMenuItemAnimationFrame = (mMqttMenuItemAnimationFrame + 1) % kConnectingAnimationDrawableIds.length;
        } else if (status == MqttManager.MqqtConnectionStatus.CONNECTED) {
            mMqttMenuItem.setIcon(R.drawable.mqtt_connected);
            mMqttMenuItemAnimationHandler.removeCallbacks(mMqttMenuItemAnimationRunnable);
        } else {
            mMqttMenuItem.setIcon(R.drawable.mqtt_disconnected);
            mMqttMenuItemAnimationHandler.removeCallbacks(mMqttMenuItemAnimationRunnable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        switch (id) {
            case R.id.action_help:
                startHelp();
                return true;

            case R.id.action_connected_settings:
                startConnectedSettings();
                return true;

            case R.id.action_refreshcache:
                if (mBleManager != null) {
                    mBleManager.refreshDeviceCache();
                }
                break;

            case R.id.action_mqttsettings:
                Intent intent = new Intent(this, MqttUartSettingsActivity.class);
                startActivityForResult(intent, kActivityRequestCode_MqttSettingsActivity);
                break;

            case R.id.action_displaymode_timestamp:
                setDisplayFormatToTimestamp(true);
                recreateDataView();
                invalidateOptionsMenu();
                return true;

            case R.id.action_displaymode_text:
                setDisplayFormatToTimestamp(false);
                recreateDataView();
                invalidateOptionsMenu();
                return true;

            case R.id.action_datamode_hex:
                mShowDataInHexFormat = true;
                recreateDataView();
                invalidateOptionsMenu();
                return true;

            case R.id.action_datamode_ascii:
                mShowDataInHexFormat = false;
                recreateDataView();
                invalidateOptionsMenu();
                return true;

            case R.id.action_echo:
                mIsEchoEnabled = !mIsEchoEnabled;
                invalidateOptionsMenu();
                return true;

            case R.id.action_eol:
                mIsEolEnabled = !mIsEolEnabled;
                invalidateOptionsMenu();
                return true;

            case R.id.action_eolmode_n:
                mEolCharactersId = 0;
                invalidateOptionsMenu();
                return true;

            case R.id.action_eolmode_r:
                mEolCharactersId = 1;
                invalidateOptionsMenu();
                return true;


            case R.id.action_eolmode_nr:
                mEolCharactersId = 2;
                invalidateOptionsMenu();
                return true;

            case R.id.action_eolmode_rn:
                mEolCharactersId = 3;
                invalidateOptionsMenu();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void startConnectedSettings() {
        // Launch connected settings activity
        Intent intent = new Intent(this, ConnectedSettingsActivity.class);
        startActivityForResult(intent, kActivityRequestCode_ConnectedSettingsActivity);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == kActivityRequestCode_ConnectedSettingsActivity && resultCode == RESULT_OK) {
            finish();
        } else if (requestCode == kActivityRequestCode_MqttSettingsActivity && resultCode == RESULT_OK) {

        }
    }

    private void startHelp() {
        // Launch app help activity
        Intent intent = new Intent(this, CommonHelpActivity.class);
        intent.putExtra("title", getString(R.string.uart_help_title));
        intent.putExtra("help", "uart_help.html");
        startActivity(intent);
    }
    // endregion

    // region BleManagerListener
    /*
    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }
*/
    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Log.d(TAG, "Disconnected. Back to previous activity");
        finish();
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        enableRxNotifications();
    }

    @Override
    public synchronized void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        super.onDataAvailable(characteristic);
        // UART RX
        if (characteristic.getService().getUuid().toString().equalsIgnoreCase(UUID_SERVICE)) {
            if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_RX)) {
                final byte[] bytes = characteristic.getValue();

                //look for 0xFF


                //convert bytes to float value
                //ByteBuffer buf = ByteBuffer.wrap(bytes);
                //float value = ByteBuffer.wrap(bytes).getFloat();
                //float value = byteValue(bytes);
                //System.out.println("byteValue: " + value);
                //display bytes received to strings
                 //String s = new String(bytes);
                //System.out.println("Text Decrypted: " + s);

                /*
                //convert flex sensor resistance value received to int
                //float resistance = Float.parseFloat(s);

                //store the first five resistance values in an array
                //thumb, index finger, middle finger, ring finger, pinky
                flexSensorArray=new double[10];
                angleArray = new double[10];
                for (int i =0;i<5;i++){
                    float resistance = Float.parseFloat(s);
                    Log.v(tag, "Calibrating flex sensors");
                    //store resistance values in array
                    flexSensorArray[i] = resistance;
                    System.out.println("flexSensor array value: " + flexSensorArray[i] + " value: " + i);
                    //pass resistance values into corresponding calibration curve equations
                    angleArray[i] = flexSensorCalibration(flexSensorArray[i], flexSensor[i]);
                    System.out.println("Flex sensor: " + flexSensor[i] + " Angle: " + angleArray[i]);

                }*/




                mReceivedBytes += bytes.length;

                //System.out.println("Bytes length: " + bytes.length);

                final UartDataChunk dataChunk = new UartDataChunk(System.currentTimeMillis(), UartDataChunk.TRANSFERMODE_RX, bytes);
                mDataBuffer.add(dataChunk);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsTimestampDisplayMode) {
                            final String currentDateTimeString = DateFormat.getTimeInstance().format(new Date(dataChunk.getTimestamp()));
                            final String formattedData = mShowDataInHexFormat ? BleUtils.bytesToHex2(bytes) : BleUtils.bytesToText(bytes, true);

                            mBufferListAdapter.add(new TimestampData("[" + currentDateTimeString + "] RX: " + formattedData, mRxColor));
                            //mBufferListAdapter.add("[" + currentDateTimeString + "] RX: " + formattedData);
                            //mBufferListView.smoothScrollToPosition(mBufferListAdapter.getCount() - 1);
                            mBufferListView.setSelection(mBufferListAdapter.getCount());
                        }
                        updateUI();
                    }
                });

                // MQTT publish to RX
                MqttSettings settings = MqttSettings.getInstance(UartActivity.this);
                if (settings.isPublishEnabled()) {
                    String topic = settings.getPublishTopic(MqttUartSettingsActivity.kPublishFeed_RX);
                    final int qos = settings.getPublishQos(MqttUartSettingsActivity.kPublishFeed_RX);
                    final String text = BleUtils.bytesToText(bytes, false);
                    mMqttManager.publish(topic, text, qos);
                }
            }
        }
    }


/*
    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }
    */
    // endregion

    private void addTextToSpanBuffer(SpannableStringBuilder spanBuffer, String text, int color) {

        if (kUseColorsForData) {
            final int from = spanBuffer.length();
            spanBuffer.append(text);
            spanBuffer.setSpan(new ForegroundColorSpan(color), from, from + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spanBuffer.append(text);
        }
    }

    private void updateUI() {
        mSentBytesTextView.setText(String.format(getString(R.string.uart_sentbytes_format), mSentBytes));
        mReceivedBytesTextView.setText(String.format(getString(R.string.uart_receivedbytes_format), mReceivedBytes));
    }

    private int mDataBufferLastSize = 0;

    private void updateTextDataUI() {

        if (!mIsTimestampDisplayMode) {
            if (mDataBufferLastSize != mDataBuffer.size()) {

                final int bufferSize = mDataBuffer.size();
                if (bufferSize > maxPacketsToPaintAsText) {
                    mDataBufferLastSize = bufferSize - maxPacketsToPaintAsText;
                    mTextSpanBuffer.clear();
                    addTextToSpanBuffer(mTextSpanBuffer, getString(R.string.uart_text_dataomitted) + "\n", kInfoColor);
                }

                // Log.d(TAG, "update packets: "+(bufferSize-mDataBufferLastSize));
                for (int i = mDataBufferLastSize; i < bufferSize; i++) {
                    final UartDataChunk dataChunk = mDataBuffer.get(i);
                    final boolean isRX = dataChunk.getMode() == UartDataChunk.TRANSFERMODE_RX;
                    final byte[] bytes = dataChunk.getData();
                    final String formattedData = mShowDataInHexFormat ? BleUtils.bytesToHex2(bytes) : BleUtils.bytesToText(bytes, true);
                    addTextToSpanBuffer(mTextSpanBuffer, formattedData, isRX ? mRxColor : mTxColor);
                }

                mDataBufferLastSize = mDataBuffer.size();
                mBufferTextView.setText(mTextSpanBuffer);
                mBufferTextView.setSelection(0, mTextSpanBuffer.length());        // to automatically scroll to the end
            }
        }
    }

    private void recreateDataView() {

        if (mIsTimestampDisplayMode) {
            mBufferListAdapter.clear();

            final int bufferSize = mDataBuffer.size();
            for (int i = 0; i < bufferSize; i++) {

                final UartDataChunk dataChunk = mDataBuffer.get(i);
                final boolean isRX = dataChunk.getMode() == UartDataChunk.TRANSFERMODE_RX;
                final byte[] bytes = dataChunk.getData();
                final String formattedData = mShowDataInHexFormat ? BleUtils.bytesToHex2(bytes) : BleUtils.bytesToText(bytes, true);

                final String currentDateTimeString = DateFormat.getTimeInstance().format(new Date(dataChunk.getTimestamp()));
                mBufferListAdapter.add(new TimestampData("[" + currentDateTimeString + "] " + (isRX ? "RX" : "TX") + ": " + formattedData, isRX ? mRxColor : mTxColor));
//                mBufferListAdapter.add("[" + currentDateTimeString + "] " + (isRX ? "RX" : "TX") + ": " + formattedData);
            }
            mBufferListView.setSelection(mBufferListAdapter.getCount());
        } else {
            mDataBufferLastSize = 0;
            mTextSpanBuffer.clear();
            mBufferTextView.setText("");
        }
    }


    // region DataFragment
    public static class DataFragment extends Fragment {
        private boolean mShowDataInHexFormat;
        private SpannableStringBuilder mTextSpanBuffer;
        private ArrayList<UartDataChunk> mDataBuffer;
        private int mSentBytes;
        private int mReceivedBytes;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private void restoreRetainedDataFragment() {
        // find the retained fragment
        FragmentManager fm = getFragmentManager();
        mRetainedDataFragment = (DataFragment) fm.findFragmentByTag(TAG);

        if (mRetainedDataFragment == null) {
            // Create
            mRetainedDataFragment = new DataFragment();
            fm.beginTransaction().add(mRetainedDataFragment, TAG).commit();

            mDataBuffer = new ArrayList<>();
            mTextSpanBuffer = new SpannableStringBuilder();
        } else {
            // Restore status
            mShowDataInHexFormat = mRetainedDataFragment.mShowDataInHexFormat;
            mTextSpanBuffer = mRetainedDataFragment.mTextSpanBuffer;
            mDataBuffer = mRetainedDataFragment.mDataBuffer;
            mSentBytes = mRetainedDataFragment.mSentBytes;
            mReceivedBytes = mRetainedDataFragment.mReceivedBytes;
        }
    }

    private void saveRetainedDataFragment() {
        mRetainedDataFragment.mShowDataInHexFormat = mShowDataInHexFormat;
        mRetainedDataFragment.mTextSpanBuffer = mTextSpanBuffer;
        mRetainedDataFragment.mDataBuffer = mDataBuffer;
        mRetainedDataFragment.mSentBytes = mSentBytes;
        mRetainedDataFragment.mReceivedBytes = mReceivedBytes;
    }
    // endregion


    // region MqttManagerListener

    @Override
    public void onMqttConnected() {
        updateMqttStatus();
    }

    @Override
    public void onMqttDisconnected() {
        updateMqttStatus();
    }

    @Override
    public void onMqttMessageArrived(String topic, MqttMessage mqttMessage) {
        final String message = new String(mqttMessage.getPayload());

        //Log.d(TAG, "Mqtt messageArrived from topic: " +topic+ " message: "+message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uartSendData(message, true);       // Don't republish to mqtt something received from mqtt
            }
        });

    }

    // endregion


    // region TimestampAdapter
    private class TimestampData {
        String text;
        int textColor;

        TimestampData(String text, int textColor) {
            this.text = text;
            this.textColor = textColor;
        }
    }

    private class TimestampListAdapter extends ArrayAdapter<TimestampData> {

        TimestampListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_uart_datachunkitem, parent, false);
            }

            TimestampData data = getItem(position);
            TextView textView = (TextView) convertView;
            textView.setText(data.text);
            textView.setTextColor(data.textColor);

            return convertView;
        }
    }
    // endregion

    //flex sensor calibration curve equations
    public double flexSensorCalibration(double resistance, int flexSensor){
        if(flexSensor == 19) {
            //Sensor 19
            angle = (resistance - 93538) / 1544.5;
        }
        else if(flexSensor == 22) {
            //Sensor 22
            angle = (resistance - 120436) / 1963.4;
        }
        else if(flexSensor == 411) {
            //Sensor 4 with tape
            angle = (resistance - 125951) / 1872.2;
        }
        else if(flexSensor == 4) {
            //Sensor 4
            angle = (resistance - 73942) / 1627.7;
        }
        else if(flexSensor == 41) {
            // Sensor 41
            angle = (resistance - 117017) / 1083.9;
        }
        else if(flexSensor == 27) {
            // Sensor 27
            angle = (resistance - 93086) / 1878.2;
        }
        else if(flexSensor == 26) {
            // Sensor 26
            angle = (resistance - 167201) / 1596.2;
        }
        else if(flexSensor == 11) {
            // Sensor 11
            angle = (resistance - 99423) / 1950;
        }
        else if(flexSensor == 9) {
            // Sensor 9
            angle = (resistance - 11448) / 2096.8;
        }
        else if(flexSensor == 2) {
            // Sensor 2
            angle = (resistance - 103636) / 805.89;
        }
        else if(flexSensor == 221) {
            // Sensor 22 que?
            angle = (resistance - 139338) / 1552.8;
        }
        else if(flexSensor == 7) {
            // Sensor 7
            angle = (resistance - 174899) / 633.01;
        }
        else if(flexSensor == 54) {
            // Sensor 54
            angle = (resistance - 151423) / 1884.2;
        }
        else if(flexSensor == 8) {
            // Sensor 8
            angle = (resistance - 118637) / 1729.6;
        }
        else if(flexSensor == 211) {
            // Sensor 2 yolo
            angle = (resistance - 131169) / 1699.1;
        }

        return angle;
    }



}
