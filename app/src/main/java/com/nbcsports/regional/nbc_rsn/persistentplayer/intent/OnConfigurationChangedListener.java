package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.fragment.app.Fragment;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Landscape;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Mini;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;

import timber.log.Timber;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.MINI_PLAYER_ANIMATION_DURATION_SHORT;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.LANDSCAPE;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.MEDIUM;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.MINI;

public class OnConfigurationChangedListener {

    private final PersistentPlayer persistentPlayer;

    private PlayerConstants.Type previousType;


    public OnConfigurationChangedListener(PersistentPlayer persistentPlayer) {
        this.persistentPlayer = persistentPlayer;
    }

    public void onConfigurationChanged(Configuration newConfig){
        persistentPlayer.log(this,"onConfigurationChanged checking..");

        if (persistentPlayer.isPortraitOnly()) {
            return;
        }
        if (persistentPlayer.getType() == null) {
            return;
        }
        if (persistentPlayer.getPlayerEngine() == null) {
            // handle the scenario where the preview has ended, and they rotate
            // from portrait to landscape. In this case we should stay in portrait.
            if(newConfig.orientation == ORIENTATION_LANDSCAPE && persistentPlayer.getType() == MEDIUM){
                ((MainActivity) persistentPlayer.getMainContractView()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return;
            }
            // handles the scenario where after 403 or 410 is shown on landscape, orienting back to portrait
            // causes landscape to show in portrait
            if (persistentPlayer.getType() == LANDSCAPE && previousType == MEDIUM){
                persistentPlayer.getMediumView().getPlayerView().postDelayed(
                        () -> persistentPlayer.showAsLandscape(false),
                        MINI_PLAYER_ANIMATION_DURATION_SHORT);
                previousType = LANDSCAPE;
                persistentPlayer.setType(MEDIUM);

            } else if (persistentPlayer.getType() == LANDSCAPE && previousType == MINI){
                persistentPlayer.getMiniView().getPlayerView().postDelayed(
                        () -> persistentPlayer.showAsLandscape(false),
                        MINI_PLAYER_ANIMATION_DURATION_SHORT);
                previousType = LANDSCAPE;
                persistentPlayer.setType(null);
            }
            return;
        }

        persistentPlayer.log(this,"onConfigurationChanged check complete type: %s", persistentPlayer.getType());

        PlayerConstants.Type type = persistentPlayer.getType();
        PlayerEngine.Interface playerEngine = persistentPlayer.getPlayerEngine();
        PersistentPlayerContract.View mediumView = persistentPlayer.getMediumView();
        PersistentPlayerContract.View landscapeView = persistentPlayer.getLandscapeView();
        PersistentPlayerContract.View miniView = persistentPlayer.getMiniView();

        if (newConfig.orientation == ORIENTATION_LANDSCAPE) {

            switch (type) {

                case MEDIUM:
                    // medium to landscape;
                    PlayerEngine.switchTarget(playerEngine, mediumView, landscapeView);
                    mediumView.getPlayerView().post(() -> {

                        if (persistentPlayer.getPlayerEngine() == null){
                            return;
                        }
                        persistentPlayer.showAsLandscape(true, MEDIUM);
                        if (persistentPlayer.isLive()){
                            landscapeView.receiveGoLiveState(mediumView.isPaused(), mediumView.isInGoLiveState());
                            persistentPlayer.log(this,"Player transit: Medium to Landscape isPaused: "+mediumView.isPaused()+ " isInGoLiveState: "+mediumView.isInGoLiveState());
                        }
                    });
                    landscapeView.updateNativeShareInfo(mediumView.getShareInfo());
                    previousType = MEDIUM;
                    persistentPlayer.setType(LANDSCAPE);

                    // in this scenario the media source in landscape is null,
                    // let's transfer this information to medium
                    // TODO: abstract this
                    ((Landscape) landscapeView).setMediaSource(((Medium) mediumView).getMediaSource());

                    landscapeView.updateSwitchScreenOrientation();
                    break;

                case MINI:
                    // mini to landscape;
                    PlayerEngine.switchTarget(playerEngine, miniView, landscapeView);
                    miniView.getPlayerView().post(() -> {
                        persistentPlayer.showAsLandscape(true, MINI);
                        landscapeView.updateNativeShareInfo(miniView.getShareInfo());
                        ((Fragment) miniView).getView().setVisibility(View.INVISIBLE);
                        ((Mini)miniView).enableMiniOnTouchListener(false);

                        if (persistentPlayer.isLive()) {
                            landscapeView.receiveGoLiveState(miniView.isPaused(), miniView.isInGoLiveState());
                            persistentPlayer.log(this,"Player transit: Mini to Landscape isPaused: " + miniView.isPaused() + " isInGoLiveState: " + miniView.isInGoLiveState());
                        }
                    });
                    previousType = MINI;
                    persistentPlayer.setType(LANDSCAPE);

                    // This piece of code is used to fix an issue where
                    // Medium -> MINI -> Landscape, Landscape's MediaSource is not
                    // updated
                    ((Landscape) landscapeView).setMediaSource(persistentPlayer.getMediaSource());

                    landscapeView.updateSwitchScreenOrientation();
                    break;
            }

        } else {

            switch (type) {
                case LANDSCAPE:
                    if (previousType == MINI) {
                        // landscape to mini;
                        PlayerEngine.switchTarget(playerEngine, landscapeView, miniView);
                        miniView.getPlayerView().post(() -> {
                            persistentPlayer.showAsLandscape(false);
                            // Adding this line fixes the issue
                            // Medium -> Mini -> Landscape (Switch to last viewed) -> Mini
                            // -> Medium
                            // Mini won't hide
                            // Because Switch to last viewed will call persistentPlayer.release()
                            // And miniShown will be set to false
                            // So it needs to manually reset to true
                            persistentPlayer.setMiniShown(true);
                            ((Fragment) miniView).getView().setVisibility(View.VISIBLE);
                            ((Mini)miniView).enableMiniOnTouchListener(true);
                        });
                        previousType = LANDSCAPE;
                        persistentPlayer.setType(MINI);

                        if (persistentPlayer.isLive()) {
                            miniView.receiveGoLiveState(landscapeView.isPaused(), landscapeView.isInGoLiveState());
                            persistentPlayer.log(this,"Player transit: Landscape to Mini isPaused: " + landscapeView.isPaused() + " isInGoLiveState: " + landscapeView.isInGoLiveState());
                        }
                        mediumView.updateSwitchScreenOrientation();
                    } else if (previousType == MEDIUM) {
                        // landscape to medium;
                        mediumView.getPlayerView().postDelayed(() -> {
                            persistentPlayer.showAsLandscape(false);
                            PlayerEngine.switchTarget(playerEngine, landscapeView, mediumView);
                        }, MINI_PLAYER_ANIMATION_DURATION_SHORT); // RSNAPP-596: Black bar issue fixed. Need to sync up with mini animation
                        previousType = LANDSCAPE;
                        persistentPlayer.setType(MEDIUM);
                        // Adding this line will fix the issue where
                        // Landscape -> medium
                        // the currentView on persistentPlayer is never change to medium
                        persistentPlayer.setCurrentType(MEDIUM);

                        if (persistentPlayer.isLive()) {
                            mediumView.receiveGoLiveState(landscapeView.isPaused(), landscapeView.isInGoLiveState());
                            persistentPlayer.log(this,"Player transit: Landscape to Medium isPaused: " + landscapeView.isPaused() + " isInGoLiveState: " + landscapeView.isInGoLiveState());
                        }
                        mediumView.updateSwitchScreenOrientation();

                        PersistentPlayerView.syncState(landscapeView.getPersistentPlayerView(), mediumView.getPersistentPlayerView());

                        // Start progress bar show and hide observer
                        // Only handle progress bar observer in
                        // Landscape to Medium
                        // It is because on Medium or Mini to Landscape
                        // Landscape.show() and Landscape.hide() method
                        // Already handling this
                        // Also Landscape to Mini should not trigger
                        // progress bar observer, because Mini does not
                        // need to show the progress bar
                        if (mediumView != null && persistentPlayer != null){
                            // Remove all progress bar observers first
                            PersistentPlayerProgressBarManager.removeAllObservers();
                            // Start and add progress bar observer into
                            // PersistentPlayerProgressBarManager
                            mediumView.getPersistentPlayerView().startProgressBarObserver(persistentPlayer, mediumView);
                            // This fixes the issue
                            // Medium -> Landscape (Switch to last viewed) -> Medium
                            // Time bar does not move
                            mediumView.getPersistentPlayerView().startTimebar();
                            // Reset time bar listener
                            mediumView.getPersistentPlayerView().resetTimeBarListener(persistentPlayer);
                        }
                    }
            }
        }
    }
}
