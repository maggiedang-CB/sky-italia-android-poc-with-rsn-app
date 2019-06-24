package com.nbcsports.regional.nbc_rsn.chromecast;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;

/**
 * Use this class for calls to ChromecastHelper when Chromecast
 * is turned off from the config
 */
public class NoChromecastHelper implements IChromecastHelper {

    @Override
    public boolean canShowCastButton() {
        // do not show cast button if ChromeCast not enabled
        return false;
    }

    @Override
    public void dismissBottomDialog() {
        // no implementation, chromecast not available so no need for this
    }

    @Override
    public boolean isLoadSameCurrentUrl(final RemoteMediaClient remoteMediaClient, final MediaSource mediaSource) {
        return true;
    }

    @Override
    public void setPersistentPlayer(final PersistentPlayer persistentPlayer) {
        // No implementation
    }

    @Override
    public void setAutoPlayListener(final AutoPlayListener autoPlayListener) {

    }

    @Override
    public void removeAutoPlayListener() {

    }

    @Override
    public ChromecastListener getChromecastListener() {
        return null;
    }

    @Override
    public CastSession getCurrentCastSession() {
        return null;
    }

    @Override
    public void handleAutoPlay(final CastSession castSession) {
        // No implementation, chromecast not available so no need for this
    }

    @Override
    public void playOnChromecast(final CastSession castSession, MediaSource mediaSource, final boolean playImmediately, ChromecastAuthorizationListener chromecastAuthorizationListener) {
        // No implementation, chromecast not available so no need for this
    }

    @Override
    public void loadVideo(Auth auth, RemoteMediaClient remoteMediaClient, MediaSource mediaSource, final boolean playImmediately) {
        // No implementation, chromecast not available so no need for this
    }

    @Override
    public boolean isStateConnected() {
        return false;
    }

    @Override
    public boolean isStateConnectedOrConnecting() {
        return false;
    }

    @Override
    public boolean isStateNotConnected() {
        return true;
    }

    @Override
    public boolean isStateNoDevicesAvailable() {
        return true;
    }

    @Override
    public void onSessionEnding(final CastSession castSession) {
        // no implementation, chromecast not available so no need for this
    }

    @Override
    public void onSessionEnded() {
        // no implementation, chromecast not available so no need for this
    }

    @Override
    public void setStandalonePlayVisible() {
        // no implementation, chromecast not available so no need for this
    }

    @Override
    public void setStandalonePlayInvisible() {
        // no implementation, chromecast not available so no need for this
    }

    @Override
    public void pauseLocalPlayback() {

    }

    @Override
    public void setClosedCaptions(final boolean enable) {

    }

    @Override
    public void setTrackControlsVisibility(final boolean isPlayPauseVisible) {

    }

    @Override
    public boolean isFreePreview(MediaSource mediaSource) {
        return false;
    }

    @Override
    public boolean isUserAuthenticated() {
        return false;
    }

    @Override
    public boolean isFreeLiveAsset(MediaSource mediaSource) {
        return false;
    }

    @Override
    public void initClosedCaptions(CastSession castSession) {

    }

    @Override
    public void onLogout() {
        // no implementation, chromecast not available so no need for this
    }
}
