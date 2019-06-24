package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.content.Context;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.persistentplayer.Behaviour;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;
import com.nbcsports.regional.nbc_rsn.urban_airship.ErrorChecker;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

/**
 * This works like a Reload button on a regular player
 * - Appears when playback completes
 * - Appears in medium when mini player is in view
 * - Appears in medium when auto play is disabled on media settings
 */
public class OnStandAlonePlayClickListener implements View.OnClickListener {

    private final Context context;
    private final PersistentPlayer persistentPlayer;
    private Medium medium;

    public OnStandAlonePlayClickListener(PersistentPlayer persistentPlayer, Context context) {
        this.persistentPlayer = persistentPlayer;
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        // Check if medium or medium.getMediaSource and persistentPlayer
        // or persistentPlayer.getMediaSource are null
        // If so, then show playback error
        if ((medium == null || medium.getMediaSource() == null)
                && (persistentPlayer == null || persistentPlayer.getMediaSource() == null)){
            NotificationsManagerKt.INSTANCE.showPlaybackError("");
            return;
        }
        // Check whether medium is null to prevent crash
        // when clicking on StandAlonePlayButton
        // 1. If medium is null, then use the media source from persistentPlayer
        // 2. Otherwise, use media source from medium
        if (!Behaviour.isCellularDataAllow(
                medium == null? persistentPlayer.getMediaSource() : medium.getMediaSource(),
                context)) {
            NotificationsManagerKt.INSTANCE.showCellularDataNotAllowError();
        }

        if (medium != null){
            persistentPlayer.setMediumView(medium);
            persistentPlayer.setCurrentType(PlayerConstants.Type.MEDIUM);
        }

        if (persistentPlayer.isMiniShown()){
            persistentPlayer.showMini(false);
        }

        // Check if the stand alone play button is on medium's persistent player view
        // or on other persistent player views
        // then remove the previous progress bar observer from progress bar manager
        // and add a new one to it
        if (medium != null && persistentPlayer != null){
            // Remove all progress bar observers first
            PersistentPlayerProgressBarManager.removeAllObservers();
            // Start and add progress bar observer into
            // PersistentPlayerProgressBarManager
            medium.getPersistentPlayerView().startProgressBarObserver(persistentPlayer, medium);
        } else if (persistentPlayer != null && persistentPlayer.getCurrentView() != null){
            // Remove all progress bar observers first
            PersistentPlayerProgressBarManager.removeAllObservers();
            // Start and add progress bar observer into
            // PersistentPlayerProgressBarManager
            persistentPlayer.getCurrentView().getPersistentPlayerView()
                    .startProgressBarObserver(persistentPlayer, persistentPlayer.getCurrentView());
        }

        persistentPlayer.resetStandalonePlayButton();
        persistentPlayer.resetPlayWhenReady();
        persistentPlayer.release();
        persistentPlayer.getCurrentView().checkAuthAndPlay();
        persistentPlayer.getCurrentView().getPersistentPlayerView().syncState();
        persistentPlayer.getCurrentView().getPersistentPlayerView().startTimebar();
        // standalone play affecting pause button, let's handle this scenario
        persistentPlayer.getCurrentView().getPersistentPlayerView().resetPause();

        ErrorChecker errorChecker = new ErrorChecker(persistentPlayer.getMediaSource());
        errorChecker.reset();
    }

    public void setMediumView(Medium medium) {
        this.medium = medium;
    }
}
