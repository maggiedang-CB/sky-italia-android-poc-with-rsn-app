package com.nbcsports.regional.nbc_rsn.analytics.kochava;

import com.nbcsports.regional.nbc_rsn.common.MediaSource;


public interface KochavaContract {

    interface View {

        void setKochava(KochavaContract.Presenter presenter);

        KochavaContract.Presenter getKochava();
    }

    interface Presenter {

        void trackAppLaunch();

        void trackAuthSuccess(String mvpdName);

        void sendAppropriateVideoStartEvent(MediaSource mediaSource);

        void sendAppropriateVideoDurationEvent(MediaSource mediaSource, long secondsSpentOnWatching);
    }
}
