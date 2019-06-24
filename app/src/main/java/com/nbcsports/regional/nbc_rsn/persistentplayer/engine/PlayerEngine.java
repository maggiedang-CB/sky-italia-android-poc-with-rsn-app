package com.nbcsports.regional.nbc_rsn.persistentplayer.engine;

import android.content.Context;

import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

public class PlayerEngine {

    public static PlayerEngine.Interface newInstance(PlayerConstants.PlayerEngine.Type type, Context context){
        PlayerEngine.Interface engine = null;
        switch (type) {
            case EXO:
                engine = newExoPlayerInstance(context);
                break;
            case PT:
                engine = newPrimetimePlayerInstance(context);
                break;
        }
        return engine;
    }

    public static PlayerEngine.Interface newExoPlayerInstance(Context context) {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();

        SimpleExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        return new ExoPlayerEngine(exoPlayer);
    }

    public static PlayerEngine.Interface newPrimetimePlayerInstance(Context context) {
        return new PrimetimePlayerEngine(context);
    }

    public static void switchTarget(PlayerEngine.Interface playerEngine, PersistentPlayerContract.View oldView, PersistentPlayerContract.View newView) {
        if (playerEngine != null){
            playerEngine.switchTarget(oldView, newView);
        }
    }

    public interface Interface {

        PlayerConstants.PlayerEngine.Type getType();

        // [Sky Italia]
        void pauseMediaPlayer();

        void playMediaPlayer();

        void addListener(PlayerEngine.EventListener listener);

        void removeListener(PlayerEngine.EventListener listener);

        long getCurrentPosition();

        boolean setPlayWhenReady(boolean playWhenReady);

        boolean getPlayWhenReady();

        void seekTo(int currentWindow, long playbackPosition);

        void release();

        int getPlaybackState();

        float getVolume();

        void setVolume(float v);

        void seekTo(Long time);

        void switchTarget(PersistentPlayerContract.View mediumView, PersistentPlayerContract.View miniView);

        void prepare(String mediaUrl, PersistentPlayer persistentPlayer, Context context, Auth auth, MediaSource mediaSource);

        long getDuration();

        int getCurrentWindowIndex();

        Timeline getCurrentTimeline();

        int getCurrentPeriodIndex();

        boolean isPlaying();

        boolean isSuspended();

        boolean getConfig(PlayerConstants.PlayerEngine.Config config);

        void seekToDefaultPosition();

        boolean isFinished();

        void cleanPlayerView(PersistentPlayerView view);

        boolean isReady();

        void enableClosedCaption(boolean closedCaptionEnabled);

        void log();

        boolean isAllowToShowLoadingProgressBar();

        String getKochavaAnalyticPlayerStatus();
    }

    public interface EventListener {

        void onTimelineChanged(Timeline timeline, Object manifest, int reason);

        void onComplete();

        void onAdBreakStart(AdBreak adBreak);

        void onAdBreakComplete(AdBreak adBreak);
    }
}
