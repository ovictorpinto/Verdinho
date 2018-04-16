package com.github.ovictorpinto.verdinho;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.github.ovictorpinto.verdinho.util.RatingHelper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inlocomedia.android.ads.InLocoMedia;
import com.inlocomedia.android.ads.InLocoMediaOptions;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

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
        configFirebase();
        configInLocoMedia();
    
        initTwitter();
    
    }
    
    private void initTwitter() {
        TwitterConfig config = new TwitterConfig.Builder(this)//
                .logger(new DefaultLogger(BuildConfig.DEBUG? Log.DEBUG:Log.WARN))
                .twitterAuthConfig(new TwitterAuthConfig(BuildConfig.TWITTER_CONSUMER_KEYS, BuildConfig.TWITTER_CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }
    
    private void configInLocoMedia() {
        // In Loco Media SDK Init
        InLocoMediaOptions options = InLocoMediaOptions.getInstance(this);
    
        // The AppId you acquired in earlier steps
        options.setAdsKey(BuildConfig.IN_LOCO_APP_ID);
    
        // Verbose mode flag, if this is set as true InLocoMedia SDK will let you know about errors on the Logcat
        options.setLogEnabled(true);
    
        // Development Devices set here are only going to receive test ads
        options.setDevelopmentDevices("11C85D6058B4FC8C37A2CE56799AD5");
    
        InLocoMedia.init(this, options);
    }
    
    private void configFirebase() {
        //https://firebase.google.com/support/guides/disable-analytics?hl=pt-br
        //só loga o firebase em produção
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        
        FirebaseMessaging.getInstance().subscribeToTopic(BuildConfig.FLAVOR + "_all");
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().subscribeToTopic(BuildConfig.FLAVOR + "_all_debug");
        }
    }
}
