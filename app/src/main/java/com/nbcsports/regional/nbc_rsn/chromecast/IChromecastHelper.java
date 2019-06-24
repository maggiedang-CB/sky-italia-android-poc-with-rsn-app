package com.nbcsports.regional.nbc_rsn.chromecast;

import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;

public interface IChromecastHelper {

    boolean canShowCastButton();
    void dismissBottomDialog();
    ChromecastListener getChromecastListener(); // always check for null when using this method
    CastSession getCurrentCastSession();
    void handleAutoPlay(CastSession castSession);
    void playOnChromecast(CastSession castSession, MediaSource mediaSource, final boolean playImmediately, ChromecastAuthorizationListener chromecastAuthorizationListener);
    void loadVideo(Auth auth, RemoteMediaClient remoteMediaClient, MediaSource mediaSource, final boolean playImmediately);
    boolean isStateConnected();
    boolean isStateConnectedOrConnecting();
    boolean isStateNotConnected();
    boolean isStateNoDevicesAvailable();
    void onSessionEnding(CastSession castSession);
    void onSessionEnded();
    void setStandalonePlayVisible();
    void setStandalonePlayInvisible();
    void pauseLocalPlayback();
    void setClosedCaptions(final boolean enable);
    void setTrackControlsVisibility(final boolean isPlayPauseVisible);
    boolean isLoadSameCurrentUrl(RemoteMediaClient remoteMediaClient, MediaSource mediaSource);
    void setPersistentPlayer(PersistentPlayer persistentPlayer);
    void setAutoPlayListener(final AutoPlayListener autoPlayListener);
    void removeAutoPlayListener();
    boolean isFreePreview(MediaSource mediaSource);
    boolean isUserAuthenticated();
    boolean isFreeLiveAsset(MediaSource mediaSource);
    void initClosedCaptions(CastSession castSession);
    void onLogout();

    default boolean isLocalPlaying(PersistentPlayer pp) {
        return pp.isPlaying();
    }

    default boolean isRemotePlaying(RemoteMediaClient remoteMediaClient) {
        return remoteMediaClient.isPlaying() ||
                remoteMediaClient.isBuffering() ||
                remoteMediaClient.isLoadingNextItem() ||
                remoteMediaClient.isPlayingAd() ||
                remoteMediaClient.isLiveStream();
    }

    /**
     * @return true if the video finished playing remotely
     */
    default boolean isVideoFinished() {
        if (getCurrentCastSession() == null) return true;
        if (getCurrentCastSession().getRemoteMediaClient() == null) return true;
        return getCurrentCastSession().getRemoteMediaClient().getIdleReason() == MediaStatus.IDLE_REASON_FINISHED;
    }

    /**
     * @return true if the video canceled playing remotely
     */
    default boolean isVideoCanceled() {
        if (getCurrentCastSession() == null) return true;
        if (getCurrentCastSession().getRemoteMediaClient() == null) return true;
        return getCurrentCastSession().getRemoteMediaClient().getIdleReason() == MediaStatus.IDLE_REASON_CANCELED;
    }

    /**
     * @return true if the video interrupted playing remotely
     */
    default boolean isVideoInterrupted() {
        if (getCurrentCastSession() == null) return true;
        if (getCurrentCastSession().getRemoteMediaClient() == null) return true;
        return getCurrentCastSession().getRemoteMediaClient().getIdleReason() == MediaStatus.IDLE_REASON_INTERRUPTED;
    }

    /**
     * @return true if the video error playing remotely
     */
    default boolean isVideoError() {
        if (getCurrentCastSession() == null) return true;
        if (getCurrentCastSession().getRemoteMediaClient() == null) return true;
        return getCurrentCastSession().getRemoteMediaClient().getIdleReason() == MediaStatus.IDLE_REASON_ERROR;
    }

    /**
     * Checks if chromecast is enabled from the config
     * @param mainActivity
     * @return true if chromecast is enabled
     *         false, otherwise
     */
    static boolean isChromecastEnabled(MainActivity mainActivity) {
        if (mainActivity == null) return false;
        if (mainActivity.getConfig() == null) return false;
        if (mainActivity.getConfig().getChromecast() == null) return false;
        return mainActivity.getConfig().getChromecast().getEnabled();
    }

    interface AutoPlayListener {
        void onAutoPlay(CastSession castSession, PersistentPlayer persistentPlayer);
    }
}
