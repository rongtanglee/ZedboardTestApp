package com.ron.zedboardtestapp;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.zedboard.zynqutil.ZedboardUtil;

import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PLContentProvider {

    private final static String TAG = "PLContentProvider";
    private final static int CHECK_TIMEOUT = 15;  //15 seconds
    private Context mContext;


    private boolean timeout = false;
    private String currentContentFile = null;

    private static PLContentProvider instance;

    //PLContentProvider is singleton class
    private PLContentProvider() {
    }

    public static synchronized PLContentProvider getInstance() {
        if (instance == null) {
            instance = new PLContentProvider();
        }
        return instance;
    }

    public void start2DCapture(Context context) {
        Log.d(TAG, "Start 2D Capture");
        mContext = context;
        new CaptureTask().execute(Boolean.valueOf(false));
    }

    public void start3DCapture(Context context) {
        Log.d(TAG, "Start 3D Capture");
        mContext = context;
        new CaptureTask().execute(Boolean.valueOf(true));
    }

    private class CaptureTask extends AsyncTask<Boolean, Integer, String> {
        @Override
        protected void onPreExecute() {
            currentContentFile = null;
            timeout = false;
        }

        @Override
        protected String doInBackground(Boolean... booleen) {
            boolean is3DCapture = booleen[0].booleanValue();

            // start 2D/3D capture
            int result = capture(is3DCapture);
            if (result < 0) {
                Log.d(TAG, "Fail to capture");
                return null;
            }

            // check data ready every seconds, timeout after 10 seconds
            int checkTimerCount = 0;
            while (true) {
                boolean dataReady = isDataReady();
                if (dataReady)
                    break;

                checkTimerCount ++;
                if (checkTimerCount > CHECK_TIMEOUT) {
                    Log.d(TAG, "Timer timeout and no data ready");
                    timeout = true;
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Fail to sleep");
                }
            }

            if (timeout) {
                return null;
            }

            // Get current time as postfix of file name
            Date d;
            try {
                //Use current time as file name postfix
                TimeStamp currentTime = ZedboardUtil.queryNTPTime("pool.ntp.org");
                Log.d(TAG, "Current Time: " + currentTime.toDateString());
                d = currentTime.getDate();
            } catch (IOException e) {
                Log.e(TAG, "Fail to get time from NTP server");
                d = new Date();  //get current date/time
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String postfix = dateFormat.format(d);

            return postfix;
        }

        @Override
        protected void onPostExecute(String s) {
            // Run in UI thread
            Activity activity = (Activity) mContext;
            Button buttonUpload = (Button) activity.findViewById(R.id.buttonUpload);
            Button button2DCapture = (Button) activity.findViewById(R.id.button2DCapture);
            Button button3DCapture = (Button) activity.findViewById(R.id.button3DCapture);

            if (s != null) {
                Log.d(TAG, "Get content file path");
                currentContentFile = getFilePath(s);

                if (currentContentFile != null && !currentContentFile.isEmpty()) {
                    Log.d(TAG, "Enable Upload button");

                    buttonUpload.setEnabled(true);
                    button2DCapture.setEnabled(true);
                    button3DCapture.setEnabled(true);
                } else {
                    Log.e(TAG, "Data is not available");
                    Toast.makeText(mContext, "Data is not available", Toast.LENGTH_SHORT).show();

                    buttonUpload.setEnabled(false);
                    button2DCapture.setEnabled(true);
                    button3DCapture.setEnabled(true);
                }
            } else {
                Log.e(TAG, "Timer timeout");
                Toast.makeText(mContext, "Timer timeout", Toast.LENGTH_SHORT).show();

                buttonUpload.setEnabled(false);
                button2DCapture.setEnabled(true);
                button3DCapture.setEnabled(true);
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    public String getCurrentContentFile() {
        return currentContentFile;
    }

    public native static int capture(boolean is3DCapture);

    public native static String getFilePath(String postfix);

    public native static long readRegister(long address);

    public native static int writeRegister(long address, long value);

    public native boolean isDataReady();

    static {
        System.loadLibrary("pl-jni");
    }
}
