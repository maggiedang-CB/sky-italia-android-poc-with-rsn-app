package com.nbcsports.regional.nbc_rsn.persistentplayer.engine;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerMediaSourceEventListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

import static com.google.android.exoplayer2.Player.STATE_ENDED;

public class ExoPlayerEngine implements PlayerEngine.Interface {

    private final SimpleExoPlayer exoPlayer;
    private Player.EventListener eventListener;

    public ExoPlayerEngine(SimpleExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    // [Sky Italia]
    @Override
    public void pauseMediaPlayer() {

    }
    @Override
    public void playMediaPlayer() {

    }

    @Override
    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public boolean setPlayWhenReady(boolean playWhenReady) {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(playWhenReady);
            return true;
        }
        return false;
    }

    @Override
    public boolean getPlayWhenReady() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public void seekTo(int currentWindow, long playbackPosition) {
        exoPlayer.seekTo(currentWindow, playbackPosition);
    }

    @Override
    public void seekTo(Long time) {
        exoPlayer.seekTo(time);
    }

    @Override
    public void switchTarget(PersistentPlayerContract.View oldView, PersistentPlayerContract.View newView) {
        PlayerView old;
        if (oldView == null){
            old = null;
        } else {
            old = oldView.getPlayerView();
        }
        PlayerView.switchTargetView(exoPlayer, old, newView.getPlayerView());
    }

    @Override
    public void prepare(String mediaUrl, PersistentPlayer persistentPlayer, Context context, Auth auth, com.nbcsports.regional.nbc_rsn.common.MediaSource mediaSource) {
        PersistentPlayerMediaSourceEventListener listener = new PersistentPlayerMediaSourceEventListener(persistentPlayer);
        HlsMediaSource hls = getHlsMediaSource(context, mediaUrl, listener);
        exoPlayer.prepare(hls, true, false);
    }

    @Override
    public void release() {
        exoPlayer.release();
    }

    @Override
    public long getDuration() {
        return exoPlayer.getDuration();
    }

    @Override
    public int getCurrentWindowIndex() {
        return exoPlayer.getCurrentWindowIndex();
    }

    @Override
    public Timeline getCurrentTimeline() {
        return exoPlayer.getCurrentTimeline();
    }

    @Override
    public int getCurrentPeriodIndex() {
        return exoPlayer.getCurrentPeriodIndex();
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlaybackState() == Player.STATE_READY && getPlayWhenReady();
    }

    @Override
    public boolean isSuspended() {
        // Need to check what state equal to Suspended
        return false;
    }

    @Override
    public boolean getConfig(PlayerConstants.PlayerEngine.Config config) {
        switch (config){
            case ALPHA_ANIMATION:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void seekToDefaultPosition() {
        exoPlayer.seekToDefaultPosition();
    }

    @Override
    public boolean isFinished() {
        return getPlaybackState() == Player.STATE_ENDED || getPlaybackState() == Player.STATE_IDLE;
    }

    @Override
    public void cleanPlayerView(PersistentPlayerView view) {
        
    }

    @Override
    public boolean isReady() {
        return exoPlayer.getPlaybackState() == Player.STATE_READY;
    }

    @Override
    public void enableClosedCaption(boolean closedCaptionEnabled) {

    }

    @Override
    public void log() {

    }

    @Override
    public boolean isAllowToShowLoadingProgressBar() {
        // TODO: implement when to show the loading animation here
        return false;
    }

    @Override
    public String getKochavaAnalyticPlayerStatus() {
        // TODO: return kochava status string base on player status
        return "";
    }

    @Override
    public int getPlaybackState() {
        return exoPlayer.getPlaybackState();
    }

    @Override
    public float getVolume() {
        return exoPlayer.getVolume();
    }

    @Override
    public void setVolume(float v) {
        exoPlayer.setVolume(v);
    }

    @Override
    public PlayerConstants.PlayerEngine.Type getType() {
        return PlayerConstants.PlayerEngine.Type.EXO;
    }

    public void addListener(PlayerEngine.EventListener listener){
        eventListener = new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                listener.onTimelineChanged(timeline, manifest, reason);
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case STATE_ENDED:
                        listener.onComplete();
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        };
        exoPlayer.addListener(eventListener);
    }

    @Override
    public void removeListener(PlayerEngine.EventListener listener) {
        exoPlayer.removeListener(eventListener);
    }

    public static HlsMediaSource getHlsMediaSource(Context context, String hlsVideoUrl, MediaSourceEventListener listener){
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Exo2"), defaultBandwidthMeter);
        Handler mainHandler = new Handler();

        HlsMediaSource hlsMediaSource = new HlsMediaSource(Uri.parse(hlsVideoUrl),
                dataSourceFactory, mainHandler, listener);
        return hlsMediaSource;
    }
}
