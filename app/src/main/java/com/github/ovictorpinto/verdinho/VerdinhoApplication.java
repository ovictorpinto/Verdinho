package com.github.ovictorpinto.verdinho;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.github.ovictorpinto.verdinho.util.RatingHelper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;

/**
 * Created by victorpinto on 17/11/15.
 */
public class VerdinhoApplication extends MultiDexApplication {
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
        
        FirebaseMessaging.getInstance().subscribeToTopic(BuildConfig.FLAVOR + "_all");
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().subscribeToTopic(BuildConfig.FLAVOR + "_all_debug");
        }
    }
}
