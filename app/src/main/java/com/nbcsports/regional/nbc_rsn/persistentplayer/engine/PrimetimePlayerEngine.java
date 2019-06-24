package com.nbcsports.regional.nbc_rsn.persistentplayer.engine;

import android.content.Context;
import android.media.AudioManager;
import android.view.ViewGroup;

import com.adobe.mediacore.DefaultMediaPlayer;
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.adobe.mediacore.qos.LoadInfo;
import com.adobe.mediacore.timeline.advertising.Ad;
import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.adobe.mediacore.timeline.advertising.AdBreakPlacement;
import com.adobe.mediacore.timeline.advertising.AdClick;
import com.adobe.mediacore.utils.TimeRange;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.analytics.heartbeat.AdobeHeartbeatAnalytics;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaAnalytic;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaAnalyticPlayerManager;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaContract;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Behaviour;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.ads.NbcAdvertisingFactory;
import com.nbcsports.regional.nbc_rsn.persistentplayer.imaPrerollsForSkyItalia.ImaAds;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Landscape;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Medium;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Mini;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

import lombok.Getter;
import timber.log.Timber;

import static com.adobe.mediacore.MediaPlayer.PlayerState.PLAYING;
import static com.adobe.mediacore.MediaPlayer.PlayerState.PREPARED;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.Error._403;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.PlayerEngine.Type.PT;

public class PrimetimePlayerEngine implements PlayerEngine.Interface {

    private final KochavaContract.Presenter kochava;
    MediaPlayer mediaPlayer;
    private MediaPlayer.PlaybackEventListener mediumPlaybackEventListener;
    private MediaPlayer.PlaybackEventListener miniPlaybackEventListener;
    private MediaPlayer.PlaybackEventListener landscapePlaybackEventListener;
    private MediaPlayer.PlaybackEventListener unknownPlaybackEventListener;
    private NbcAdvertisingFactory adClient;
    private MediaPlayer.AdPlaybackEventListener mediumAdEventListener;
    private MediaPlayer.AdPlaybackEventListener miniAdEventListener;
    private MediaPlayer.AdPlaybackEventListener landscapeAdEventListener;
    private MediaPlayer.AdPlaybackEventListener unknownAdEventListener;
    private AdobeHeartbeatAnalytics heartbeat;
    private MediaPlayer.PlayerState lastPlayerState;
    private MediaSource lastKnownMediaSource;
    private boolean isBuffering;

    public PrimetimePlayerEngine(Context context) {
        mediaPlayer = DefaultMediaPlayer.create(context.getApplicationContext(), BuildConfig.IS_PROD);
        kochava = KochavaAnalytic.Injection.provideKochava((KochavaContract.View) context);
    }

    // [Sky Italia]
    public void pauseMediaPlayer() {
        // Pauses video
        if (mediaPlayer != null) mediaPlayer.pause();
    }
    public void playMediaPlayer() {
        // resume
        if (mediaPlayer != null) mediaPlayer.play();
    }

    @Override
    public void switchTarget(PersistentPlayerContract.View oldView, PersistentPlayerContract.View newView) {

        if (mediaPlayer == null){
            return;
        }
        // If the layout has animateChanges=true, it will prevent parent from being removed.
        // Make sure it's set to false.
        if (mediaPlayer.getView().getParent() != null) {
            ((ViewGroup) mediaPlayer.getView().getParent()).removeView(mediaPlayer.getView());
        }

        newView.getPrimetimeView().addView(mediaPlayer.getView(), 0);
    }

    @Override
    public void prepare(String mediaUrl, PersistentPlayer persistentPlayer, Context context, Auth auth, MediaSource mediaSource) {
        try {

            // create preroll ads
            MetadataNode metadata = PrimetimePlayerAd.createMetadata(context, persistentPlayer.getMediaSource(), auth);

            // set up analytics
            Config config = ((MainActivity) context).getConfig();
            if (heartbeat == null) {
                heartbeat = new AdobeHeartbeatAnalytics(context, auth, persistentPlayer, config);
            }
            heartbeat.attachPlayer(mediaPlayer);
            heartbeat.startPlayback(mediaSource.getAsset(), metadata, mediaSource);

            // create a MediaResource instance pointing to some HLS content
            MediaResource mediaResource = MediaResource.createFromUrl(mediaUrl, metadata);

            if ( mediaPlayer.getCurrentItem() == null
                    || ! mediaPlayer.getCurrentItem().getResource().equals(mediaResource)) {
                mediaPlayer.replaceCurrentItem(mediaResource);
            }

            // setup ads (midrolls:podbusters)
            setupPodbusters(context, persistentPlayer.getMediaSource());

            // Set up prepare playback event listener
            // Please note: although every medium, mini, landscape or
            // unknown are having their own playback event listener,
            // this prepare playback event listener is used for general operations
            // This listener will be removed when mediaPlayer.release() is called
            mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, getNewPlaybackEventListener(true,
                    config, persistentPlayer, context, auth, mediaSource, null));
            // Set up qos event listener for
            // 1. Buffering detection for video loading spinner
            // This listener will be removed when mediaPlayer.release() is called
            mediaPlayer.addEventListener(MediaPlayer.Event.QOS, getNewQOSEventListener());

            lastKnownMediaSource = mediaSource;

        } catch(IllegalArgumentException ex) {
            // this exception is thrown if the URL does not point
            // to a valid url.
            Behaviour.loadCancel(persistentPlayer);
        }
    }

    @Override
    public boolean setPlayWhenReady(boolean playWhenReady) {

        if (mediaPlayer.getStatus() == MediaPlayer.PlayerState.INITIALIZING
                || mediaPlayer.getStatus() == MediaPlayer.PlayerState.PREPARING
                || mediaPlayer.getStatus() == MediaPlayer.PlayerState.IDLE
                || mediaPlayer.getStatus() == MediaPlayer.PlayerState.ERROR) {
            return false;
        }

        if (playWhenReady) {
            // [Sky Italia] Play Preroll Ads
            //playIMAPreRoll();
            mediaPlayer.play();
        } else {
            mediaPlayer.pause();
        }
        return true;
    }

    @Override
    public boolean getPlayWhenReady() {
        return mediaPlayer.getStatus() == MediaPlayer.PlayerState.PLAYING
                || mediaPlayer.getStatus() == MediaPlayer.PlayerState.SUSPENDED;
    }

    @Override
    public void release() {
        if (heartbeat != null){
            heartbeat.detachPlayer();
            heartbeat = null;
        }
        if (mediaPlayer != null){
            try {
                mediaPlayer.release();
            } catch (IllegalStateException e)  {
                Timber.e("primetime player issue: %s", e);
            }
        }
        mediaPlayer = null;
        isBuffering = false;
    }

    @Override
    public PlayerConstants.PlayerEngine.Type getType() {
        return PT;
    }

    /**
     * This method is used to add
     *
     *     1. Playback event listener
     *     2. AD event listener
     *
     * to MediaPlayer base on listener's instance type
     * (i.e. Medium, Mini, Landscape, and new type in the future which is unknown for now)
     *
     * Each call will add both Playback event listener and AD event listener for only one
     * type of instance
     *
     * @param listener
     */
    @Override
    public void addListener(PlayerEngine.EventListener listener) {
        if (mediaPlayer == null) return;
        removeListener(listener);
        if (listener instanceof Medium){
            mediumPlaybackEventListener = getNewPlaybackEventListener(false,
                    null, null, null, null, null, listener);
            mediumAdEventListener = getNewAdEventListener(listener);
            mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, mediumPlaybackEventListener);
            mediaPlayer.addEventListener(MediaPlayer.Event.AD_PLAYBACK, mediumAdEventListener);
        } else if (listener instanceof Mini){
            miniPlaybackEventListener = getNewPlaybackEventListener(false,
                    null, null, null, null, null, listener);
            miniAdEventListener = getNewAdEventListener(listener);
            mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, miniPlaybackEventListener);
            mediaPlayer.addEventListener(MediaPlayer.Event.AD_PLAYBACK, miniAdEventListener);
        } else if (listener instanceof Landscape){
            landscapePlaybackEventListener = getNewPlaybackEventListener(false,
                    null, null, null, null, null, listener);
            landscapeAdEventListener = getNewAdEventListener(listener);
            mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, landscapePlaybackEventListener);
            mediaPlayer.addEventListener(MediaPlayer.Event.AD_PLAYBACK, landscapeAdEventListener);
        } else {
            unknownPlaybackEventListener = getNewPlaybackEventListener(false,
                    null, null, null, null, null, listener);
            unknownAdEventListener = getNewAdEventListener(listener);
            mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, unknownPlaybackEventListener);
            mediaPlayer.addEventListener(MediaPlayer.Event.AD_PLAYBACK, unknownAdEventListener);
        }
    }

    /**
     * This method is used to remove
     *
     *     1. Playback event listener
     *     2. AD event listener
     *
     * from MediaPlayer base on listener's instance type
     * (i.e. Medium, Mini, Landscape, and new type in the future which is unknown for now)
     *
     * Each call will remove both Playback event listener and AD event listener for only one
     * type of instance
     *
     * @param listener
     */
    @Override
    public void removeListener(PlayerEngine.EventListener listener) {
        if (mediaPlayer == null) return;
        if (listener instanceof Medium){
            if (mediumPlaybackEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, mediumPlaybackEventListener);
            }
            if (mediumAdEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.AD_PLAYBACK, mediumAdEventListener);
            }
        } else if (listener instanceof Mini){
            if (miniPlaybackEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, miniPlaybackEventListener);
            }
            if (miniAdEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.AD_PLAYBACK, miniAdEventListener);
            }
        } else if (listener instanceof Landscape){
            if (landscapePlaybackEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, landscapePlaybackEventListener);
            }
            if (landscapeAdEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.AD_PLAYBACK, landscapeAdEventListener);
            }
        } else {
            if (unknownPlaybackEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, unknownPlaybackEventListener);
            }
            if (unknownAdEventListener != null){
                mediaPlayer.removeEventListener(MediaPlayer.Event.AD_PLAYBACK, unknownAdEventListener);
            }
        }
    }

    @Override
    public long getCurrentPosition() {
        return mediaPlayer.getCurrentTime();
    }

    @Override
    public void seekTo(int currentWindow, long playbackPosition) {
        switch (mediaPlayer.getStatus()) {
            case PREPARED:
            case COMPLETE:
            case PAUSED:
            case PLAYING:
                mediaPlayer.seek(playbackPosition);
                break;
        }
    }

    @Override
    public int getPlaybackState() {
        return 0;
    }

    @Override
    public float getVolume() {
        Context context = mediaPlayer.getView().getContext();
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return currentVolume / (float) maxVolume * 100;
    }

    @Override
    public void setVolume(float v) {

        if (mediaPlayer.getStatus() == MediaPlayer.PlayerState.RELEASED){ return; }
        if (mediaPlayer.getStatus() == MediaPlayer.PlayerState.ERROR){ return; }

        mediaPlayer.setVolume((int)v);
    }

    @Override
    public void seekTo(Long time) {
        switch (mediaPlayer.getStatus()) {
            case PREPARED:
            case COMPLETE:
            case PAUSED:
            case PLAYING:
                mediaPlayer.seek(time);
                break;
        }
    }

    @Override
    public long getDuration() {
        TimeRange range = mediaPlayer.getPlaybackRange();
        return range.getDuration();
    }

    @Override
    public int getCurrentWindowIndex() {
        return 0;
    }

    @Override
    public Timeline getCurrentTimeline() {
        return null;
    }

    @Override
    public int getCurrentPeriodIndex() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.getStatus() == MediaPlayer.PlayerState.PLAYING;
    }

    @Override
    public boolean isSuspended() {
        return mediaPlayer.getStatus() == MediaPlayer.PlayerState.SUSPENDED;
    }

    @Override
    public boolean getConfig(PlayerConstants.PlayerEngine.Config config) {
        switch (config){
            case ALPHA_ANIMATION:
                return false;
            default:
                return false;
        }
    }

    @Override
    public void seekToDefaultPosition() {
        mediaPlayer.seek(MediaPlayer.LIVE_POINT);
    }

    @Override
    public boolean isFinished() {
        return mediaPlayer.getStatus() == MediaPlayer.PlayerState.COMPLETE || mediaPlayer.getStatus() == MediaPlayer.PlayerState.IDLE;
    }

    /**
     * This is to prevent the crash in TVSDK:
     * java.lang.NullPointerException: Attempt to invoke virtual method 'void com.adobe.ave.VideoEngineView.layout(int, int, int, int)' on a null object reference
     */
    @Override
    public void cleanPlayerView(PersistentPlayerView view) {
        if (view == null){ return; }
        if (view.getPrimetimePlayerView() == null){ return; }
        if (mediaPlayer == null){ return; }

        view.getPrimetimePlayerView().removeView(mediaPlayer.getView());
    }

    @Override
    public boolean isReady() {
        if (mediaPlayer == null){ return false; }
        return mediaPlayer.getStatus() == PREPARED;
    }

    @Override
    public void enableClosedCaption(boolean closedCaptionEnabled) {
        mediaPlayer.setCCVisibility(closedCaptionEnabled ? MediaPlayer.Visibility.VISIBLE : MediaPlayer.Visibility.INVISIBLE);
    }

    @Override
    public void log() {
        Timber.d("Player loading animation: player status: %s, isBuffering: %s",
                mediaPlayer.getStatus(),
                isBuffering);
    }

    @Override
    public boolean isAllowToShowLoadingProgressBar() {
        boolean isAllowToShow = false;
        switch (mediaPlayer.getStatus()){
            case INITIALIZING:
            case PREPARING:
                isAllowToShow = true;
                break;
            default:
                isAllowToShow = false;
                break;
        }
        if (isBuffering){
            isAllowToShow = true;
        }

        return isAllowToShow;
    }

    /**
     * This method is used to get player status
     * The player status will be used in kochava analytic disposable observer
     *
     * TODO: rewrite this method to not include any thing relates to Kochava
     *
     * @return player status
     */
    @Override
    public String getKochavaAnalyticPlayerStatus() {
        String playerStatusString = "";
        if (mediaPlayer != null){
            switch (mediaPlayer.getStatus()){
                case PLAYING:
                    playerStatusString = KochavaAnalyticPlayerManager.KOCHAVA_ANALYTICS_VIDEO_PLAYING;
                    break;
                case PAUSED:
                    playerStatusString = KochavaAnalyticPlayerManager.KOCHAVA_ANALYTICS_VIDEO_PAUSE;
                    break;
                case COMPLETE:
                case RELEASED:
                    playerStatusString = KochavaAnalyticPlayerManager.KOCHAVA_ANALYTICS_VIDEO_DONE;
                    break;
                default:
                    playerStatusString = "";
                    break;
            }
        }
        return playerStatusString;
    }

    private void setupPodbusters(Context context, MediaSource mediaSource) {
        adClient = PrimetimePlayerAd.createAdClient(context, mediaSource);
        mediaPlayer.registerAdClientFactory(adClient);
    }

    private void setupMidrolls(Context context, MediaSource mediaSource) {
        if (mediaSource != null
                && mediaSource.getAsset() != null
                && mediaSource.getAsset().getSsaiType().equalsIgnoreCase(PrimetimePlayerAd.ADOBE_CLIENT_ADS)){
            PrimetimePlayerAd.setUpMidrolls(context, mediaSource);
        }
    }

    /**
     * This method is used to check ad break type
     * If ad break type == PRE_ROLL, then set prerollAdDetected to true
     * Otherwise, set prerollAdDetected to false
     */
    private void kochavaOnTimelineUpdated() {
        KochavaAnalyticPlayerManager.setPrerollAdDetected(false);
        boolean hasMarker = mediaPlayer.getTimeline().timelineMarkers().hasNext();
        if (hasMarker){
            AdBreak.Type type;
            try {
                type = ((AdBreakPlacement) mediaPlayer.getTimeline().timelineMarkers().next())
                        .getAdBreak().getType();
            } catch (Exception e){
                type = null;
            }
            if (type == AdBreak.Type.PRE_ROLL){
                KochavaAnalyticPlayerManager.setPrerollAdDetected(true);
            }
        }
    }

    /**
     * This method is used to reset prerollAdDetected in KochavaAnalyticPlayerManager
     * when ad break is completed
     */
    private void kochavaOnAdBreakComplete() {
        KochavaAnalyticPlayerManager.setPrerollAdDetected(false);
    }

    /**
     * This method is used to generate either
     *
     *     1. prepare playback event listener for general operations
     *     2. new playback event listener for
     *        Medium, Mini, Landscape, and new type in the future which is unknown for now
     *
     * @param isGeneral
     * @param config
     * @param persistentPlayer
     * @param context
     * @param auth
     * @param mediaSource
     * @param listener
     * @return preparePlaybackEventListener, which is used for general operations
     *         view's PlaybackEventListener, which is used for each view's specific operations
     */
    private MediaPlayer.PlaybackEventListener getNewPlaybackEventListener(boolean isGeneral,
                                                                          Config config,
                                                                          PersistentPlayer persistentPlayer,
                                                                          Context context, Auth auth,
                                                                          MediaSource mediaSource,
                                                                          PlayerEngine.EventListener listener) {
        return new MediaPlayer.PlaybackEventListener() {
            @Override
            public void onPrepared() {
                // 1. General operation
                // 2. View's specific operation
                if (isGeneral && persistentPlayer != null && mediaSource != null){
                    persistentPlayer.log(this, "ONPREPARED ----- !!! %s", mediaSource.getTitle());
                } else {
                    Timber.d("persistentplayer ONPREPARED ----- !!!");
                }
            }

            @Override
            public void onUpdated() {

            }

            @Override
            public void onReplaceMediaPlayerItem() {

            }

            @Override
            public void onTimedMetadata(TimedMetadata timedMetadata) {
                // General operation
                if (isGeneral && adClient != null){
                    adClient.configureMidroll( timedMetadata );
                }
            }

            @Override
            public void onTimelineUpdated() {
                // General operation
                if (isGeneral){
                    kochavaOnTimelineUpdated();
                }
            }

            @Override
            public void onPlayStart() {
                // General operation
                if (isGeneral){
                    setupMidrolls(context, mediaSource);
                }
            }

            @Override
            public void onPlayComplete() {
                // View's specific operation
                if (!isGeneral && listener != null){
                    listener.onComplete();
                }
            }

            @Override
            public void onStateChanged(MediaPlayer.PlayerState playerState, MediaPlayerNotification mediaPlayerNotification) {
                // General operations
                if (isGeneral && persistentPlayer != null){
                    if (playerState == MediaPlayer.PlayerState.ERROR) {
                        HttpDataSource.InvalidResponseCodeException exception = new HttpDataSource.InvalidResponseCodeException(_403, null, null);
                        Behaviour.loadError(persistentPlayer, exception, auth, config);

                    } else if (playerState == MediaPlayer.PlayerState.INITIALIZED) {

                        try {
                            persistentPlayer.log(this, "prepareToPlay() preparing..");
                            mediaPlayer.prepareToPlay();
                        } catch (Exception e){
                            persistentPlayer.log(this, "prepareToPlay() state exception. can be ignored for now");
                        }

                    } else if (playerState == PREPARED) {
                        Behaviour.autoPlay(persistentPlayer, PrimetimePlayerEngine.this);
                    } else if (playerState == PLAYING) {
                        Behaviour.loadComplete(persistentPlayer);
                    }
                }
            }

            @Override
            public void onSizeAvailable(long l, long l1) {

            }

            @Override
            public void onProfileChanged(long l, long l1) {

            }

            @Override
            public void onRateSelected(float v) {

            }

            @Override
            public void onRatePlaying(float v) {

            }
        };
    }

    /**
     * This method is used to generate new ad event listener for
     * Medium, Mini, Landscape, and new type in the future which is unknown for now
     *
     * @param listener
     * @return AdPlaybackEventListener
     */
    private MediaPlayer.AdPlaybackEventListener getNewAdEventListener(PlayerEngine.EventListener listener) {
        return new MediaPlayer.AdPlaybackEventListener() {
            @Override
            public void onAdBreakStart(AdBreak adBreak) {
                if (listener != null){
                    listener.onAdBreakStart(adBreak);
                }
            }

            @Override
            public void onAdStart(AdBreak adBreak, Ad ad) {

            }

            @Override
            public void onAdProgress(AdBreak adBreak, Ad ad, int i) {

            }

            @Override
            public void onAdComplete(AdBreak adBreak, Ad ad) {

            }

            @Override
            public void onAdBreakComplete(AdBreak adBreak) {
                if (listener != null){
                    listener.onAdBreakComplete(adBreak);
                }
                kochavaOnAdBreakComplete();
            }

            @Override
            public void onAdBreakSkipped(AdBreak adBreak) {

            }

            @Override
            public void onAdClick(AdBreak adBreak, Ad ad, AdClick adClick) {

            }
        };
    }

    /**
     * This method is used to generate new qos event listener for
     * general use (so once for every stream is enough and no need to
     * be generated for each Medium, Mini, Landscape, and new type in the
     * future which is unknown for now)
     *
     * @return QOSEventListener
     */
    private MediaPlayer.QOSEventListener getNewQOSEventListener() {
        return new MediaPlayer.QOSEventListener() {
            @Override
            public void onBufferStart() {
                isBuffering = true;
            }

            @Override
            public void onBufferComplete() {
                isBuffering = false;
            }

            @Override
            public void onSeekStart() {

            }

            @Override
            public void onSeekComplete(long l) {

            }

            @Override
            public void onLoadInfo(LoadInfo loadInfo) {

            }

            @Override
            public void onOperationFailed(MediaPlayerNotification.Warning warning) {

            }
        };
    }
}