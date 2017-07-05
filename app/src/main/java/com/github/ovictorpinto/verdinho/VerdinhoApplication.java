package com.github.ovictorpinto.verdinho;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.ovictorpinto.verdinho.util.RatingHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

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
    
        //https://firebase.google.com/support/guides/disable-analytics?hl=pt-br
        //só loga o firebase em produção
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
    }
}
