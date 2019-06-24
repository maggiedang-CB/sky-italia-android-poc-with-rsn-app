package com.nbcsports.regional.nbc_rsn;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.comscore.Analytics;
import com.comscore.PublisherConfiguration;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.gu.toolargetool.TooLargeTool;
import com.nbcsports.regional.nbc_rsn.urban_airship.UrbanAirshipConfigurator;
import com.nbcsports.regional.nbc_rsn.utils.NBCSystemUtils;
import com.squareup.leakcanary.LeakCanary;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterConfig;

import java.io.IOException;

/**
 * An application class representing this application.
 */
public class RsnApplication extends Application {

    public static final String NAME_SHARED_PREFERENCE = "com-nbcsports-regional-nbc_rsn--shared-preferences-file-name";
    public static final String SP_APP_WAS_OPENED_BEFORE = "com-nbcsports-regional-nbc_rsn--app-was-opened-before-shared-preferences-name";

    private boolean appWasOpenedBefore;

    static private RsnApplication singleton;

    static public RsnApplication getInstance() {
        return singleton;
    }

    /**
     * Checks if it is the first launch of this app.
     * @return
     */
    public boolean isFirstLaunch() {
        return !appWasOpenedBefore;
    }

    private void initTwitterKit() {
        if (BuildConfig.DEBUG) {
            TwitterConfig config = new TwitterConfig.Builder(this)
                    .logger(new DefaultLogger(Log.DEBUG))
                    .debug(true)
                    .build();
            Twitter.initialize(config);
        } else {
            Twitter.initialize(this);
        }
    }

    /**
     * Init and start comscore analytics
     */
    private void initAndStartComScoreAnalytics() {
        // Init comscore
        PublisherConfiguration publisherConfiguration = new PublisherConfiguration.Builder()
                .publisherId(BuildConfig.COMSCORE_PUBLISHER_ID)
                .publisherSecret(BuildConfig.COMSCORE_PUBLISHER_SECRET)
                .build();
        Analytics.getConfiguration().addClient(publisherConfiguration);
        // Start comscore
        Analytics.start(this);
    }

    @Override
    public void onCreate() {

        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Fabric.with(this, new Crashlytics());

        initTwitterKit();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        singleton = this;

        // Check if it is the first launch of this app and remember that.
        SharedPreferences preferences = getSharedPreferences(RsnApplication.NAME_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        appWasOpenedBefore = preferences.getBoolean(RsnApplication.SP_APP_WAS_OPENED_BEFORE, false);
        preferences.edit().putBoolean(RsnApplication.SP_APP_WAS_OPENED_BEFORE, true).commit();

        // Transaction Too Large
        // https://medium.com/@mdmasudparvez/android-os-transactiontoolargeexception-on-nougat-solved-3b6e30597345
        // possible solution:
        // https://github.com/livefront/bridge
        // https://stackoverflow.com/questions/11451393/what-to-do-on-transactiontoolargeexception

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            // resolve RSNAPP-595 for crash when using fab menu on devices running Android 5 and 6
            TooLargeTool.startLogging(this);
        }

        UrbanAirshipConfigurator.setupNotification(this);

        /*
         * Sometimes app crashes because of 'thread interrupted'. Seems like its because we
         * disposes the observable before its task finishes.
         * This is the solution to silent the exception.
         * https://stackoverflow.com/a/43525858
         * https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
         * */
        Consumer<Throwable> errorHandler = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) {
                if (e instanceof UndeliverableException) {
                    e = e.getCause();
                }
                if ((e instanceof IOException)) {
                    // fine, irrelevant network problem or API that throws on cancellation
                    return;
                }
                if (e instanceof InterruptedException) {
                    // fine, some blocking code was interrupted by a dispose call
                    return;
                }

                Timber.w(e, "Undeliverable exception received, not sure what to do");
            }
        };
        RxJavaPlugins.setErrorHandler(errorHandler);

        // Check if google play service available
        int playServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        NBCSystemUtils.INSTANCE.setPLAY_SERVICES_AVAILABLE(playServicesAvailable == ConnectionResult.SUCCESS);

        // Init and start comscore analytics
        initAndStartComScoreAnalytics();
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
    }
}
