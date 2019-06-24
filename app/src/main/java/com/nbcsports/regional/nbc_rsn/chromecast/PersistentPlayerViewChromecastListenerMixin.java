package com.nbcsports.regional.nbc_rsn.chromecast;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.chromecast.IChromecastHelper.AutoPlayListener;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerBottomSheet;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnKebabClickListener;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PersistentPlayerViewChromecastListenerMixin extends SessionManagerListener<CastSession>,
        ChromecastMixinsBase, AutoPlayListener {

    ImageView getChromecastImage();
    View getChromecastLayout();
    ImageView getChromecastPlayBtn();
    ImageView getChromecastPauseBtn();
    ImageButton getChromecastKebabBtn();
    IChromecastHelper getChromecastHelper();
    MediaSource getChromecastMediaSource();
    PersistentPlayer getChromecastPersistentPlayer();
    RemoteMediaClient.Callback getChromecastCallBack();
    void setChromecastCallBack(RemoteMediaClient.Callback callBack);

    /**
     * Initialization, call it when the view is attached to the window, or onStart
     *
     * Using getChromecastMediaSource() instead of persistentPlayer.getMediaSource()
     * for Medium view, because in Medium view,
     * the PersistentPlayer's MediaSource is updated after initChromecast(...) is called
     * in order to prevent delay on showing the correct button (Play/Pause)
     *
     * @param bottomSheet
     * @param context
     */
    default void initChromecast(PersistentPlayerBottomSheet bottomSheet, Context context) {

        // Check if chromecast is created, if not, then create one
        if (getChromecastCallBack() == null){
            setChromecastCallBack(createNewChromecastCallBack());
        }
        cleanUpChromecastListener();
        addSessionManagerListener();
        addRemoteMediaClientCallback(getChromecastCallBack());
        getChromecastHelper().setAutoPlayListener(this);
        getChromecastKebabBtn().setOnClickListener(new OnKebabClickListener(bottomSheet, context));
        getChromecastPlayBtn().setOnClickListener(v -> onPlayClicked());
        getChromecastPauseBtn().setOnClickListener(v -> onPauseClicked());

        if (getChromecastHelper() != null) {
            // Check if any chromecast device is connected, if so, then show chromecast layout
            showHideChromecastLayer(getChromecastHelper().isStateConnected());
            if (getChromecastHelper().isStateConnected() &&
                    getChromecastHelper().getCurrentCastSession() != null &&
                    getChromecastHelper().getCurrentCastSession().getRemoteMediaClient() != null) {
                boolean loadingSameVideo = false;
                if (getChromecastMediaSource() != null){
                    loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                            getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                            getChromecastMediaSource());
                }
                else if (getChromecastPersistentPlayer() != null){
                    loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                            getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                            getChromecastPersistentPlayer().getMediaSource());
                }
                remoteVideoPlayingActionHandling(this::showChromecastPauseBtn,
                        this::showChromecastPlayBtn, this::showChromecastImage, loadingSameVideo);
                // Initialize closed captions
                getChromecastHelper().initClosedCaptions(getChromecastHelper().getCurrentCastSession());
            }
        }
    }

    /**
     * This method is used to check the current status of casting media
     * and show related button if appropriate
     *
     * @param castSession
     */
    @Override
    default void onAutoPlay(CastSession castSession, PersistentPlayer persistentPlayer) {
        boolean loadingSameVideo = false;
        if (getChromecastMediaSource() != null){
            loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                    getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                    getChromecastMediaSource());
        } else if (getChromecastPersistentPlayer() != null){
            loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                    getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                    getChromecastPersistentPlayer().getMediaSource());
        }
        remoteVideoPlayingActionHandling(this::showChromecastPauseBtn,
                this::showChromecastPlayBtn, this::showChromecastImage, loadingSameVideo);
    }

    default void onPlayClicked() {
        if (getChromecastHelper() != null
                && getChromecastMediaSource() != null
                && getChromecastMediaSource().getLive()
                && (getChromecastHelper().isFreePreview(getChromecastMediaSource()) || !getChromecastHelper().isUserAuthenticated())
                && !getChromecastHelper().isFreeLiveAsset(getChromecastMediaSource())){
            NotificationsManagerKt.INSTANCE.showChromecastAssetError();
            showChromecastPlayBtn();
            return;
        } else if (getChromecastHelper() != null
                && getChromecastPersistentPlayer() != null
                && getChromecastPersistentPlayer().getMediaSource() != null
                && getChromecastPersistentPlayer().getMediaSource().getLive()
                && (getChromecastHelper().isFreePreview(getChromecastPersistentPlayer().getMediaSource()) || !getChromecastHelper().isUserAuthenticated())
                && !getChromecastHelper().isFreeLiveAsset(getChromecastPersistentPlayer().getMediaSource())){
            NotificationsManagerKt.INSTANCE.showChromecastAssetError();
            showChromecastPlayBtn();
            return;
        }
        // If the videos on the app and chromecast are the same, then resume playback on chromecast
        // Otherwise, load new media to chromecast
        if (getChromecastHelper() != null &&
                getChromecastHelper().getCurrentCastSession() != null &&
                getChromecastHelper().getCurrentCastSession().getRemoteMediaClient() != null) {

            showChromecastImage();
            CastSession castSession = getChromecastHelper().getCurrentCastSession();

            // Check if the videos are the same
            boolean loadingSameVideo = false;
            if (getChromecastMediaSource() != null){
                loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                        getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                        getChromecastMediaSource());
            } else if (getChromecastPersistentPlayer() != null){
                loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                        getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                        getChromecastPersistentPlayer().getMediaSource());
            }
            if (loadingSameVideo) {
                if (getChromecastHelper().isVideoFinished()
                        || getChromecastHelper().isVideoCanceled()
                        || getChromecastHelper().isVideoInterrupted()
                        || getChromecastHelper().isVideoError()) {
                    // Check if media source exists
                    if (getChromecastMediaSource() == null && (getChromecastPersistentPlayer() == null || getChromecastPersistentPlayer().getMediaSource() == null)){
                        NotificationsManagerKt.INSTANCE.showChromecastAssetError();
                        showChromecastPlayBtn();
                        return;
                    }
                    // Clone media source
                    MediaSource cloneMediaSource;
                    if (getChromecastMediaSource() != null){
                        cloneMediaSource = new MediaSource(getChromecastMediaSource());
                    } else {
                        cloneMediaSource = new MediaSource(getChromecastPersistentPlayer().getMediaSource());
                        // If media source has asset, it means the media source is not vod
                        // Then the media source's stream url of PersistentPlayer should be replace by
                        // the NBC tokenlized url.
                        // So need to change it back to the original AndroidStreamUrl
                        if (getChromecastPersistentPlayer().getMediaSource().getAsset() != null
                                && getChromecastPersistentPlayer().getMediaSource().getAsset().getAndroidStreamUrl() != null
                                && !getChromecastPersistentPlayer().getMediaSource().getAsset().getAndroidStreamUrl().isEmpty()){
                            cloneMediaSource.setStreamUrl(getChromecastPersistentPlayer().getMediaSource().getAsset().getAndroidStreamUrl());
                        }
                    }
                    // Cast to chromecast
                    getChromecastHelper().playOnChromecast(castSession, cloneMediaSource, true, new ChromecastAuthorizationListener() {
                        @Override
                        public void onAuthorizationSuccess(@NotNull Auth auth) {
                            getChromecastHelper().loadVideo(auth, castSession.getRemoteMediaClient(), cloneMediaSource, true);
                            getChromecastHelper().initClosedCaptions(castSession);
                        }

                        @Override
                        public void onAuthorizationFailure(@Nullable Throwable e) {
                            NotificationsManagerKt.INSTANCE.showChromecastAssetError();
                            showChromecastPlayBtn();
                        }
                    });
                } else {
                    castSession.getRemoteMediaClient().play();
                }
            } else {
                castSession.getRemoteMediaClient().stop();
                // Check if media source exists
                if (getChromecastMediaSource() == null && (getChromecastPersistentPlayer() == null || getChromecastPersistentPlayer().getMediaSource() == null)){
                    NotificationsManagerKt.INSTANCE.showChromecastAssetError();
                    showChromecastPlayBtn();
                    return;
                }
                // Clone media source
                MediaSource cloneMediaSource;
                if (getChromecastMediaSource() != null){
                    cloneMediaSource = new MediaSource(getChromecastMediaSource());
                } else {
                    cloneMediaSource = new MediaSource(getChromecastPersistentPlayer().getMediaSource());
                    // If media source has asset, it means the media source is not vod
                    // Then the media source's stream url of PersistentPlayer should be replace by
                    // the NBC tokenlized url.
                    // So need to change it back to the original AndroidStreamUrl
                    if (getChromecastPersistentPlayer().getMediaSource().getAsset() != null
                            && getChromecastPersistentPlayer().getMediaSource().getAsset().getAndroidStreamUrl() != null
                            && !getChromecastPersistentPlayer().getMediaSource().getAsset().getAndroidStreamUrl().isEmpty()){
                        cloneMediaSource.setStreamUrl(getChromecastPersistentPlayer().getMediaSource().getAsset().getAndroidStreamUrl());
                    }
                }
                // Cast to chromecast
                getChromecastHelper().playOnChromecast(castSession, cloneMediaSource, true, new ChromecastAuthorizationListener() {
                    @Override
                    public void onAuthorizationSuccess(@NotNull Auth auth) {
                        getChromecastHelper().loadVideo(auth, castSession.getRemoteMediaClient(), cloneMediaSource, true);
                        getChromecastHelper().initClosedCaptions(castSession);
                    }

                    @Override
                    public void onAuthorizationFailure(@Nullable Throwable e) {
                        NotificationsManagerKt.INSTANCE.showChromecastAssetError();
                        showChromecastPlayBtn();
                    }
                });
            }
        } else {
            NotificationsManagerKt.INSTANCE.showChromecastAssetError();
            showChromecastPlayBtn();
        }
    }

    default void onPauseClicked() {
        // Pause RemoteMediaClient playback
        if (getChromecastHelper()!=null &&
                getChromecastHelper().getCurrentCastSession() != null &&
                getChromecastHelper().getCurrentCastSession().getRemoteMediaClient() != null){
            getChromecastHelper().getCurrentCastSession().getRemoteMediaClient().pause();
        }
    }

    /**
     * This method is used to handle chromecast playback control buttons appear disappear logic
     *
     * @param pauseButtonRunnable this runnable must implement the action to take if remote is playing
     * @param playButtonRunnable this runnable must implement the action to take if remote is paused or idle
     * @param chromecastImageRunnable this runnable implement the action to take if remote is buffering or unknown
     */
    default void remoteVideoPlayingActionHandling(@NonNull Runnable pauseButtonRunnable,
                                                  @NonNull Runnable playButtonRunnable,
                                                  @NonNull Runnable chromecastImageRunnable,
                                                  boolean loadingSame) {
        if (getChromecastHelper().getCurrentCastSession() != null
                && getChromecastHelper().isRemotePlaying(getChromecastHelper().getCurrentCastSession().getRemoteMediaClient())) {
            if (loadingSame) {
                // Check status of casting media
                RemoteMediaClient remoteMediaClient = getChromecastHelper()
                        .getCurrentCastSession().getRemoteMediaClient();
                if (remoteMediaClient != null
                        && remoteMediaClient.getMediaStatus() != null){
                    switch (remoteMediaClient.getMediaStatus().getPlayerState()) {
                        case MediaStatus.PLAYER_STATE_PLAYING:
                            pauseButtonRunnable.run();
                            break;
                        case MediaStatus.PLAYER_STATE_PAUSED:
                        case MediaStatus.PLAYER_STATE_IDLE:
                            playButtonRunnable.run();
                            break;
                        case MediaStatus.PLAYER_STATE_BUFFERING:
                        case MediaStatus.PLAYER_STATE_UNKNOWN:
                            chromecastImageRunnable.run();
                            break;
                    }
                } else {
                    playButtonRunnable.run();
                }
            } else {
                playButtonRunnable.run();
            }
        } else {
            playButtonRunnable.run();
        }
    }

    default void showHideChromecastLayer(boolean show) {
        if (getChromecastLayout() != null){
            getChromecastLayout().setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    default void showChromecastPauseBtn() {
        if (getChromecastPauseBtn() != null){
            getChromecastPauseBtn().setVisibility(View.VISIBLE);
        }
        if (getChromecastImage() != null) {
            getChromecastImage().setVisibility(View.GONE);
        }
        if (getChromecastPlayBtn() != null){
            getChromecastPlayBtn().setVisibility(View.GONE);
        }
    }

    default void showChromecastPlayBtn() {
        if (getChromecastPlayBtn() != null){
            getChromecastPlayBtn().setVisibility(View.VISIBLE);
        }
        if (getChromecastImage() != null){
            getChromecastImage().setVisibility(View.GONE);
        }
        if (getChromecastPauseBtn() != null){
            getChromecastPauseBtn().setVisibility(View.GONE);
        }
    }

    default void showChromecastImage() {
        if (getChromecastImage() != null){
            getChromecastImage().setVisibility(View.VISIBLE);
        }
        if (getChromecastPauseBtn() != null){
            getChromecastPauseBtn().setVisibility(View.GONE);
        }
        if (getChromecastPlayBtn() != null){
            getChromecastPlayBtn().setVisibility(View.GONE);
        }
    }

    @Override
    default SessionManagerListener<CastSession> getChromecastListener() {
        return this;
    }

    default void chromecastSessionStarting(final CastSession castSession) {
        // Show related chromecast layout
        showHideChromecastLayer(true);
        showChromecastImage();
    }

    @Override
    default void onSessionStarting(final CastSession castSession) {
        chromecastSessionStarting(castSession);
    }

    @Override
    default void onSessionStarted(final CastSession castSession, final String s) {
        // Check if RemoteMediaClient.Callback is created, if not, then create one
        if (getChromecastCallBack() == null){
            setChromecastCallBack(createNewChromecastCallBack());
        }
        // Remove RemoteMediaClient.Callback
        removeRemoteMediaClientCallback(getChromecastCallBack());
        // Add RemoteMediaClient.Callback again
        addRemoteMediaClientCallback(getChromecastCallBack());
    }

    @Override
    default void onSessionStartFailed(final CastSession castSession, final int i) {
        removeRemoteMediaClientCallback(getChromecastCallBack());
        showHideChromecastLayer(false);
        // Show chromecast failed notification banner
        NotificationsManagerKt.INSTANCE.showChromecastFailed(CastStatusCodes.getStatusCodeString(i));
    }

    @Override
    default void onSessionEnding(final CastSession castSession) {
        removeRemoteMediaClientCallback(getChromecastCallBack());
        showChromecastImage();
    }

    @Override
    default void onSessionEnded(final CastSession castSession, final int i) {
        showHideChromecastLayer(false);
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
    default void onSessionResuming(final CastSession castSession, final String s) {
        chromecastSessionStarting(castSession);
    }

    @Override
    default void onSessionResumed(final CastSession castSession, final boolean b) {
        // Check if RemoteMediaClient.Callback is created, if not, then create one
        if (getChromecastCallBack() == null){
            setChromecastCallBack(createNewChromecastCallBack());
        }
        // Remove RemoteMediaClient.Callback
        removeRemoteMediaClientCallback(getChromecastCallBack());
        // Add RemoteMediaClient.Callback again
        addRemoteMediaClientCallback(getChromecastCallBack());
    }

    @Override
    default void onSessionResumeFailed(final CastSession castSession, final int i) {
        removeRemoteMediaClientCallback(getChromecastCallBack());
        showHideChromecastLayer(false);
        // Show chromecast failed notification banner
        NotificationsManagerKt.INSTANCE.showChromecastFailed(CastStatusCodes.getStatusCodeString(i));
    }

    @Override
    default void onSessionSuspended(final CastSession castSession, final int i) {
        removeRemoteMediaClientCallback(getChromecastCallBack());
        showHideChromecastLayer(false);
        // 1. Show chromecast failed notification banner with CAUSE_SERVICE_DISCONNECTED
        // 2. Show chromecast failed notification banner with CAUSE_NETWORK_LOST
        // Base on GoogleApiClient.ConnectionCallbacks.CAUSE_*.
        if (i == 1){
            NotificationsManagerKt.INSTANCE.showChromecastFailed("CAUSE_SERVICE_DISCONNECTED");
        } else if (i == 2){
            NotificationsManagerKt.INSTANCE.showChromecastFailed("CAUSE_NETWORK_LOST");
        }
    }

    default RemoteMediaClient.Callback createNewChromecastCallBack() {
        return new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                super.onStatusUpdated();
                if (getChromecastHelper() != null
                        && getChromecastHelper().getCurrentCastSession() != null
                        && getChromecastHelper().getCurrentCastSession().getRemoteMediaClient() != null
                        && getChromecastHelper().getCurrentCastSession().getRemoteMediaClient().getMediaStatus() != null) {
                    boolean loadingSameVideo = false;
                    if (getChromecastMediaSource() != null){
                        loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                                getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                                getChromecastMediaSource());
                    } else if (getChromecastPersistentPlayer() != null){
                        loadingSameVideo = getChromecastHelper().isLoadSameCurrentUrl(
                                getChromecastHelper().getCurrentCastSession().getRemoteMediaClient(),
                                getChromecastPersistentPlayer().getMediaSource());
                    }
                    if (loadingSameVideo){
                        switch (getChromecastHelper().getCurrentCastSession()
                                .getRemoteMediaClient().getMediaStatus().getPlayerState()){
                            case MediaStatus.PLAYER_STATE_PLAYING:
                                showChromecastPauseBtn();
                                break;
                            case MediaStatus.PLAYER_STATE_PAUSED:
                                showChromecastPlayBtn();
                                break;
                            case MediaStatus.PLAYER_STATE_IDLE:
                                showChromecastPlayBtn();
                                break;
                            case MediaStatus.PLAYER_STATE_BUFFERING:
                                showChromecastImage();
                                break;
                            case MediaStatus.PLAYER_STATE_UNKNOWN:
                                showChromecastImage();
                                break;
                        }
                    }
                    else {
                        showChromecastPlayBtn();
                    }
                }
            }

            @Override
            public void onMetadataUpdated() {
                super.onMetadataUpdated();
            }

            @Override
            public void onQueueStatusUpdated() {
                super.onQueueStatusUpdated();
            }

            @Override
            public void onPreloadStatusUpdated() {
                super.onPreloadStatusUpdated();
            }

            @Override
            public void onSendingRemoteMediaRequest() {
                super.onSendingRemoteMediaRequest();
            }

            @Override
            public void onAdBreakStatusUpdated() {
                super.onAdBreakStatusUpdated();
            }
        };
    }

    default void cleanUpChromecastListener() {
        removeSessionManagerListener();
        removeRemoteMediaClientCallback(getChromecastCallBack());
        getChromecastHelper().removeAutoPlayListener();
    }
}
