package com.wecareapp.android;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.parse.Parse;
import com.parse.twitter.ParseTwitterUtils;


public class BaseApplication extends Application {
    private static BaseApplication mInstance;

    //App credentials
    private static final String APPLICATION_ID = "80504";
    private static final String AUTH_KEY = "Bbz7QX9mYpXNMCs";
    private static final String AUTH_SECRET = "h45FjHV7hd-bwQh";
    private static final String ACCOUNT_KEY = "xNhygS2PyVg162pxxyWc";

    public BaseApplication() {
    }

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

//          MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/

        mInstance = this;
        Stetho.initializeWithDefaults(this);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                // if desired
                .clientKey(getString(R.string.back4app_client_key))
                .server("https://parseapi.back4app.com/")
                .build()
        );

        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));

        FirebaseApp.initializeApp(this);
//        Sherlock.init(this); //Initializing Sherlock


    }
}
