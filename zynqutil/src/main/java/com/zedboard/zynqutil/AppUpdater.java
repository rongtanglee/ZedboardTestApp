package com.zedboard.zynqutil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class AppUpdater extends AsyncTask<String, Integer, String> {
    private final static String TAG = "AppUpdater";
    private Context mContext;
    private boolean fileDownloaded = false;
    private boolean installAfterDownloaded = false;
    private boolean forceUpdate = false;
    private String filePath = null;
    private ProgressDialog dialog;

    public AppUpdater(Context mContext, boolean installAfterDownloaded, boolean forceUpdate) {
        this.mContext = mContext;
        this.installAfterDownloaded = installAfterDownloaded;
        this.forceUpdate = forceUpdate; //if true, force update APK no matter the version code
        filePath = Environment.getExternalStorageDirectory().getPath() + "/Download/zedboard-testapp.apk";
    }

    @Override
    protected String doInBackground(String... apkUrl) {
        try {
            URL url = new URL(apkUrl[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int resCode = conn.getResponseCode();

            int fileLength = conn.getContentLength();

            Log.d(TAG, "fileLength=" + fileLength + ",resCode=" + resCode);
            Log.d(TAG, "Downloading file from " + url.toString() + " to " + filePath);

            InputStream inputStream = new BufferedInputStream(url.openStream());
            OutputStream outputStream = new FileOutputStream(filePath);

            byte[] data = new byte[1024];
            int total = 0;
            int count;
            while ((count = inputStream.read(data)) != -1) {
                total += count;
                // publishing the progress....
                publishProgress((int) (total * 100 / fileLength));
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
            fileDownloaded = true;
        } catch (IOException e) {
            Log.e(TAG, "Fail to download apk file");
            fileDownloaded = false;
        } finally {
            dialog.dismiss();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(mContext);
        dialog.show(mContext, "Update App", "Downloading APK", true, true);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (fileDownloaded == false) {
            return;
        }

        if (forceUpdate) {
            Log.d(TAG, "Force update the APK");
            installAPK();
            return;
        }

        if (installAfterDownloaded) {
            if (getCurrentVersionCode() < getAPKVersionCode()) {
                Log.d(TAG, "APK version is newer than current version, updating it...");
                installAPK();
            } else {
                Log.d(TAG, "APK version is older than or same as current version, abort updating");
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        Log.d(TAG, "progress= " + values[0]);
        if (values[0] == 100) {
            dialog.dismiss();
        }
    }

    public int getAPKVersionCode() {
        if (!fileDownloaded) {
            Log.d(TAG, "File not downloaded");
            return 0;
        }

        PackageManager pm = mContext.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(filePath, 0);
        Log.d(TAG, "VersionCode:" + info.versionCode + ", VersionName:" + info.versionName);
        return info.versionCode;
    }

    private int getCurrentVersionCode() {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "versionCode not found");
        }
        return 0;
    }

    public void installAPK() {
        if (!fileDownloaded) {
            Log.d(TAG, "File not downloaded");
            return;
        }

        Uri fileUri = Uri.fromFile(new File(filePath));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

        Log.d(TAG, "Start install the APK");
    }

    public boolean isFileDownloaded() {
        return fileDownloaded;
    }
}



