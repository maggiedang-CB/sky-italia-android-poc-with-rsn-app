package com.nbcsports.regional.nbc_rsn.urban_airship;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.push.notifications.DefaultNotificationFactory;

/***
 * See createAirshipConfigOptions(Context context) for configuring the Urban Airship settings.
 *  Application is configured to use this class in AndroidManifest.xml
 */
public class UrbanAirshipConfigurator extends Autopilot {

    private static final String DEV_APP_KEY = "jlaQQphiQXmZZEtDs6gCog";
    private static final String DEV_APP_SECRET = "qX5O9i6KReuWhQ6JIe7_7g";
    private static final String SENDER_ID = "979353246654";
    private static final String PROD_APP_KEY = "YGg8lpG2SeqOe0sgP8Az0A";
    private static final String PROD_APP_SECRET = "x4R28EDWQxqssP1K44KWRw";

    @Override
    public void onAirshipReady(@NonNull UAirship airship) {
        super.onAirshipReady(airship);

        if (RsnApplication.getInstance() != null){
            setupNotification(RsnApplication.getInstance());
        }
    }

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(@NonNull Context context) {
        //Must be declared as a variable
        AirshipConfigOptions options = new AirshipConfigOptions.Builder()
                .setDevelopmentAppKey(DEV_APP_KEY)
                .setDevelopmentAppSecret(DEV_APP_SECRET)
                .setDevelopmentLogLevel(Log.DEBUG)
                .setProductionAppKey(PROD_APP_KEY)
                .setProductionAppSecret(PROD_APP_SECRET)
                //.setInProduction(!BuildConfig.DEBUG)
                .setInProduction(BuildConfig.IS_PROD)
                .setFcmSenderId(SENDER_ID)
                .build();
        return options;
    }

    public static void setupNotification(Application application){

        DefaultNotificationFactory defaultNotificationFactory = new DefaultNotificationFactory(application);

        // https://stackoverflow.com/a/45883564
        defaultNotificationFactory.setSmallIconId(R.drawable.ic_notification);

        defaultNotificationFactory.setColor(NotificationCompat.COLOR_DEFAULT);
        UAirship.shared().getPushManager().setNotificationFactory(defaultNotificationFactory);

    }
}
