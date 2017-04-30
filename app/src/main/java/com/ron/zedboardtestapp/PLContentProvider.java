package com.ron.zedboardtestapp;



public class PLContentProvider {

    public PLContentProvider() {
    }

    public native static int capture();

    public native static String getFilePath(String postfix);

    static {
        System.loadLibrary("pl-jni");
    }
}
