package com.ron.zedboardtestapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;

import static com.ron.zedboardtestapp.ZedboardUtil.queryNTPTime;

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

    private EditText editTextSSID;
    private EditText editTextPassword;

    private TextView textViewBatteryCapacity;
    private TextView textViewNTPTime;

    private ZedboardUtil zutil;
    private ZedboardPreference preference;

    private boolean batteryIsCharging = false;
    private long batteryCapacity = 0;

    private TimeStamp currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zutil = new ZedboardUtil();
        preference = new ZedboardPreference(MainActivity.this);
        initUI();
        setupListener();

        zutil.registerBatteryReceiver(MainActivity.this, batteryInfoReceiver);

        boolean wifiConnected = zutil.isWiFiConnected(MainActivity.this);
        if (!wifiConnected) {
            zutil.enableWiFi(MainActivity.this);
            zutil.gotoWiFiSetting(MainActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean wifiConnected = zutil.isWiFiConnected(MainActivity.this);
        if (!wifiConnected) {
            zutil.enableWiFi(MainActivity.this);
            zutil.gotoWiFiSetting(MainActivity.this);
        }
    }

    private void initUI() {
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        buttonWiFiStatus = (Button) findViewById(R.id.buttonWiFiStatus);
        buttonEnableWiFi = (Button) findViewById(R.id.buttonEnableWiFi);
        buttonWiFiSetting = (Button) findViewById(R.id.buttonWiFiSetting);
        buttonConnectWiFi = (Button) findViewById(R.id.buttonConnectWiFi);
        buttonGetNTPTime = (Button) findViewById(R.id.buttonGetNTPTime);
        buttonUpdateApp = (Button) findViewById(R.id.buttonUpdateApp);
        buttonPreference = (Button) findViewById(R.id.buttonPreference);

        editTextSSID = (EditText) findViewById(R.id.editTextSSID);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextSSID.setText(preference.getSSID());
        editTextPassword.setText(preference.getPassword());

        textViewBatteryCapacity= (TextView) findViewById(R.id.textViewBatteryCapacity);
        textViewBatteryCapacity.setText("Battery Capacity:" + zutil.getBatteryCapacity(MainActivity.this));

        textViewNTPTime = (TextView) findViewById(R.id.textViewNTPTime);
    }

    private void setupListener() {
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
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
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            currentTime = queryNTPTime("pool.ntp.org");
                            Log.d(TAG, "Current Time: " + currentTime.toDateString());
                        } catch (IOException e) {
                            Log.e(TAG, "Fail to get time from NTP server");
                            return;
                        }
                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();
                try {
                    thread.join();
                    textViewNTPTime.setText("Current Time: " + currentTime.toDateString());
                    Toast.makeText(getApplicationContext(), "Current Time: " + currentTime.toDateString(), Toast.LENGTH_SHORT).show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
