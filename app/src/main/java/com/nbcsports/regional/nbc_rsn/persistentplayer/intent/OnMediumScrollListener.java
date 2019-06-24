package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;

public class OnMediumScrollListener {
    private final Medium medium;
    private final PersistentPlayer persistentPlayer;

    public OnMediumScrollListener(Medium medium) {
        this.medium = medium;
        persistentPlayer = medium.getPersistentPlayer();
    }

    public void show(boolean visible) {
        persistentPlayer.log(this, "medium.show %s", DisplayUtils.isVisible(medium));

        if (medium.isShown() && visible) {

            persistentPlayer.log(this, "medium.show is isShown() & visible=true");
            if (medium.isMiniShowingOnMedium()) {
                medium.showStandalonePlayButton();
                return;
            }
            medium.checkAuthAndPlay();
            persistentPlayer.log(persistentPlayer, "player transit: mini is shown "+persistentPlayer.isMiniShown());
            if (persistentPlayer.isMiniShown()){
                persistentPlayer.showMini(false);
            } else {
                // Adding this is because
                // Medium (pause) -> Scroll down team feed page -> Scroll up team feed page
                // -> Medium
                // The Medium is showing a dark screen.
                // And it is because only the showMini(boolean) is calling the switchTarget(...)
                PlayerEngine.switchTarget(persistentPlayer.getPlayerEngine(), null, medium);
            }
            medium.getPersistentPlayerView().startTimebar();
            // Re-apply the time bar listener to the time bar
            medium.getPersistentPlayerView().resetTimeBarListener(persistentPlayer);

            // Start progress bar show and hide observer
            if (medium != null && persistentPlayer != null){
                // Remove all progress bar observers first
                PersistentPlayerProgressBarManager.removeAllObservers();
                // Start and add progress bar observer into
                // PersistentPlayerProgressBarManager
                medium.getPersistentPlayerView().startProgressBarObserver(persistentPlayer, medium);
            }

            // Show temp pass count down if available
            if (medium != null && persistentPlayer != null && medium.getMediaSource() != null
                    && medium.getMediaSource().getLive() && medium.getMediaSource().getAsset() != null
                    && !medium.getMediaSource().getAsset().isFree()
                    && medium.getStreamAuthenticationPresenter() != null
                    && medium.getStreamAuthenticationPresenter().getLastKnownAuth() != null
                    && medium.getStreamAuthenticationPresenter().getConfig() != null
                    && medium.getStreamAuthenticationPresenter().getLastKnownAuth().isTempPass(medium.getStreamAuthenticationPresenter().getConfig())){
                medium.showTempPassCountDown(medium.getStreamAuthenticationPresenter().getLastKnownAuth());
            }

        } else {
            // the check for MINI is necessary so that when medium is no longer visible
            // even though we need to reset orientation, but there is an exception where mini is
            // still showing, we will ignore resetOrientation so that mini is able to transition to landscape
            if (persistentPlayer.getType() != PlayerConstants.Type.MINI) {
                persistentPlayer.resetOrientation();
            }
            persistentPlayer.log(this, "medium.show is %s persistentPlayer.isPaused is %s", visible, persistentPlayer.isPaused());

            if (!visible
                    && ! persistentPlayer.isPaused()
                    && persistentPlayer.isPlaying()) {

                persistentPlayer.showMini(true);
            }

            // Stop progress bar show and hide observer
            if (medium != null){
                medium.getPersistentPlayerView().stopProgressBarObserver(medium);
            }
        }

        // This piece of code is used for the following scenario
        // medium player -> mini player -> switch to other team feed
        // -> switch back to old team feed -> mini player -> medium player
        // Since when switch to other team feed, the medium player will be
        // released and need to add the playback event listener and ad event
        // listener again
        if (persistentPlayer.getPlayerEngine() != null) {
            persistentPlayer.getPlayerEngine().addListener(medium);
        }
    }

    public void hide() {
        if (persistentPlayer.getType() == PlayerConstants.Type.MEDIUM
                && persistentPlayer.getPlayerEngine() != null) {

            persistentPlayer.setPlayWhenReady(false);
            persistentPlayer.resetOrientation();
            persistentPlayer.log(this, "medium.hide() setPlayWhenReady(false), isShown: %s", medium.isShown());
        }

        // Stop progress bar show and hide observer
        if (medium != null){
            medium.getPersistentPlayerView().stopProgressBarObserver(medium);
        }

        if (persistentPlayer.getPlayerEngine() != null) {
            persistentPlayer.getPlayerEngine().removeListener(medium);
        }
    }
}
