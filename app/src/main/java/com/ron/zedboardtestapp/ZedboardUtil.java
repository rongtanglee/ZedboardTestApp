package com.ron.zedboardtestapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Log;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static android.content.ContentValues.TAG;


public class ZedboardUtil {
    public boolean isWiFiConnected(final Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo != null && wifiInfo.isConnected();
    }

    public boolean isNetworkConnected(final Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo() != null && connMgr.getActiveNetworkInfo().isConnected();
    }

    public void enableWiFi(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }

    // Only Support WPA/WPA2
    public void connectWiFiAP(final Context context, String ssid, String key) {
        if (!isWiFiConnected(context)) {
            enableWiFi(context);
        }

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";
        wifiConfig.preSharedKey = "\"" + key + "\"";


        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiMgr.addNetwork(wifiConfig);

        List<WifiConfiguration> list = wifiMgr.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals(wifiConfig.SSID)) {
                wifiMgr.disconnect();
                wifiMgr.enableNetwork(i.networkId, true);
                wifiMgr.reconnect();
                break;
            }
        }
    }

    public void gotoWiFiSetting(final Context context) {
        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public int getBatteryCapacity(final Context context) {
        BatteryManager mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int capacity = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        return capacity;
    }

    public void registerBatteryReceiver(final Context context, BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        context.registerReceiver(receiver, intentFilter);
    }

    public static TimeStamp queryNTPTime(String ntpServerHostname) throws IOException {
        NTPUDPClient client = new NTPUDPClient();

        client.setDefaultTimeout(10000);

        TimeInfo info = null;
        try {
            client.open();

            InetAddress hostAddr = InetAddress.getByName(ntpServerHostname);
            Log.d(TAG, "Trying to get time from " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());

            info = client.getTime(hostAddr);
        } finally {
            client.close();
        }

        NtpV3Packet message = info.getMessage();
        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();

        return rcvNtpTime;
    }

}
