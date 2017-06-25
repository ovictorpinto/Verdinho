package com.github.ovictorpinto.verdinho;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.ovictorpinto.verdinho.util.RatingHelper;

import io.fabric.sdk.android.Fabric;

/**
 * Created by victorpinto on 17/11/15.
 */
public class VerdinhoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        RatingHelper ratingHelper = new RatingHelper(this);
        ratingHelper.count();
    }
}
