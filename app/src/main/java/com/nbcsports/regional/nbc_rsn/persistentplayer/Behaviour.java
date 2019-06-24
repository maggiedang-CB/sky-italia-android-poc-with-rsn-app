package com.nbcsports.regional.nbc_rsn.persistentplayer;

import android.content.Context;
import android.widget.ImageButton;

import java.io.IOException;

import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationFragment;
import com.nbcsports.regional.nbc_rsn.chromecast.IChromecastHelper;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.settings.media_settings.MediaSettingsPresenter;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.ConnectionUtils;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;

import static android.view.View.GONE;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.LANDSCAPE;

public class Behaviour {

    public static void loadComplete(PersistentPlayer persistentPlayer){
        persistentPlayer.loadCompleted = true;
        if (persistentPlayer.currentView != null){
            persistentPlayer.currentView.showProgress(false);
        }
        if (persistentPlayer.is247()) {
            persistentPlayer.setOrientationLandscape();
        } else if (NavigationManager.getInstance().getCurrentFragment() instanceof StreamAuthenticationFragment){
            // Do nothing so far
        } else {
            persistentPlayer.setOrientationSensor();
        }

        // if we should "continue where we left off last time"
        if (persistentPlayer.isShouldContinueScrubber()) {
            persistentPlayer.continueScrubber();
            persistentPlayer.pause();
        }
    }

    public static void loadCancel(PersistentPlayer persistentPlayer){
        NotificationsManagerKt.INSTANCE.showPlaybackError("load cancelled");
        if (persistentPlayer.getCurrentView() != null){
            persistentPlayer.getCurrentView().showProgress(false);
        }
    }

    public static void loadError(PersistentPlayer persistentPlayer, IOException error,
                                 Auth auth,
                                 Config config){

        persistentPlayer.loadCompleted = false;

        // hide progress, when authn or playback reaches this state, it's considered to be in its
        // final state so progress can be hidden.
        if (persistentPlayer.currentView != null){
            persistentPlayer.currentView.showProgress(false);
        }

        boolean isTempPassExpiredShowing = persistentPlayer.mediumView != null
                && persistentPlayer.mediumView.getPersistentPlayerView().isTempPassExpiredShowing();

        if (Error.is403(error) || Error.is410(error)) {
            // if error is 403 or 410 show sign in overlay so that user can start authn
            if (persistentPlayer.type == LANDSCAPE) {
                // if still getting 403 when an AuthZ token is returned
                // that means the user is not entitled
                if (Error.is403(error)
                        && isAuthenticated(auth, config)
                        && ! isTempPassExpiredShowing ) {
                    NotificationsManagerKt.INSTANCE.showAuthZError(auth);
                }
                persistentPlayer.landscapeView.showSignIn(error);

            } else if ( !isTempPassExpiredShowing && ! persistentPlayer.mediumView.isSignedIn()) {

                ImageButton spb = persistentPlayer.mediumView.getPersistentPlayerView().getStandalonePlayButton();
                if (spb != null && spb.getVisibility() == GONE) {
                    persistentPlayer.mediumView.showSignIn(error);
                }

            } else if ( ! isTempPassExpiredShowing
                    && !persistentPlayer.mediumView.isAuthorized() ) {
                // if there is 4xx error but there is an authz token returned, that means
                // there is an error with upstream, such as stream is geo-blocked.
                // Or the user is not entitled
                NotificationsManagerKt.INSTANCE.showAuthZError(auth);

            } else if ( ! isTempPassExpiredShowing
                    && persistentPlayer.mediumView.isSignedIn()) {
                // if there is 4xx error but user is able to signed in and is authorized, that means
                // there is a playback error.
                NotificationsManagerKt.INSTANCE.showPlaybackError(error.getMessage());

            }
        } else {
            // if playback has started successfully that means this error was from a previous attempt, ignore it
            if ( persistentPlayer.isPlaying() ) { return; }
            // otherwise show the error in banner
            NotificationsManagerKt.INSTANCE.showPlaybackError(error.getMessage());
        }

        // if not landscape return back to portrait
        if (persistentPlayer.type != LANDSCAPE) {
            persistentPlayer.resetOrientation();
        }

        persistentPlayer.release();
    }

    public static void autoPlay(PersistentPlayer persistentPlayer, PlayerEngine.Interface playerEngine) {
        boolean ableToPlay = playerEngine.isReady();
        IChromecastHelper chromecastHelper = persistentPlayer.getChromecastHelper();

        if (ableToPlay) {
            persistentPlayer.setPlayWhenReady(persistentPlayer.isPlayWhenReady());

            if (persistentPlayer.getCurrentView() instanceof Medium){
                ((Medium) persistentPlayer.getCurrentView()).hideAllControls();
            }
            if (persistentPlayer.getCurrentView() != null){
                persistentPlayer.getCurrentView().showProgress(false);
                persistentPlayer.getCurrentView().showPlayerControlView();
            }
        }

        if (chromecastHelper != null && chromecastHelper.isStateConnected()) {
            chromecastHelper.handleAutoPlay(chromecastHelper.getCurrentCastSession());
        }
        persistentPlayer.log(persistentPlayer, "autoPlay() ableToPlay: %s", ableToPlay);
    }

    /**
     * Check if video auto play is allowed
     *
     * @param persistentPlayer contains previous player info
     * @param newMediaSource contains new media source
     * @return true if auto play is allowed or Mini player is playing the same media source
     *         false otherwise
     */
    public static boolean isAutoPlayAllow(PersistentPlayer persistentPlayer, MediaSource newMediaSource) {
        boolean isAllow = false;
        if (PreferenceUtils.INSTANCE.getBoolean(MediaSettingsPresenter.AUTO_PLAY_LIVE_GAMES, true)){
            if (newMediaSource != null){
                if (newMediaSource.getLive()){ isAllow = true; }
            } else { isAllow = true; }
        }
        if (PreferenceUtils.INSTANCE.getBoolean(MediaSettingsPresenter.AUTO_PLAY_VIDEOS, true)){
            if (newMediaSource != null){
                if (!newMediaSource.getLive()){ isAllow = true; }
            } else { isAllow = true; }
        }
        persistentPlayer.log(persistentPlayer, "medium.show persistent player type: "+persistentPlayer.getType()+" persistent player is live: "+persistentPlayer.isLive());
        if (persistentPlayer.getType() == PlayerConstants.Type.MINI){
            isAllow = true;
        }
        return isAllow;
    }

    public static boolean isCellularDataAllow(MediaSource mediaSource, Context context) {
        boolean isAllow = true;
        if (!ConnectionUtils.isConnectedWifi(context)) {
            if ((mediaSource.getLive() && !PreferenceUtils.INSTANCE.getBoolean(MediaSettingsPresenter.CELLULAR_DATA_FOR_LIVE_STREAM, true)) ||
                    (!mediaSource.getLive() && !PreferenceUtils.INSTANCE.getBoolean(MediaSettingsPresenter.CELLULAR_DATA_FOR_MEDIA_STREAM, true))) {
                isAllow = false;
            }
        }
        return isAllow;
    }

    /**
     * If authz token is present or there is a temp pass
     * user is considered authenticated and can watch feeds.
     */
    private static boolean isAuthenticated(Auth auth, Config config){
        if (auth == null) { return false; }
        if (config == null) { return false; }
        return auth.getAuthZToken() != null || auth.isTempPassAuthN(config);
    }
}
