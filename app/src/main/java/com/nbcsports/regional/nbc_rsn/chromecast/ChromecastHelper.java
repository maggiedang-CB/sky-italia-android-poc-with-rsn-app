package com.nbcsports.regional.nbc_rsn.chromecast;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;

import java.util.concurrent.TimeUnit;

import com.adobe.mobile.MobileServices;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.Constants;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.ads.Helper;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ChromecastHelper implements IChromecastHelper {

    private final StreamAuthenticationContract.Presenter streamAuthentication;
    private MainActivity mainActivity;
    private CastContext castContext;
    /* package visible*/ PersistentPlayer persistentPlayer;
    private ChromecastListener chromecastListener;
    private Disposable timerDisposable;
    private RemoteMediaClientProgressListener remoteMediaClientProgressListener;
    /* package visible*/ ChromecastClosedCaptions chromecastClosedCaptions;
    private AutoPlayListener autoPlayListener;

    public ChromecastHelper(Activity activity, PersistentPlayer persistentPlayer,
            StreamAuthenticationContract.Presenter streamAuthentication) {
        if (!(activity instanceof PersistentPlayerContract.Main.View)) {
            throw new RuntimeException("activity must implement PersistentPlayerContract.Main.View");
        }

        castContext = CastContext.getSharedInstance(activity.getApplicationContext());
        this.persistentPlayer = persistentPlayer;
        chromecastClosedCaptions = new ChromecastClosedCaptions();
        this.streamAuthentication = streamAuthentication;
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        }
        initChromecastListener();
    }

    public void setPersistentPlayer(final PersistentPlayer persistentPlayer) {
        this.persistentPlayer = persistentPlayer;
    }

    /* package visible */
    static MediaInfo convertToMediaInfo(MediaSource mediaSource, Context context, Config config, Auth auth) {

        Timber.d("This is the enter point: convertToMediaInfo mediaSource %s", mediaSource);

        // Set up MediaMetadata
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mediaSource.getTitle());
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, getSportName(mediaSource));
        if (mediaSource.getAsset() != null){
            movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mediaSource.getAsset().getInfo());
        }
        if (mediaSource.getImage() != null && !mediaSource.getImage().isEmpty()){
            movieMetadata.addImage(new WebImage(Uri.parse(
                    config.getImagesBaseUrl()
                            +mediaSource.getImage()
                            +"_640x360.jpg")));
        } else {
            movieMetadata.addImage(new WebImage(Uri.parse(mediaSource.getBackupImage())));
        }

        // Set up CustomData
        JSONObject customDataJsonObject = new JSONObject();
        try {
            customDataJsonObject.put(Constants.CC_PID_KEY, mediaSource.getPid());
            customDataJsonObject.put(Constants.CC_ID_KEY, mediaSource.getId());
            customDataJsonObject.put(Constants.CC_IS_LIVE_KEY, mediaSource.getLive());
            // Empty for now since it is missing from config
            customDataJsonObject.put(Constants.CC_RESOURCE_KEY, "");
            customDataJsonObject.put(Constants.CC_PLATFORM_KEY, "Android: Mobile");
            customDataJsonObject.put(Constants.CC_ADOBE_MID_KEY, MobileServices.getMarketingCloudVisitorID());
            // Set is free default to 0
            customDataJsonObject.put(Constants.CC_IS_FREE_KEY, 0);
            if (auth != null && auth.getAuthNToken() != null && auth.getAuthZToken() != null) {
                customDataJsonObject.put(Constants.CC_MVPD_ID_KEY, auth.getAuthNToken().getMvpd());
                customDataJsonObject.put(Constants.CC_MVPD_NAME_KEY, auth.getAuthZToken().getMvpd());
            }
            if (mediaSource.getAsset() != null){
                customDataJsonObject.put(Constants.CC_START_KEY, mediaSource.getAsset().getStart());
                customDataJsonObject.put(Constants.CC_SPORT_KEY, mediaSource.getAsset().getSportName());
                customDataJsonObject.put(Constants.CC_LEAGUE_KEY, mediaSource.getAsset().getLeague());
                customDataJsonObject.put(Constants.CC_LENGTH_KEY, mediaSource.getAsset().getLength());
                // Set is free to exact value if available
                customDataJsonObject.put(Constants.CC_IS_FREE_KEY, mediaSource.getAsset().getFree());
            }
        } catch (Exception e){
            Timber.e(e);
        }

        String authenticatedStreamUrl = mediaSource.getStreamUrl();
        if (auth != null
                && auth.getNbcToken() != null
                && auth.getNbcToken().getTokenizedUrl() != null
                && !auth.getNbcToken().getTokenizedUrl().isEmpty()){
            authenticatedStreamUrl = auth.getNbcToken().getTokenizedUrl();
        }

        String urlToPlay;
        if (mediaSource.getLive() && mediaSource.getAsset() != null) {
            String mvpdHash = Helper.getFreeWheelHash(config, auth);
            String csid = ""+config.getChromecast().getLive().getSiteSectionId();
            String sfid = ""+config.getChromecast().getLive().getSfId();
            String afid = ""+config.getChromecast().getLive().getAfId();
            urlToPlay = Helper.getAdobeBootstrapUrl(context,
                    authenticatedStreamUrl,
                    mediaSource.getAsset(),
                    mvpdHash,
                    csid,
                    sfid,
                    afid,
                    Helper.FREEWHEEL_DEFAULT_PROFILE,
                    true
            );
        } else {
            urlToPlay = mediaSource.getStreamUrl();
        }

        if (auth != null
                && auth.getNbcToken() != null
                && auth.getNbcToken().getToken() != null
                && !auth.getNbcToken().getToken().isEmpty()){
            try {
                customDataJsonObject.put(Constants.CC_TOKEN_KEY, auth.getNbcToken().getToken());
            } catch (Exception e){
                Timber.e(e.getLocalizedMessage());
            }
        }

        Timber.d("This is the enter point: convertToMediaInfo mediaSource CustomData %s", customDataJsonObject.toString());
        Timber.d("This is the enter point: convertToMediaInfo mediaSource Auth: %s", auth);

        return new MediaInfo.Builder(urlToPlay)
                .setStreamType(mediaSource.getLive() ? MediaInfo.STREAM_TYPE_LIVE : MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(urlToPlay.contains(".ism") ? "application/vnd.apple.mpegurl" : "application/x-mpegurl")
                .setMetadata(movieMetadata)
                .setCustomData(customDataJsonObject)
                //.setMediaTracks(tracks)
                .build();
    }

    private void initChromecastListener() {
        chromecastListener = new ChromecastListener(this);
    }

    @Override
    public ChromecastListener getChromecastListener() {
        if (chromecastListener == null) {
            initChromecastListener();
        }
        return chromecastListener;
    }

    @Override
    public CastSession getCurrentCastSession() {
        return castContext.getSessionManager().getCurrentCastSession();
    }

    private int getCastState() {
        return castContext.getCastState();
    }

    @Override
    public boolean isStateConnected() {
        return getCastState() == CastState.CONNECTED;
    }

    @Override
    public boolean isStateConnectedOrConnecting() {
        return getCastState() == CastState.CONNECTED ||
                getCastState() == CastState.CONNECTING;
    }

    @Override
    public boolean isStateNotConnected() {
        return getCastState() == CastState.NOT_CONNECTED;
    }

    @Override
    public boolean isStateNoDevicesAvailable() {
        return getCastState() == CastState.NO_DEVICES_AVAILABLE;
    }

    @Override
    public void pauseLocalPlayback() {
        persistentPlayer.setPlayWhenReady(false);
    }

    @Override
    public void loadVideo(Auth auth, RemoteMediaClient remoteMediaClient, MediaSource mediaSource, final boolean playImmediately) {
        MediaInfo mediaInfo;
        // Check if auth is null
        // 1. If auth is null, it means media source is vod or free live
        // 2. Otherwise, media source is live
        if (auth == null){
            mediaInfo = convertToMediaInfo(mediaSource,
                    mainActivity,
                    mainActivity.getConfig(),
                    null);
        } else {
            mediaInfo = convertToMediaInfo(mediaSource,
                    mainActivity,
                    mainActivity.getConfig(),
                    auth);
        }
        // Set up media load options (i.e. auto play)
        MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder()
                .setAutoplay(playImmediately).build();
        // Load media into chromecast device
        remoteMediaClient.load(mediaInfo, mediaLoadOptions);
        // pause playback if player is paused
        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                super.onStatusUpdated();
                if (remoteMediaClient != null &&
                        remoteMediaClient.getMediaStatus() != null &&
                        remoteMediaClient.getMediaStatus().getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING &&
                        !playImmediately) {
                    remoteMediaClient.pause();
                    remoteMediaClient.unregisterCallback(this);
                }
            }
        });
    }

    public void setAutoPlayListener(final AutoPlayListener autoPlayListener) {
        this.autoPlayListener = autoPlayListener;
    }

    public void removeAutoPlayListener() {
        autoPlayListener = null;
    }

    @Override
    public void handleAutoPlay(CastSession castSession) {
        if (autoPlayListener != null) {
            autoPlayListener.onAutoPlay(castSession, persistentPlayer);
        }
    }

    @Override
    public void playOnChromecast(CastSession castSession, MediaSource mediaSource, final boolean playImmediately, ChromecastAuthorizationListener chromecastAuthorizationListener) {
        if (castSession == null) {
            if (chromecastAuthorizationListener != null){
                chromecastAuthorizationListener.onAuthorizationFailure(null);
            }
            return;
        }
        if (castSession.getRemoteMediaClient() == null) {
            if (chromecastAuthorizationListener != null){
                chromecastAuthorizationListener.onAuthorizationFailure(null);
            }
            return;
        }
        if (mediaSource != null
                && mediaSource.getLive()
                && (isFreePreview(mediaSource) || !isUserAuthenticated())
                && !isFreeLiveAsset(mediaSource)){
            if (chromecastAuthorizationListener != null){
                chromecastAuthorizationListener.onAuthorizationFailure(null);
            }
            return;
        } else if (persistentPlayer != null
                && persistentPlayer.getMediaSource() != null
                && persistentPlayer.getMediaSource().getLive()
                && (isFreePreview(mediaSource) || !isUserAuthenticated())
                && !isFreeLiveAsset(mediaSource)){
            if (chromecastAuthorizationListener != null){
                chromecastAuthorizationListener.onAuthorizationFailure(null);
            }
            return;
        } else if (mediaSource == null
                && (persistentPlayer == null || persistentPlayer.getMediaSource() == null)){
            if (chromecastAuthorizationListener != null){
                chromecastAuthorizationListener.onAuthorizationFailure(null);
            }
            return;
        } else if (streamAuthentication == null){
            if (chromecastAuthorizationListener != null){
                chromecastAuthorizationListener.onAuthorizationFailure(null);
            }
            return;
        }

        RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();

        if (mediaSource != null && mediaSource.getLive()){
            streamAuthentication.chromecastCheckAuthAndPlay(mediaSource, chromecastAuthorizationListener);
        } else if (mediaSource != null && !mediaSource.getLive()){
            loadVideo(null, remoteMediaClient, mediaSource, playImmediately);
            initClosedCaptions(castSession);
        } else if (persistentPlayer != null
                && persistentPlayer.getMediaSource() != null
                && persistentPlayer.getMediaSource().getLive()){
            MediaSource cloneMediaSource = new MediaSource(persistentPlayer.getMediaSource());
            // If media source has asset, it means the media source is not vod
            // Then the media source's stream url of PersistentPlayer should be replace by
            // the NBC tokenlized url.
            // So need to change it back to the original AndroidStreamUrl
            if (persistentPlayer.getMediaSource().getAsset() != null
                    && persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl() != null
                    && !persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl().isEmpty()){
                cloneMediaSource.setStreamUrl(persistentPlayer.getMediaSource().getAsset().getAndroidStreamUrl());
            }
            streamAuthentication.chromecastCheckAuthAndPlay(cloneMediaSource, chromecastAuthorizationListener);
        } else if (persistentPlayer != null
                && persistentPlayer.getMediaSource() != null
                && !persistentPlayer.getMediaSource().getLive()){
            loadVideo(null, remoteMediaClient, persistentPlayer.getMediaSource(), playImmediately);
            initClosedCaptions(castSession);
        } else if (chromecastAuthorizationListener != null){
            chromecastAuthorizationListener.onAuthorizationFailure(null);
        }
    }

    @Override
    public void initClosedCaptions(CastSession castSession) {
        chromecastClosedCaptions.closeCustomMessageChannel();
        chromecastClosedCaptions.startCustomMessageChannel(castSession);

        if (persistentPlayer != null && persistentPlayer.isClosedCaptionEnabled()) {
            setClosedCaptions(true);
        }
    }

    @Override
    public void setClosedCaptions(final boolean enable) {
        chromecastClosedCaptions.sendMessage(enable);
    }

    public boolean isLoadSameCurrentUrl(RemoteMediaClient remoteMediaClient, MediaSource mediaSource) {
        return remoteMediaClient != null
                && remoteMediaClient.getMediaInfo() != null
                && remoteMediaClient.getMediaInfo().getMetadata() != null
                && remoteMediaClient.getMediaInfo().getMetadata().getString(MediaMetadata.KEY_TITLE) != null
                && mediaSource != null
                && mediaSource.getTitle() != null
                && remoteMediaClient.getMediaInfo().getMetadata().getString(MediaMetadata.KEY_TITLE)
                .equalsIgnoreCase(mediaSource.getTitle());
    }

    /**
     * This method will determine the visibility of the play/pause button
     * the kebab menu will always be visible
     */
    @Override
    public void setTrackControlsVisibility(final boolean isPlayPauseVisible) {
        if (persistentPlayer != null
                && persistentPlayer.getCurrentView() != null
                && persistentPlayer.getCurrentView().getPersistentPlayerView() != null){
            persistentPlayer.getCurrentView().showPlayerControlView();
            persistentPlayer.getCurrentView().getPersistentPlayerView()
                    .trackControlsVisibility(isPlayPauseVisible, isPlayPauseVisible, isPlayPauseVisible);
            persistentPlayer.getCurrentView().showProgress(isPlayPauseVisible);
        }
    }

    /**
     * Initialize the progress bar for controlling the playback remotely
     */
    void initProgressBar(final RemoteMediaClient remoteMediaClient) {
        if (remoteMediaClientProgressListener != null) {
            remoteMediaClient.removeProgressListener(remoteMediaClientProgressListener);
        }
        remoteMediaClientProgressListener = new RemoteMediaClientProgressListener(persistentPlayer);
        remoteMediaClient.addProgressListener(new RemoteMediaClientProgressListener(persistentPlayer), 1000);

        // periodically check for user scroll actions
        stopTimer();
        timerDisposable = Observable.interval(500, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (persistentPlayer != null && persistentPlayer.getPlayerEngine() != null) {
                        long playerMs = persistentPlayer.getPlayerEngine().getCurrentPosition();
                        long remoteMs = remoteMediaClient.getApproximateStreamPosition();
                        if (playerMs - remoteMs > 2000) {
                            remoteMediaClient.seek(playerMs);
                        }
                    } else {
                        //stopTimer();
                    }
                });
    }

    private void stopTimer() {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
            timerDisposable = null;
        }
    }

    public void dismissBottomDialog() {
        // Dismiss both kabab menu bottom sheet of medium view and landscape view
        // Because when kabab menu bottom sheet is showing on medium view, medium view
        // can still be switched to landscape view, then medium view's kabab menu bottom sheet
        // is showing while the player is in landscape mode
        // Dismiss kabab menu bottom sheet of medium view
        if (persistentPlayer != null &&
                persistentPlayer.getMediumView() != null &&
                persistentPlayer.getMediumView().getPersistentPlayerView() != null &&
                persistentPlayer.getMediumView().getPersistentPlayerView().getLastKnownBottomSheet() != null) {
            persistentPlayer.getMediumView().getPersistentPlayerView().getLastKnownBottomSheet().dismiss();
        }
        // Dismiss kabab menu bottom sheet of landscape view
        if (persistentPlayer != null &&
                persistentPlayer.getLandscapeView() != null &&
                persistentPlayer.getLandscapeView().getPersistentPlayerView() != null &&
                persistentPlayer.getLandscapeView().getPersistentPlayerView().getLastKnownBottomSheet() != null){
            persistentPlayer.getLandscapeView().getPersistentPlayerView().getLastKnownBottomSheet().dismiss();
        }
    }

    @Override
    public void onSessionEnding(CastSession castSession) {
        setTrackControlsVisibility(true);
        setStandalonePlayInvisible();
    }

    @Override
    public void onSessionEnded() {
        if (persistentPlayer != null){
            persistentPlayer.setPlayWhenReady(persistentPlayer.isPlayWhenReady());
        }
    }

    @Override
    public void setStandalonePlayVisible() {
        if (persistentPlayer != null
                && persistentPlayer.getCurrentView() != null
                && persistentPlayer.getCurrentView().getPersistentPlayerView() != null
                && persistentPlayer.getCurrentView().getPersistentPlayerView().getStandalonePlayButton() != null) {
            persistentPlayer.getCurrentView().getPersistentPlayerView().getStandalonePlayButton().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setStandalonePlayInvisible() {
        if (persistentPlayer != null
                && persistentPlayer.getCurrentView() != null
                && persistentPlayer.getCurrentView().getPersistentPlayerView() != null
                && persistentPlayer.getCurrentView().getPersistentPlayerView().getStandalonePlayButton() != null){
            persistentPlayer.getCurrentView().getPersistentPlayerView().getStandalonePlayButton().setVisibility(View.GONE);
        }
    }

    public boolean isFreePreview(MediaSource mediaSource) {
        if (mainActivity == null) return false;
        if (mainActivity.getConfig() == null) return false;
        // if auth is null, we are viewing a video that does not require auth
        // so free preview does not apply to the video
        if (streamAuthentication == null) return false;
        if (streamAuthentication.getLastKnownAuth() == null) return false;
        if (persistentPlayer == null) return false;
        if (mediaSource == null && persistentPlayer.getMediaSource() == null) return false;

        MediaSource finalMediaSource = mediaSource != null ? mediaSource : persistentPlayer.getMediaSource();
        Auth auth = streamAuthentication.getLastKnownAuth();
        Config config = mainActivity.getConfig();
        return finalMediaSource.getLive() && auth.isTempPassAuthN(config);
    }

    public boolean isUserAuthenticated() {
        return streamAuthentication.getLastKnownAuth() != null;
    }

    public boolean isFreeLiveAsset(MediaSource mediaSource) {
        if (mediaSource == null){
            if (persistentPlayer == null ) return false;
            if (persistentPlayer.getMediaSource() == null ) return false;
            if (persistentPlayer.getMediaSource().getAsset() == null) return false;
            return persistentPlayer.getMediaSource().getAsset().isFree();
        } else {
            if (mediaSource.getAsset() == null) return false;
            return mediaSource.getAsset().isFree();
        }
    }

    @Override
    public boolean canShowCastButton() {
        // initial conditions
        // show button if already connected, to control the current video
        if (isStateConnected()) return true;
        if (persistentPlayer == null) return false;
        if (persistentPlayer.getMediaSource() == null) return false;

        if (!persistentPlayer.getMediaSource().getLive()) {
            // if video is vod, always show cast button
            return true;
        } else if (persistentPlayer.getMediaSource().getLive() && isFreeLiveAsset(null)){
            // If video is live and it is free, then show cast button
            return true;
        } else if (!isFreePreview(null) && isUserAuthenticated()) {
            // never show button for free preview or unauthenticated users
            return true;
        }
        return false;
    }

    /**
     * This method is used to check if stream casting is live and not free
     * 1. If so, stop the stream on chromecast
     * 2. Otherwise, do nothing
     */
    @Override
    public void onLogout() {
        if (castContext != null
                && castContext.getSessionManager() != null
                && getCurrentCastSession() != null
                && getCurrentCastSession().getRemoteMediaClient() != null
                && getCurrentCastSession().getRemoteMediaClient().getMediaInfo() != null
                && getCurrentCastSession().getRemoteMediaClient().getMediaInfo().getCustomData() != null){
            try {
                JSONObject jsonObject = getCurrentCastSession().getRemoteMediaClient().getMediaInfo().getCustomData();
                boolean isStreamLive = jsonObject.getBoolean(Constants.CC_IS_LIVE_KEY);
                int streamFreeInt = jsonObject.getInt(Constants.CC_IS_FREE_KEY);
                if (isStreamLive && streamFreeInt == 0){
                    getCurrentCastSession().getRemoteMediaClient().stop();
                }
            } catch (Exception e){
                Timber.d(e);
            }
        }
    }

    private static String getSportName(MediaSource mediaSource) {
        String finalName = "";
        if (mediaSource != null && mediaSource.getAsset() != null){
            if (mediaSource.getAsset().getLeague() != null
                    && !mediaSource.getAsset().getLeague().isEmpty()){
                finalName = mediaSource.getAsset().getLeague();
            } else if (mediaSource.getAsset().getSportName() != null
                    && !mediaSource.getAsset().getSportName().isEmpty()){
                finalName = mediaSource.getAsset().getSportName();
            }
        }
        return finalName.equalsIgnoreCase("generic") ? "" : finalName;
    }
}
