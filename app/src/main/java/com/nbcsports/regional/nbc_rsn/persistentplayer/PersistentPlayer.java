package com.nbcsports.regional.nbc_rsn.persistentplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.View;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaAnalyticPlayerManager;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaContract;
import com.nbcsports.regional.nbc_rsn.authentication.Injection;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationFragment;
import com.nbcsports.regional.nbc_rsn.chromecast.IChromecastHelper;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayWhenReadyListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PrimetimePlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.imaPrerollsForSkyItalia.ImaAds;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnConfigurationChangedListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Landscape;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

import static android.view.View.GONE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.PlayerEngine.Type.PT;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.LANDSCAPE;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.MEDIUM;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type.MINI;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Type._247;

/**
 * init()
 * -> setCurrentType()
 * -> setMediaSource()
 * play()
 * -> pause() -> release()
 */
public class PersistentPlayer implements AdEvent.AdEventListener {
    @NonNull
    protected final PersistentPlayerContract.Main.View mainActivityView;

    @Getter
    private final MainContract.View mainContractView;
    private final OnConfigurationChangedListener onConfigurationChangedListener;
    private StreamAuthenticationContract.Presenter streamAuthentication;

    @Setter
    private IChromecastHelper chromecastHelper;

    @NonNull @Getter
    protected PersistentPlayerContract.View landscapeView;

    @NonNull
    private final int landscapeFragmentID;

    @NonNull @Getter
    private final PersistentPlayerContract.View miniView;

    @NonNull
    private final int miniFragmentID;

    @Getter
    protected PersistentPlayerContract.View mediumView;

    @Getter @Setter
    private PlayerEngine.Interface playerEngine;

    @Getter
    private MediaSource mediaSource;

    private boolean miniShown;
    public boolean isMiniShown() {
        return miniShown;
    }
    public void setMiniShown(boolean isMiniShown) {
        miniShown = isMiniShown;
    }

    @Setter
    protected PlayerConstants.Type type;

    @Getter
    private boolean playWhenReady = true;

    @Getter @Setter
    private boolean goLive;

    @Getter @Setter
    private boolean isAdPlaying;

    @Getter @Setter
    private boolean isPaused;

    @Setter
    private boolean muted;

    private float currentVolume;
    protected boolean loadCompleted;
    private long playbackPosition;
    private boolean closedCaptionEnabled;

    @Getter
    private boolean portraitOnly;

    @Getter
    protected PersistentPlayerContract.View currentView;
    @Getter @Setter
    private boolean is247;

    @Getter @Setter
    private long lastStoppedPosition;

    @Getter
    private boolean shouldContinueScrubber = false;
    private boolean shouldSaveScrubberIfMiniDismissed = true;

    @Getter
    NativeShareUtils.ShareInfo shareInfo;

    private PlayWhenReadyListener playWhenReadyListener;

    // [Sky Italia]
    @Getter
    private ImaAds imaAds;

    public PersistentPlayer(PersistentPlayerContract.Main.View mainActivity,
                            MainContract.View mainContractView,
                            PersistentPlayerContract.View landscapeView, int landscapeFragmentID,
                            PersistentPlayerContract.View miniView, int miniFragmentID) {

        mainActivityView = mainActivity;
        this.mainContractView = mainContractView;
        this.landscapeView = landscapeView;
        this.landscapeFragmentID = landscapeFragmentID;
        this.miniView = miniView;
        this.miniFragmentID = miniFragmentID;

        onConfigurationChangedListener = new OnConfigurationChangedListener(this);
        streamAuthentication = Injection.provideStreamAuthentication((StreamAuthenticationContract.View) mainActivityView.getContext());
    }

    public void init() {
        playerEngine = PlayerEngine.newInstance(PT, mainActivityView.getContext());

        // Init Ads Loader [Sky Italia]
        //this.imaAds = new ImaAds(mainActivityView.getContext(), currentView.getPrimetimeView(), (PrimetimePlayerEngine) getPlayerEngine());
        this.imaAds = new ImaAds(mainActivityView.getContext(), currentView.getPrimetimeView(), this);
        imaAds.initAdsLoader();
    }

    public void playIMAPreRoll() {
        // --- [Sky Italia] ---
        // Request and play Preroll
        imaAds.requestAds();
        // --------------------
    }

    // [Sky Italia]
    @Override
    public void onAdEvent(AdEvent adEvent) {
        switch (adEvent.getType()) {
            case LOADED:
                // adsManager should start()
                Timber.d("[Sky Italia] Ad Duration: " + adEvent.getAd().getDuration());
                if (getCurrentView() == mediumView) {
                    Timber.d("[Sky Italia] Disable ad view change to landscape when ad is playing on medium");
                    setPortraitOnly(true);
                }
                break;
            case PAUSED:
                // Ad is paused from user clicking LEARN MORE Link on ad
                Timber.d("[Sky Italia] Ad was paused. Resume Ad now.");
                imaAds.getAdsManager().resume();
                break;
            case CONTENT_PAUSE_REQUESTED:
                // Pause video to play ad
                Timber.d("[Sky Italia] CONTENT_PAUSE");
                imaAds.setAdDisplayed(true);
                // pause asset
                //getPlayerEngine().pauseMediaPlayer();
                break;
            case CONTENT_RESUME_REQUESTED:
                Timber.d("[Sky Italia] CONTENT_RESUME");
                // resume asset after ad is done
                imaAds.setAdDisplayed(false);
                //getPlayerEngine().playMediaPlayer();

                // Change video player back to landscape if orientation is landscape / unlock portrait only
                setPortraitOnly(false);

                if (!getPlayerEngine().isPlaying()) {
                    Timber.d("[Sky Italia] Video was paused, resume playback");
                    getPlayerEngine().playMediaPlayer();
                } else {
                    // GO back to prev position or beginning of video
                    Timber.d("[Sky Italia] seekTo(playbackPosition)");
                    getPlayerEngine().seekTo(playbackPosition);
                }

                break;
            case ALL_ADS_COMPLETED:
                // Destroy AdsManager
                if (imaAds.getAdsManager() != null) {
                    Timber.d("[Sky Italia] DESTROY AdsManager");
                    imaAds.getAdsManager().destroy();
                    imaAds.setAdsManager(null);
                }
                break;
        }
    }

    public void release() {
        log("release");
        setGoLive(false);
        setAdPlaying(false);
        setPaused(false);

        if (getPlayerEngine() != null) {
            // Remove all kochava analytic disposable observer
            // and reset all variables
            // and send the last duration event if available
            KochavaAnalyticPlayerManager.removeAllObservers(this,
                    (KochavaContract.View) mainActivityView.getContext());

            if (mediumView != null) {
                getPlayerEngine().cleanPlayerView(mediumView.getPersistentPlayerView());
            }
            getPlayerEngine().cleanPlayerView(miniView.getPersistentPlayerView());
            getPlayerEngine().cleanPlayerView(landscapeView.getPersistentPlayerView());
            getPlayerEngine().release();
            setPlayerEngine(null);
        }

        miniShown = false;

        if (mediumView != null) {
            mediumView.reset();
        }
        if (landscapeView != null) {
            landscapeView.reset();
        }
        if (miniView != null) {
            miniView.reset();
        }
        closedCaptionEnabled = false;

        mediaSource = null;

    }

    public void updateShareInfo(NativeShareUtils.ShareInfo shareInfo) {
        this.shareInfo = shareInfo;
        landscapeView.updateNativeShareInfo(shareInfo);
        if (mediumView != null)
            mediumView.updateNativeShareInfo(shareInfo);
        miniView.updateNativeShareInfo(shareInfo);
    }

    public void setCurrentType(PlayerConstants.Type type) {
        // Adding this condition to fix the following issue
        // Medium_1 (pause) -> Scroll to other team feed -> Medium_2
        // Medium_2 is not loading if Medium_1 and Medium_2 are not the same view
        // The purpose of checking currentView != null and mediumView != null is
        // to make sure if the mediumView is the first mediumView that users ever seen,
        // then skip resetting isPaused
        if (this.type == MEDIUM && type == MEDIUM && currentView != null
                && mediumView != null && currentView.hashCode() != mediumView.hashCode()){
            setPaused(false);
            currentView.showStandalonePlayButton();
        }
        this.type = type;

        if (type != null) {
            if (is247) {
                setOrientationLandscape();
            } else if (!portraitOnly
                    && !(NavigationManager.getInstance().getCurrentFragment() instanceof StreamAuthenticationFragment)) {
                setOrientationSensor();
            }
        } else {
            ((Activity) mainActivityView).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        switch (type) {
            case MEDIUM:
                currentView = mediumView;
                break;
            case LANDSCAPE:
                currentView = landscapeView;
                break;
            case MINI:
                currentView = miniView;
                break;
        }
    }

    public void play() {
        Timber.d("Kochava player: kochava play()");

        // if the chromecast helper exists updated the reference to persistent player
        if (chromecastHelper != null) {
            chromecastHelper.setPersistentPlayer(this);
        }

        if (mediaSource == null) {
            log("play() fail: mediaSource is null");
            return;
        }
        if (type == null) {
            log("play() fail: type is null");
            return;
        }

        String mediaUrl = mediaSource.getStreamUrl();
        String mediaTitle = mediaSource.getTitle();

        // If persistentPlayer got released, init it and make sure loadComplete is set to false
        if (getPlayerEngine() == null) {
            init();
            loadCompleted = false;

            // Remove all kochava analytic disposable observer
            // and reset all variables
            // and send the last duration event if available
            KochavaAnalyticPlayerManager.removeAllObservers(this,
                    (KochavaContract.View) mainActivityView.getContext());

            // Start tracking video status for kochava analytic
            KochavaAnalyticPlayerManager.startVideoTracking(this,
                    (KochavaContract.View) mainActivityView.getContext());
        }

        playbackPosition = getPlayerEngine().getCurrentPosition();

        if (!loadCompleted) {
            switch (type) {
                case MEDIUM:
                    PlayerEngine.switchTarget(playerEngine, null, mediumView);
                    // calls SwitchScreen.onVideoPlay() -> switchScreenAdapter.notifyDataSetChanged()
                    landscapeView.updateSwitchScreen(mediaSource);
                    mediumView.updateSwitchScreen(mediaSource);
                    break;

                case LANDSCAPE:
                    PlayerEngine.switchTarget(playerEngine, null, landscapeView);
                    if (mediumView != null)
                        // calls SwitchScreen.onVideoPlay() -> switchScreenAdapter.notifyDataSetChanged()
                        mediumView.updateSwitchScreen(mediaSource);
                        landscapeView.updateSwitchScreen(mediaSource);
                    break;

                case MINI:
                    break;
            }
            streamAuthentication = Injection.provideStreamAuthentication((StreamAuthenticationContract.View) mainActivityView.getContext());
            getPlayerEngine().prepare(mediaUrl, this,
                    mainActivityView.getContext(),
                    streamAuthentication.getLastKnownAuth(),
                    mediaSource);
            //Behaviour.autoPlay(this, getPlayerEngine());
        }

        // [Sky Italia] PLay preRoll
        if (getPlayerEngine() != null) {
            playIMAPreRoll();
        }

        getPlayerEngine().seekTo(playbackPosition);
        boolean ableToPlay = getPlayerEngine().setPlayWhenReady(getPlayerEngine().getPlayWhenReady());

        if (isPhoneMuted()) {
            mute(true);
        } else {
            mute(muted);
        }

        if (mediumView != null) {
            mediumView.reset();
        }
        if (landscapeView != null) {
            landscapeView.reset();
        }
        if (miniView != null) {
            miniView.reset();
        }

        resetStandalonePlayButton();

        if (! ableToPlay) {
            log(this, "play() fail: title:%s", mediaTitle);
        } else {
            log(this, "play() success: title:%s", mediaTitle);
        }

        // This piece of code checks if current type is MEDIUM
        // If so then add playback event listener and AD event listener for Medium
        // This is used when play a new steam in medium
        // Please note: both here and the one inside OnMediumScrollListener are required
        // and they are handling different things
        if (type == MEDIUM && mediumView instanceof Medium){
            getPlayerEngine().addListener((Medium)mediumView);
        }

    }

    private boolean isPhoneMuted() {
        AudioManager audioManager = (AudioManager) mainActivityView.getContext().getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
    }

    public boolean setMediaSource(MediaSource newMediaSource) {

        if (mediaSource != null && newMediaSource != null) {

            if (!mediaSource.getStreamUrl().equalsIgnoreCase(newMediaSource.getStreamUrl())
                    && getPlayerEngine() != null) {
                // that means a new media source is loaded
                // lets release the player
                loadCompleted = false;
                release();
                mediaSource = newMediaSource;
                log("setMediaSource: NEW media source is loaded, loadCompleted=false, exoPlayer.stop(), newMediaSource:%s", newMediaSource);
            }

            if (mediaSource.getStreamUrl().equalsIgnoreCase(newMediaSource.getStreamUrl())) {
                // if the stream url is the same
                // ignore mediaSource assignment because it's the same content AND
                // reset loadCompleted
                loadCompleted = false;
                log("setMediaSource: SAME media source is loaded, do nothing. newMediaSource:%s", newMediaSource);
                return false;
            }
        }

        mediaSource = newMediaSource;
        return true;
    }

    public void resetPlayWhenReady() {
        // By default, the player should play the content as soon as prepare() is complete
        playWhenReady = true;
    }

    public void setPlayWhenReady(boolean play) {
        // never start playing if connected to chromecast
        setPlayWhenReady(chromecastHelper.isStateConnected() ? false : play, PlayerConstants.Save.ONE_TIME_ONLY);
    }

    public void setPlayWhenReady(boolean play, PlayerConstants.Save saveType) {
        log("setPlayWhenReady play:%s", play);
        if (saveType == PlayerConstants.Save.REMEMBER) {
            playWhenReady = play;
        }
        if (getPlayerEngine() != null) {
            getPlayerEngine().setPlayWhenReady(play);
            if (playWhenReadyListener != null){
                playWhenReadyListener.onSet(play);
            }
        }
    }

    public void setPlayWhenReadyListener(PlayWhenReadyListener playWhenReadyListener){
        this.playWhenReadyListener = playWhenReadyListener;
    }

    public boolean isPlaying() {

        if (getPlayerEngine() == null) {
            return false;
        }

        return playerEngine.isPlaying();
    }

    public boolean isSuspended() {
        if (getPlayerEngine() == null) {
            return false;
        }

        return playerEngine.isSuspended();
    }

    public boolean isLandscapeVisible(@NonNull FragmentManager supportFragmentManager) {
        Fragment fragment = supportFragmentManager.findFragmentById(landscapeFragmentID);
        return fragment != null && fragment.isVisible();
    }

    public void mute(boolean enable) {
        log("mute enable: %s", enable);
        muted = enable;

        if (getPlayerEngine() != null) {
            if (enable) {
                currentVolume = getPlayerEngine().getVolume();
                getPlayerEngine().setVolume(0f);
            } else {
                if (currentVolume == 0) {
                    currentVolume = getPlayerEngine().getVolume();
                }
                getPlayerEngine().setVolume(currentVolume);
            }
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public PlayerConstants.Type getType() {
        return type;
    }

    public void showAsLandscape(boolean show) {
        showAsLandscape(show, LANDSCAPE);
    }

    /**
     * Show landscape method
     *
     * As the implementation below, the methods calling order will be
     * ...
     * landscape.showUI(...)
     * landscape.show(...)
     * ...
     *
     * That means everything on showUI() will happen before everything on show()
     *
     * @param show
     * @param source
     */
    public void showAsLandscape(boolean show, PlayerConstants.Type source) {
        if (show) {
            updateShareInfo(shareInfo);
            streamAuthentication = Injection.provideStreamAuthentication((StreamAuthenticationContract.View) mainActivityView.getContext());
            if (streamAuthentication != null
                    && (source == MEDIUM || source == _247 || source == MINI)){
                ((Landscape) landscapeView).showUI(streamAuthentication.getLastKnownAuth());
            }
            mainActivityView.showLandscape(source);
            mainContractView.hideFab();
        } else {
            mainActivityView.hideLandscape();
            // only show fab when streamAuthenticationFragment is hidden
            if(!(NavigationManager.getInstance().getCurrentFragment() instanceof StreamAuthenticationFragment)) {
                mainContractView.showFab();
            }
        }
    }

    /**
     * Generic show/hide mini view
     * @param show
     */
    private void showAsMini(boolean show) {

        if (getPlayerEngine() == null) {
            log("showAsMini: return if player engine == null");
            return;
        }

        if ( isPaused() ) {
            log("showAsMini: return if paused");
            // Adding this line will fix mini not showing when
            // Medium -> Mini -> Landscape (pause) -> Mini -> Medium (play) -> Mini
            miniShown = false;
            return;
        }

        if (show) {

            if (miniShown) {
                // if it's already shown
                log("showAsMini: return if it's already shown");
                return;
            }

            mainActivityView.showMini();
            type = MINI;
            miniShown = true;

        } else {

            if (!miniShown) {
                return;
            } // if it's already hidden

            mainActivityView.hideMini();
            miniShown = false;
        }
    }

    public void showMiniIfAvailable(boolean show) {

        if (getPlayerEngine() == null) {
            return;
        }

        log("showMiniIfAvailable show: %s, type: %s, playWhenReady: %s, playbackState: %s",
                show, type, getPlayerEngine().getPlayWhenReady(), getPlayerEngine().getPlaybackState());

        if (type == MINI && getPlayerEngine().getPlayWhenReady()) {
            showAsMini(show);
        }
    }

    public void log(Object object, String string, Object... args) {
        try {
            checkArgument(object != null && string != null, "object or string cannot be null");
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            return;
        }

        Class<?> enclosingClass = object.getClass().getEnclosingClass();
        String className;
        if (enclosingClass != null) {
            className = enclosingClass.getSimpleName();
        } else {
            className = getClass().getSimpleName();
        }

        String message = String.format(string, args);
        Timber.d("(%s:%s:%s) %s",
                className,
                getPlayerEngine() != null ? getPlayerEngine().hashCode() : null,
                type,
                message);
    }

    public void log(String string, Object... args) {
        log(this, string, args);
    }

    public void showStreamAuthentication(MediaSource mediaSource) {
        resetOrientation();
        NavigationManager.getInstance().showStreamAuthenticationFragment(mediaSource);
    }

    public void closeStreamAuthentication() {
        mainActivityView.closeStreamAuthenticationFragment();
    }

    /**
     * This is used when transition from medium -> mini
     * @param visible
     */
    public void showMini(boolean visible) {

        if (mediumView == null) {
            return;
        }

        // [Sky Italia] Disable show mini when an ad is playing
        if (imaAds.isAdDisplayed()) {
            Timber.d("[Sky Italia] Mini view disabled when ad is playing");
            return;
        }

        miniView.updateNativeShareInfo(mediumView.getShareInfo());
        if (visible) {
            if(this.type == MEDIUM) {
                PlayerEngine.switchTarget(playerEngine, mediumView, miniView);
                mediumView.getPlayerView().post(() -> {
                    showAsMini(true);
                    if (!portraitOnly)
                        setOrientationSensor();
                });

                if (isLive()) {
                    miniView.receiveGoLiveState(mediumView.isPaused(), mediumView.isInGoLiveState());
                    log("Player transit: Medium to Mini isPaused: " + mediumView.isPaused() + " isInGoLiveState: " + mediumView.isInGoLiveState());
                }
            }
        } else {
            PlayerEngine.switchTarget(playerEngine, miniView, mediumView);
            showAsMini(false);
            mainActivityView.hideMini();

            if (mediumView.getPersistentPlayerView() != null
                    && mediumView.getPersistentPlayerView().getLastKnownPersistentPlayer() == null){
                mediumView.showPlayerControlView();
            }

            if (isLive()) {
                mediumView.receiveGoLiveState(miniView.isPaused(), miniView.isInGoLiveState());
                log("Player transit: Mini to Medium isPaused: " + miniView.isPaused() + " isInGoLiveState: " + miniView.isInGoLiveState());
            }

            PersistentPlayerView.syncState(landscapeView.getPersistentPlayerView(), mediumView.getPersistentPlayerView());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        onConfigurationChangedListener.onConfigurationChanged(newConfig);
    }

    public void resetOrientation() {
        log("resetOrientation Portrait");
        ((Activity) mainActivityView).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void setOrientationLandscape() {
        log("setOrientation Landscape");
        ((Activity) mainActivityView).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    public void setOrientationSensor() {
        log("setOrientationLandscape Sensor");
        if (getPlayerEngine() == null) {
            log("setOrientationLandscape Sensor, cancel because player is null");
            return;
        }
        if (portraitOnly) {
            log("setOrientationLandscape Sensor, cancel because because portrait only");
            return;
        }
        ((Activity) mainActivityView).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public void setPortraitOnly(boolean orientationLockEnabled) {
        this.portraitOnly = orientationLockEnabled;
        if (portraitOnly) {
            ((Activity) mainActivityView).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            if ((mediumView != null && mediumView.isShown())
                    || (miniView != null && miniView.isShown())) {

                if ((getPlayerEngine() != null && getPlayerEngine().isFinished())
                        || DataMenuUtils.INSTANCE.getDATA_MENU_IS_OPENED()){
                    return;
                }
                setOrientationSensor();
            }
        }
        log("setPortraitOnly() portraitOnly: %s", portraitOnly);
    }

    public PlayerConstants.State getState() {

        if (mediumView == null) {
            return PlayerConstants.State.NOT_SHOWING;
        }

        if (DisplayUtils.isVisible((View) mediumView)) {
            if (mediumView.isAuthorized()) {
                return PlayerConstants.State.SHOWING_AUTHENTICATED;
            } else {
                return PlayerConstants.State.SHOWING_NOT_AUTHENTICATED;
            }
        } else {
            return PlayerConstants.State.NOT_SHOWING;
        }
    }

    public boolean isLive() {
        if (mediaSource != null) {
            return mediaSource.getLive();
        }
        return false;
    }

    public void showAs247() {

        is247 = true;
        resetPlayWhenReady();
        setCurrentType(LANDSCAPE);
        landscapeView.reset();
        // it's important to call play() before showAsLandscape() due to player engine not being set
        // if play() is not called first
        play();

        showAsLandscape(true, PlayerConstants.Type._247);
        landscapeView.updateNativeShareInfo(shareInfo);
        landscapeView.enableSwitchScreen(false);
        landscapeView.enableCloseButton(true);


        if (mediumView != null)
            mediumView.reset();
        if (miniView != null)
            miniView.reset();
    }

    public void setMediumView(PersistentPlayerContract.View mediumView) {
        if (this.mediumView != null && playerEngine != null) {
            playerEngine.cleanPlayerView(this.mediumView.getPersistentPlayerView());
        }
        this.mediumView = mediumView;
    }

    public void resetStandalonePlayButton() {
        if (mediumView != null)
            mediumView.getPersistentPlayerView().getStandalonePlayButton().setVisibility(GONE);
        if (landscapeView != null)
            landscapeView.getPersistentPlayerView().getStandalonePlayButton().setVisibility(GONE);
    }

    public boolean isClosedCaptionEnabled() {
        return closedCaptionEnabled;
    }

    public void enableClosedCaption(final boolean closedCaptionEnabled) {
        if (playerEngine == null) return;
        this.closedCaptionEnabled = closedCaptionEnabled;
        playerEngine.enableClosedCaption(closedCaptionEnabled);
    }

    public IChromecastHelper getChromecastHelper(){
        return chromecastHelper;
    }


   /**
     * This will compare the mediaSource object using the streamUrl without the query
     * removing the query is to prevent the token getting mixed when doing equalsIgnoreCase
     * @param oldSource
     * @param newSource
     * @return
     */
   public static boolean isMediaSourceEqual(MediaSource oldSource, MediaSource newSource){

       if (oldSource == null && newSource != null) {
           return false;
       }
       if (oldSource == null && newSource == null) {
           return true;
       }

       URL oldUrl;
       URL newUrl;

       try {
           oldUrl = new URL(oldSource.getStreamUrl());
           newUrl = new URL(newSource.getStreamUrl());
       } catch (MalformedURLException e) {
           e.printStackTrace();
           return false;
       }
       String oldUrlNoQuery = oldUrl.getProtocol() + "://" + oldUrl.getHost() + "?" + oldUrl.getPath();
       String newUrlNoQuery = newUrl.getProtocol() + "://" + newUrl.getHost() + "?" + newUrl.getPath();

       return StringUtils.equalsIgnoreCase(oldUrlNoQuery, newUrlNoQuery);
    }

    public void continueScrubber() {
       if(playerEngine != null) {
           playerEngine.seekTo(lastStoppedPosition);
           lastStoppedPosition = 0;
           // reset boolean storing whether or not we should continue from the last stop position
           // so the player doesn't go backwards when scrolling between medium and mini
           shouldContinueScrubber = false;
       }
    }

    public void saveCurrentScrubberPosition() {
        if(playerEngine != null && shouldSaveScrubberIfMiniDismissed) {
            lastStoppedPosition = playerEngine.getCurrentPosition();
            shouldContinueScrubber = true;
        }
        shouldSaveScrubberIfMiniDismissed = true;
    }

    public void resetSavedScrubberPosition() {
        shouldContinueScrubber = false;
        if(miniShown) {
            shouldSaveScrubberIfMiniDismissed = false;
        }
    }

    public void pause() {
       setPaused(true);
       setPlayWhenReady(false, PlayerConstants.Save.REMEMBER);
    }
}
