package com.nbcsports.regional.nbc_rsn.analytics.kochava;

import com.kochava.base.Tracker;
import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.common.Asset;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;

import org.apache.commons.lang3.StringUtils;

import timber.log.Timber;

public class KochavaAnalytic implements KochavaContract.Presenter {

    private static final String kOPEN                 = "app_launch";
    private static final String kVOD_START            = "VODStart";
    private static final String kVOD_DURATION         = "VODDuration";
    private static final String kLIVE_STREAM_START    = "LiveStreamStart";
    private static final String kLIVE_STREAM_DURATION = "LiveStreamDuration";
    private static final String kAUTH_SUCCESS         = "AuthSuccess";

    public KochavaAnalytic(KochavaContract.View view) {
        view.setKochava(this);
        String appId = BuildConfig.KOCHAVA_APP_ID;
        if (!appId.isEmpty()) {
            Tracker.configure(new Tracker.Configuration(RsnApplication.getInstance())
                .setAppGuid(appId)
                .setLogLevel(Tracker.LOG_LEVEL_INFO)
            );
        }
    }

    private void kochavaEvent(String eventName, String eventData) {
        Timber.e("Kochava player: eventName: %s, eventData: %s", eventName, eventData);
        Tracker.sendEvent(eventName, eventData);
    }

    @Override
    public void trackAppLaunch() {
        this.kochavaEvent(kOPEN,"");
    }

    @Override
    public void trackAuthSuccess(String provider) {
        this.kochavaEvent(kAUTH_SUCCESS, provider);
    }

    private void trackVODStart(String title, String channel) {
        this.kochavaEvent(kVOD_START, title + "|" + channel);
    }

    private void trackLiveStreamStart(String title, String channel) {
        this.kochavaEvent(kLIVE_STREAM_START, title + "|" + channel);
    }

    @Override
    public void sendAppropriateVideoStartEvent(MediaSource mediaSource) {
        if (mediaSource != null){
            if (mediaSource.getLive() && mediaSource.getAsset() != null){
                trackLiveStreamStart(mediaSource.getAsset().getTitle(),
                        mediaSource.getAsset().getChannel());
            } else {
                String channel = getChannel(mediaSource);
                trackVODStart(mediaSource.getTitle(), channel);
            }
        }
    }

    @Override
    public void sendAppropriateVideoDurationEvent(MediaSource mediaSource, long secondsSpentOnWatching) {
        if (secondsSpentOnWatching <= 0 || mediaSource == null) return;

        String event;
        Asset asset = mediaSource.getAsset();
        if (asset != null && asset.isLive()) {
            event = kLIVE_STREAM_DURATION;
        } else {
            event = kVOD_DURATION;
        }

        String sportName = "";
        String channel   = "";
        String title     = "";

        if (asset != null){
            sportName = asset.getSportName();
            channel   = asset.getChannel();
            title     = asset.getTitle();
        } else {
            channel = getChannel(mediaSource);
            title   = mediaSource.getTitle();
        }

        String minutes = Long.toString(secondsSpentOnWatching / 60);
        String seconds = String.format("%02d", secondsSpentOnWatching % 60);
        this.kochavaEvent(event, sportName + "|" + title + "|" + channel + "|" + minutes + ":" + seconds);
    }

    private String getChannel(MediaSource mediaSource) {
        String channel = "";
        if (mediaSource.getDeeplink() != null
                && mediaSource.getDeeplink().getTeam() != null
                && StringUtils.isNotEmpty(mediaSource.getDeeplink().getTeam().getRequestorId())){
            channel = mediaSource.getDeeplink().getTeam().getRequestorId();
        }
        return channel;
    }

    public static class Injection {
        public static KochavaContract.Presenter provideKochava(KochavaContract.View view) {
            try {
                KochavaContract.View contractView = view;
                return contractView.getKochava();
            } catch (ClassCastException castException) {
                return null;
            }
        }
    }
}
