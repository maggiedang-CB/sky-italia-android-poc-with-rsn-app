package com.nbcsports.regional.nbc_rsn;

import android.content.Context;

import com.clearbridge.bottom_notification_banner.BottomNotificationBanner;
import com.clearbridge.bottom_notification_banner.BottomNotificationData;
import com.nbcsports.regional.nbc_rsn.common.BaseView;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.utils.TotalCast;

import java.util.HashMap;

public interface MainContract {

    interface View extends BaseView<Presenter> {

        MainContract.Presenter getMainPresenter();

        void hideFab();

        void showFab();

        void showFabWithAnimation(long startDelay, boolean enableCustomOnTouch);

        void clearFabAnimations();

        void fadeInAndFadeOurFab(float percent);

        void disableFabTouch(boolean disableTouch);

        PersistentPlayer getPersistentPlayer();

        void updateTeamLogos(HashMap<Integer, String> map);

        boolean getActivityVisibility();

        BottomNotificationBanner getBottomNotificationBanner();
    }

    interface Presenter {

        void configGet();

        void configGetWithDelay(long delay, Throwable e, Context context);

        void configAddListener(ConfigListener configListener);

        void configRemoveListener(ConfigListener configListener);

        void localizationsGet(String localizationsUrl);

        void geolocationGet(String geolocationUrl);

        void affiliatesGet();

        Config getLastKnownConfig();

        TotalCast getTotalCast();

        void fadeInAndFadeOurFab(float percent, android.view.View... views);

        void showBottomNotification(BottomNotificationData bottomNotificationData, int duration);

        void hideBottomNotification();
    }
}
