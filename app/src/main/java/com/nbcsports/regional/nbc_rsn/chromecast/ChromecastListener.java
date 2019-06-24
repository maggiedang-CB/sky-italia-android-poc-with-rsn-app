package com.nbcsports.regional.nbc_rsn.chromecast;

import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChromecastListener implements SessionManagerListener<CastSession> {
    private final ChromecastHelper chromecastHelper;
    private boolean isStartStatePlay = true;

    public ChromecastListener(ChromecastHelper chromecastHelper) {
        this.chromecastHelper = chromecastHelper;
    }

    @Override
    public void onSessionStarting(CastSession castSession) {
        isStartStatePlay = chromecastHelper.isLocalPlaying(chromecastHelper.persistentPlayer);
        chromecastHelper.pauseLocalPlayback();
    }

    @Override
    public void onSessionStarted(CastSession castSession, String s) {
        if (chromecastHelper != null
                && chromecastHelper.persistentPlayer != null
                && chromecastHelper.persistentPlayer.getMediaSource() != null){
            MediaSource cloneMediaSource = new MediaSource(chromecastHelper.persistentPlayer.getMediaSource());
            // If media source has asset, it means the media source is not vod
            // Then the media source's stream url of PersistentPlayer should be replace by
            // the NBC tokenlized url.
            // So need to change it back to the original AndroidStreamUrl
            if (chromecastHelper.persistentPlayer.getMediaSource().getAsset() != null
                    && chromecastHelper.persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl() != null
                    && !chromecastHelper.persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl().isEmpty()){
                cloneMediaSource.setStreamUrl(chromecastHelper.persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl());
            }
            chromecastHelper.playOnChromecast(castSession, cloneMediaSource, isStartStatePlay, new ChromecastAuthorizationListener() {
                @Override
                public void onAuthorizationSuccess(@NotNull Auth auth) {
                    // Load video
                    chromecastHelper.loadVideo(auth, castSession.getRemoteMediaClient(), cloneMediaSource, isStartStatePlay);
                    chromecastHelper.initClosedCaptions(castSession);
                }

                @Override
                public void onAuthorizationFailure(@Nullable Throwable e) {
                    if (cloneMediaSource != null
                            && cloneMediaSource.getAsset() != null
                            && cloneMediaSource.getLive()
                            && cloneMediaSource.getAsset().isFree()){
                        chromecastHelper.loadVideo(null, castSession.getRemoteMediaClient(), cloneMediaSource, isStartStatePlay);
                        chromecastHelper.initClosedCaptions(castSession);
                    } else {
                        NotificationsManagerKt.INSTANCE.showChromecastAssetError();
                    }
                }
            });
            // once connected dismiss the dialog
            chromecastHelper.dismissBottomDialog();
        } else {
            NotificationsManagerKt.INSTANCE.showChromecastAssetError();
        }
        // reset start state
        isStartStatePlay = true;
    }

    @Override
    public void onSessionStartFailed(CastSession castSession, int i) {
        // Show chromecast failed notification banner
        NotificationsManagerKt.INSTANCE.showChromecastFailed(CastStatusCodes.getStatusCodeString(i));
    }

    @Override
    public void onSessionEnding(CastSession castSession) {
        chromecastHelper.onSessionEnding(castSession);
    }

    @Override
    public void onSessionEnded(CastSession castSession, int i) {
        chromecastHelper.onSessionEnded();
        // once disconnected dismiss the dialog
        chromecastHelper.dismissBottomDialog();
        // close custom message channel
        chromecastHelper.chromecastClosedCaptions.closeCustomMessageChannel();
        // Check if error code is equal to 0
        // 1. If so, show chromecast ended notification banner
        // 2. Otherwise, show chromecast error notification banner
        if (i == 0){
            NotificationsManagerKt.INSTANCE.showChromecastEnded();
        } else {
            NotificationsManagerKt.INSTANCE.showChromecastError(CastStatusCodes.getStatusCodeString(i));
        }
    }

    @Override
    public void onSessionResuming(CastSession castSession, String s) {
        isStartStatePlay = chromecastHelper.isLocalPlaying(chromecastHelper.persistentPlayer);
        chromecastHelper.pauseLocalPlayback();
    }

    @Override
    public void onSessionResumed(CastSession castSession, boolean b) {
        if (chromecastHelper != null
                && chromecastHelper.persistentPlayer != null
                && chromecastHelper.persistentPlayer.getMediaSource() != null){
            MediaSource cloneMediaSource = new MediaSource(chromecastHelper.persistentPlayer.getMediaSource());
            // If media source has asset, it means the media source is not vod
            // Then the media source's stream url of PersistentPlayer should be replace by
            // the NBC tokenlized url.
            // So need to change it back to the original AndroidStreamUrl
            if (chromecastHelper.persistentPlayer.getMediaSource().getAsset() != null
                    && chromecastHelper.persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl() != null
                    && !chromecastHelper.persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl().isEmpty()){
                cloneMediaSource.setStreamUrl(chromecastHelper.persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl());
            }
            chromecastHelper.playOnChromecast(castSession, cloneMediaSource, isStartStatePlay, new ChromecastAuthorizationListener() {
                @Override
                public void onAuthorizationSuccess(@NotNull Auth auth) {
                    // Load video
                    chromecastHelper.loadVideo(auth, castSession.getRemoteMediaClient(), cloneMediaSource, isStartStatePlay);
                    chromecastHelper.initClosedCaptions(castSession);
                }

                @Override
                public void onAuthorizationFailure(@Nullable Throwable e) {
                    if (cloneMediaSource != null
                            && cloneMediaSource.getAsset() != null
                            && cloneMediaSource.getLive()
                            && cloneMediaSource.getAsset().isFree()){
                        chromecastHelper.loadVideo(null, castSession.getRemoteMediaClient(), cloneMediaSource, isStartStatePlay);
                        chromecastHelper.initClosedCaptions(castSession);
                    } else {
                        NotificationsManagerKt.INSTANCE.showChromecastAssetError();
                    }
                }
            });
            // once connected dismiss the dialog
            chromecastHelper.dismissBottomDialog();
        } else {
            NotificationsManagerKt.INSTANCE.showChromecastAssetError();
        }
        // reset start state
        isStartStatePlay = true;
    }

    @Override
    public void onSessionResumeFailed(CastSession castSession, int i) {
        // Show chromecast failed notification banner
        NotificationsManagerKt.INSTANCE.showChromecastFailed(CastStatusCodes.getStatusCodeString(i));
    }

    @Override
    public void onSessionSuspended(CastSession castSession, int i) {
        // 1. Show chromecast failed notification banner with CAUSE_SERVICE_DISCONNECTED
        // 2. Show chromecast failed notification banner with CAUSE_NETWORK_LOST
        // Base on GoogleApiClient.ConnectionCallbacks.CAUSE_*.
        if (i == 1){
            NotificationsManagerKt.INSTANCE.showChromecastFailed("CAUSE_SERVICE_DISCONNECTED");
        } else if (i == 2){
            NotificationsManagerKt.INSTANCE.showChromecastFailed("CAUSE_NETWORK_LOST");
        }
    }
}
