package com.ron.zedboardtestapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zedboard.zynqutil.AppUpdater;
import com.zedboard.zynqutil.ZedboardPreference;
import com.zedboard.zynqutil.ZedboardUtil;

import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;


public class MainActivity extends Activity {

    private final static String TAG = "ZedboardTestApp";

    private Button buttonUpload;
    private Button buttonWiFiStatus;
    private Button buttonEnableWiFi;
    private Button buttonWiFiSetting;
    private Button buttonConnectWiFi;
    private Button buttonGetNTPTime;
    private Button buttonUpdateApp;
    private Button buttonPreference;
    private Button buttonReadRegister;
    private Button buttonWriteRegister;
    private Button button2DCapture;
    private Button button3DCapture;

    private EditText editTextSSID;
    private EditText editTextPassword;
    private EditText editTextRegisterAddress;
    private EditText editTextRegisterValue;

    private TextView textViewBatteryCapacity;
    private TextView textViewNTPTime;

    private TextView textViewTouchX;
    private TextView textViewTouchY;

    private ZedboardUtil zutil;
    private ZedboardPreference preference;

    private boolean batteryIsCharging = false;
    private long batteryCapacity = 0;

    private PLContentProvider provider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zutil = new ZedboardUtil();
        preference = new ZedboardPreference(MainActivity.this);
        provider = PLContentProvider.getInstance();
        initUI();
        setupListener();



        /*boolean wifiConnected = zutil.isWiFiConnected(MainActivity.this);
        if (!wifiConnected) {
            zutil.enableWiFi(MainActivity.this);
            zutil.gotoWiFiSetting(MainActivity.this);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*boolean wifiConnected = zutil.isWiFiConnected(MainActivity.this);
        if (!wifiConnected) {
            zutil.enableWiFi(MainActivity.this);
            zutil.gotoWiFiSetting(MainActivity.this);
        }*/

        buttonUpload.setEnabled(false);

        editTextSSID.setText(preference.getSSID());
        editTextPassword.setText(preference.getPassword());

        zutil.registerBatteryReceiver(MainActivity.this, batteryInfoReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(batteryInfoReceiver);
    }

    private void initUI() {
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonUpload.setEnabled(false);

        buttonWiFiStatus = (Button) findViewById(R.id.buttonWiFiStatus);
        buttonEnableWiFi = (Button) findViewById(R.id.buttonEnableWiFi);
        buttonWiFiSetting = (Button) findViewById(R.id.buttonWiFiSetting);
        buttonConnectWiFi = (Button) findViewById(R.id.buttonConnectWiFi);
        buttonGetNTPTime = (Button) findViewById(R.id.buttonGetNTPTime);
        buttonUpdateApp = (Button) findViewById(R.id.buttonUpdateApp);
        buttonPreference = (Button) findViewById(R.id.buttonPreference);
        buttonReadRegister = (Button) findViewById(R.id.buttonReadRegister);
        buttonWriteRegister = (Button) findViewById(R.id.buttonWriteRegister);

        button2DCapture = (Button) findViewById(R.id.button2DCapture);
        button3DCapture = (Button) findViewById(R.id.button3DCapture);

        editTextSSID = (EditText) findViewById(R.id.editTextSSID);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextSSID.setText(preference.getSSID());
        editTextPassword.setText(preference.getPassword());

        editTextRegisterAddress = (EditText) findViewById(R.id.editTextRegister);
        editTextRegisterValue = (EditText) findViewById(R.id.editTextValue);

        textViewBatteryCapacity= (TextView) findViewById(R.id.textViewBatteryCapacity);
        textViewBatteryCapacity.setText("Battery Capacity:" + zutil.getBatteryCapacity(MainActivity.this));

        textViewTouchX = (TextView) findViewById(R.id.textViewTouchX);
        textViewTouchY = (TextView) findViewById(R.id.textViewTouchY);

        textViewNTPTime = (TextView) findViewById(R.id.textViewNTPTime);
    }

    private void setupListener() {
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                String filePath = provider.getCurrentContentFile();
                intent.putExtra("FILE_PATH", filePath);
                startActivity(intent);
            }
        });

        buttonWiFiStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean wifiConnected = zutil.isWiFiConnected(MainActivity.this);
                if (wifiConnected) {
                    Toast.makeText(MainActivity.this, "WiFi is connected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "WiFi is not connected", Toast.LENGTH_SHORT).show();
                    zutil.enableWiFi(MainActivity.this);
                    zutil.gotoWiFiSetting(MainActivity.this);
                }
            }
        });

        buttonEnableWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zutil.enableWiFi(MainActivity.this);
            }
        });

        buttonWiFiSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zutil.gotoWiFiSetting(MainActivity.this);
            }
        });

        buttonConnectWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zutil.connectWiFiAP(MainActivity.this, editTextSSID.getText().toString(),
                                    editTextPassword.getText().toString());
            }
        });

        buttonGetNTPTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                class NTPTask extends AsyncTask<String, Void, TimeStamp> {
                    @Override
                    protected TimeStamp doInBackground(String... strings) {
                        String ntpServer = strings[0];
                        TimeStamp currentTime = null;
                        try {
                            currentTime = zutil.queryNTPTime(ntpServer);
                            Log.d(TAG, "Current Time: " + currentTime.toDateString());
                        } catch (IOException e) {
                            Log.e(TAG, "Fail to get time from NTP server");
                            currentTime = null;
                        }
                        return currentTime;
                    }

                    @Override
                    protected void onPostExecute(TimeStamp timeStamp) {
                        if (timeStamp != null) {
                            textViewNTPTime.setText("Current Time: " + timeStamp.toDateString());
                            Toast.makeText(getApplicationContext(), "Current Time: " + timeStamp.toDateString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                new NTPTask().execute("pool.ntp.org");
            }
        });

        buttonUpdateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppUpdater appUpdater = new AppUpdater(MainActivity.this, true, true);
                appUpdater.execute("https://s3-ap-northeast-1.amazonaws.com/app-update-test/app-arm7-debug.apk");

            }
        });

        buttonPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
                startActivity(intent);
            }
        });

        buttonReadRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long address;
                long value;

                try {
                    address = Long.parseLong(editTextRegisterAddress.getText().toString(), 16);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid register address format");
                    Toast.makeText(MainActivity.this, "Invalid register address format", Toast.LENGTH_SHORT).show();
                    return;
                }
                value = provider.readRegister(address);
                if (value < 0) {
                    Log.e(TAG, "Could not get value");
                    Toast.makeText(MainActivity.this, "Could not get value", Toast.LENGTH_SHORT).show();
                    return;
                }
                editTextRegisterValue.setText(Long.toHexString(value));
                Toast.makeText(MainActivity.this, String.format("Read register %08x with %08x", address, value), Toast.LENGTH_SHORT).show();
            }
        });

        buttonWriteRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long address;
                long value;
                int result;

                try {
                    address = Long.parseLong(editTextRegisterAddress.getText().toString(), 16);
                    value = Long.parseLong(editTextRegisterValue.getText().toString(), 16);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid register address or value format");
                    Toast.makeText(MainActivity.this, "Invalid register address or value format", Toast.LENGTH_SHORT).show();
                    return;
                }
                result = provider.writeRegister(address, value);
                if (result == 0) {
                    Toast.makeText(MainActivity.this, String.format("Writed register %08x with %08x", address, value), Toast.LENGTH_SHORT).show();
                }
            }
        });

        button2DCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonUpload.setEnabled(false);
                button2DCapture.setEnabled(false);
                button3DCapture.setEnabled(false);
                provider.start2DCapture(MainActivity.this);
            }
        });

        button3DCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonUpload.setEnabled(false);
                button2DCapture.setEnabled(false);
                button3DCapture.setEnabled(false);
                provider.start3DCapture(MainActivity.this);
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        textViewTouchX.setText("X=" + x);
        textViewTouchY.setText("Y=" + y);
        return super.onTouchEvent(event);
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryInfo(intent);
        }
    };

    private void updateBatteryInfo(Intent intent) {
        int  health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
        int  level= intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
        int  plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
        boolean  present= intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        int  scale= intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
        int  status= intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
        String  technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        int  temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
        int  voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);

        Log.d(TAG, "Health: "+health+"\n"
                + "Level: "+level + "\n"
                + "Plugged: "+plugged + "\n"
                + "Present: "+present + "\n"
                + "Scale: "+scale + "\n"
                + "Status: "+status + "\n"
                + "Technology: "+technology + "\n"
                + "Temperature: "+temperature+"\n"
                + "Voltage: "+voltage+"\n");

        textViewBatteryCapacity.setText("Battery Capacity:" + level);
    }
}
