package com.github.ovictorpinto.verdinho.util;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.github.ovictorpinto.verdinho.BuildConfig;

/**
 * Created by victor on 25/08/15.
 */
public class LogHelper {
    
    public static void log(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }
    
    public static void log(Throwable e) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace();
        } else {
            Crashlytics.logException(e);
        }
    }
    
}
