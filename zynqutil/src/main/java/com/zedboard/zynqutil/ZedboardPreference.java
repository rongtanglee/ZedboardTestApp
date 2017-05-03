package com.zedboard.zynqutil;

import android.content.SharedPreferences;
import android.content.Context;

/**
 * Created by ron on 2017/4/27.
 */

public class ZedboardPreference {
    private static final String prefName = "zedboard-preference";

    private static final String ssidField = "SSID";
    private static final String passwordField = "Password";
    private static final String accountIDField = "AccountID";
    private static final String poolIDField = "PoolID";
    private static final String poolRegionField = "PoolRegion";
    private static final String unauthRoleARNField = "UnauthRoleARN";
    private static final String authRoleARNField = "AuthRoleARN";
    private static final String bucketNameField = "BucketName";
    private static final String bucketRegionField = "BucketRegion";

    public static final String defSSID = "RonHome";
    public static final String defPassword = "29972665";
    public static final String defAccountID = "924423895306";
    public static final String defPoolID = "ap-northeast-1:6b636872-3a36-4dca-bc9e-1faf9c118511";
    public static final String defPoolRegion = "ap-northeast-1";
    public static final String defUnauthRoleARN = "arn:aws:iam::924423895306:role/Cognito_ZedboardTestAppUnauth_Role";
    public static final String defAuthRoleARN = "arn:aws:iam::924423895306:role/Cognito_ZedboardTestAppAuth_Role";
    public static final String defBucketName = "zedboard-testapp";
    public static final String defBucketRegion = "ap-northeast-1";

    private SharedPreferences sharedPreferences;
    private Context mContext;

    public ZedboardPreference(Context context) {
        mContext = context;
        this.sharedPreferences = mContext.getSharedPreferences(prefName, 0);
    }

    public void setSSID(String ssid) {
        sharedPreferences.edit().putString(ssidField, ssid).commit();
    }

    public String getSSID() {
        return sharedPreferences.getString(ssidField, defSSID);
    }

    public void setPassword(String password) {
        sharedPreferences.edit().putString(passwordField, password).commit();;
    }

    public String getPassword() {
        return sharedPreferences.getString(passwordField, defPassword);
    }

    public void setAccountID(String accountID) {
        sharedPreferences.edit().putString(accountIDField, accountID).commit();;
    }

    public String getAccountID() {
        return sharedPreferences.getString(accountIDField, defAccountID);
    }

    public void setPoolID(String poolID) {
        sharedPreferences.edit().putString(poolIDField, poolID).commit();;
    }

    public String getPoolID() {
        return sharedPreferences.getString(poolIDField, defPoolID);
    }

    public void setPoolRegion(String region) {
        sharedPreferences.edit().putString(poolRegionField, region).commit();;
    }

    public String getPoolRegion() {
        return sharedPreferences.getString(poolRegionField, defPoolRegion);
    }

    public void setUnauthRoleARN(String arn) {
        sharedPreferences.edit().putString(unauthRoleARNField, arn).commit();;
    }

    public String getUnauthRoleARN() {
        return sharedPreferences.getString(unauthRoleARNField, defUnauthRoleARN);
    }

    public void setAuthRoleARN(String arn) {
        sharedPreferences.edit().putString(authRoleARNField, arn).commit();;
    }

    public String getAuthRoleARN() {
        return sharedPreferences.getString(authRoleARNField, defAuthRoleARN);
    }

    public void setBucketName(String name) {
        sharedPreferences.edit().putString(bucketNameField, name).commit();;
    }

    public String getBucketName() {
        return sharedPreferences.getString(bucketNameField, defBucketName);
    }

    public void setBucketRegion(String region) {
        sharedPreferences.edit().putString(bucketRegionField, region).commit();;
    }

    public String getBucketRegion() {
        return sharedPreferences.getString(bucketRegionField, defBucketRegion);
    }

}
