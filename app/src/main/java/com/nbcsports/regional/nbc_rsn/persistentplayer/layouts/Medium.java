package com.nbcsports.regional.nbc_rsn.persistentplayer.layouts;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;

import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.auditude.ads.util.StringUtil;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationPresenter;
import com.nbcsports.regional.nbc_rsn.authentication.TokenListener;
import com.nbcsports.regional.nbc_rsn.chromecast.IChromecastHelper;
import com.nbcsports.regional.nbc_rsn.chromecast.PersistentPlayerViewChromecastListenerMixin;
import com.nbcsports.regional.nbc_rsn.common.Highlight;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Behaviour;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Error;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Injection;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerBottomSheet;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnMediumScrollListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnStandAlonePlayClickListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.LiveTimeBarControls;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.NBCSystemUtils;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import lombok.Getter;
import timber.log.Timber;

/*
Setting visiblity on control view seems to cause weird scrolling effect:
- playerControlView.setVisibility( View.VISIBLE );

Custom view implementation:
http://trickyandroid.com/protip-inflating-layout-for-your-custom-view/
*/
public class Medium extends ConstraintLayout implements PersistentPlayerContract.View, TokenListener, PlayerEngine.EventListener,
                                                        PersistentPlayerViewChromecastListenerMixin {

    @Getter
    private StreamAuthenticationContract.Presenter streamAuthenticationPresenter;
    NativeShareUtils.ShareInfo shareInfo;
    private DisposableObserver<Long> tempPassCountDown;
    private CompositeDisposable compositeDisposable;
    private IOException authError;
    protected LiveTimeBarControls liveControls;
    private PersistentPlayerBottomSheet bottomSheet;
    private boolean isPlayerInitialStatePlay = true;

    @BindView(R.id.persistent_player_view)
    protected PersistentPlayerView persistentPlayerView;

    @BindView(R.id.chromecast_connected_layout)
    View chromecastConnectedLayout;

    @BindView(R.id.chromecast_pause_btn)
    ImageView chromecastPauseBtn;

    @BindView(R.id.chromecast_play_btn)
    ImageView chromecastPlayBtn;

    @BindView(R.id.chromecast_image)
    ImageView chromecastImage;

    @BindView(R.id.chromecast_kebab)
    ImageButton chromecastKebab;

    @Getter
    private Auth lastKnownAuth;

    @Getter
    private PersistentPlayer persistentPlayer;

    @Getter
    private MediaSource mediaSource;

    @Getter
    private Auth auth = null;

    // enabled by default
    private boolean switchScreenEnabled = true;

    @Getter
    private int teamPrimaryColor = Color.BLACK;
    private OnMediumScrollListener onMediumScrollListener;

    private RemoteMediaClient.Callback chromecastCallBack;

    public Medium(Context context) {
        super(context);
        init();
    }

    public Medium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Medium(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        inflate(getContext(), R.layout.persistent_player_layout, this);
        ButterKnife.bind(this);

        persistentPlayer = Injection.providePlayer((PersistentPlayerContract.Main.View) getContext());
        streamAuthenticationPresenter = com.nbcsports.regional.nbc_rsn.authentication.Injection.provideStreamAuthentication(
                (StreamAuthenticationContract.View) getContext());

        bottomSheet = new PersistentPlayerBottomSheet();
        persistentPlayerView.initLocalization();
        setUpSwitchScreen();
        onMediumScrollListener = new OnMediumScrollListener(this);
        ImageButton standAloneButton = findViewById(R.id.standalone_play_button);
        OnStandAlonePlayClickListener sap = new OnStandAlonePlayClickListener(persistentPlayer, getContext());
        sap.setMediumView(this);
        standAloneButton.setOnClickListener(sap);

        persistentPlayer.log(persistentPlayer, "player transit: medium init");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Initialize PersistentPlayerViewChromecastListenerMixin if available
        if (IChromecastHelper.isChromecastEnabled((MainActivity)getActivity())
                && NBCSystemUtils.INSTANCE.getPLAY_SERVICES_AVAILABLE()){
            initChromecast(bottomSheet, getContext());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (persistentPlayerView.isControlsInitialized()) {
            persistentPlayerView.getPlayerControlView().setVisibility(View.INVISIBLE);
        }
        streamAuthenticationPresenter.pollAuthNtokenStop();

        if (persistentPlayer != null && !persistentPlayer.isPlaying() && !isPaused()) {
            persistentPlayer.log(persistentPlayer, "player transit: persistent player released");
            persistentPlayer.release();
        }

        // Clean up PersistentPlayerViewChromecastListenerMixin if available
        if (IChromecastHelper.isChromecastEnabled((MainActivity)getActivity())
                && NBCSystemUtils.INSTANCE.getPLAY_SERVICES_AVAILABLE()) {
            cleanUpChromecastListener();
        }
    }

    public void setMediaSource(MediaSource mediaSource) {

        Timber.d("persistent player: setMediaSource(): %s", mediaSource);
        // cloning so that streamUrl doesn't get tainted.
        MediaSource newMediaSource = new MediaSource(mediaSource);
        this.mediaSource = newMediaSource;
    }

    @OnClick({R.id.sign_in_to_watch_icon, R.id.expired_button})
    public void signInToWatch() {
        persistentPlayer.showStreamAuthentication(mediaSource);
    }

    @Override
    public boolean isPaused() {
        return persistentPlayer.isPaused();
    }

    public void show(boolean visible) {
        onMediumScrollListener.show(visible);
    }

    public void hide() {
        onMediumScrollListener.hide();
    }

    @Override
    public void setKebabClickable(boolean clickable) {
        if (persistentPlayerView.getKebabClickArea() != null) {
            persistentPlayerView.getKebabClickArea().setClickable(clickable);
        }
    }

    public void checkAuthAndPlay() {
        persistentPlayer.setMediumView(this);
        persistentPlayer.setCurrentType(PlayerConstants.Type.MEDIUM);

        ((StreamAuthenticationPresenter) streamAuthenticationPresenter)
                .checkAuthAndPlay(getContext(), this, mediaSource);
    }

    @Override
    public boolean isAuthorized() {
        return streamAuthenticationPresenter.isAuthorized();
    }

    @Override
    public void showProgress(boolean show) {
        persistentPlayerView.showProgressBar(show);
        //showControllers(!show);
        if (mediaSource.getLive() && liveControls != null) {
            liveControls.show(!show);
        }

        // RSNAPP-489: This is causing landscape to get set prematurely during loading
        // letting us seeing the team view in landscape if user rotate the device
        if (show) {

        } else {
            // This will disable the landscape mode when the video is restricted with cellular data
            if (!Behaviour.isCellularDataAllow(mediaSource, getContext())) {
                persistentPlayer.resetOrientation();
            }
        }
    }

    @Override
    public void setMainPresenter(PersistentPlayerContract.Presenter mainPresenter) {

    }

    @Override
    public void showHighlightMarker(Highlight highlight) {

    }

    @Override
    public void updateNativeShareInfo(NativeShareUtils.ShareInfo shareInfo) {
        bottomSheet.updateShareInfo(shareInfo);
    }

    @Override
    public NativeShareUtils.ShareInfo getShareInfo() {
        if (shareInfo == null) {
            Timber.d("Share Info not yet initialized in Medium");
        }
        return shareInfo;
    }

    @Override
    public PlayerView getPlayerView() {
        return persistentPlayerView.getExoPlayerView();
    }

    public void setTeamPrimaryColor(int teamPrimaryColor) {
        this.teamPrimaryColor = teamPrimaryColor;
    }

    @Override
    public void showSignInToWatchOverlay(IOException error) {

        if (mediaSource == null) {
            return;
        }

        if (error != null) {
            authError = error;
        }

        streamAuthenticationPresenter.showSignInToWatchOverlay( persistentPlayer,
                persistentPlayerView,
                mediaSource,
                this,
                teamPrimaryColor,
                error);
    }

    @Override
    public void showSignIn(IOException error) {
        showSignInToWatchOverlay(error);
    }

    @Override
    public boolean isSignedIn() {
        return streamAuthenticationPresenter.isAuthenticated();
    }

    @Override
    public void reset() {
        persistentPlayer.setPaused(false);
        if (compositeDisposable != null) {
            // Please do not use compositeDisposable.dispose()
            // Use compositeDisposable.clear() instead
            // Because dispose() will set the static disposed variable
            // in compositeDisposable to true
            // In this case the compositeDisposable needs to be re-created again
            // regardless of it is null or not
            compositeDisposable.clear();
        }
        if (liveControls != null){
            liveControls.reset();
        }
        if (persistentPlayerView != null
                && persistentPlayerView.getAdsLabel() != null){
            persistentPlayerView.getAdsLabel().setVisibility(GONE);
        }
    }

    @Override
    public void onSuccess(Auth auth) {

        Timber.d("onSuccess (%s:%s) title: %s", this.hashCode(), persistentPlayer.getCurrentView().hashCode(), mediaSource.getTitle());

        this.auth = auth;

        if (!Behaviour.isCellularDataAllow(mediaSource, getContext())) {
            showStandalonePlayButton();
            return;
        }

        if (this.hashCode() != persistentPlayer.getCurrentView().hashCode()){
            showStandalonePlayButton();
            return;
        }

        if (DisplayUtils.isVisible(this)) {
            MediaSource newMediaSource = new MediaSource(mediaSource);
            newMediaSource.setStreamUrl(auth.getNbcToken().getTokenizedUrl());
            persistentPlayer.setMediaSource(newMediaSource);
            persistentPlayer.setCurrentType(PlayerConstants.Type.MEDIUM);
            persistentPlayer.play();
            persistentPlayer.setOrientationSensor();

            if (persistentPlayer.isMiniShown()){
                persistentPlayer.showMini(false);
            }
        }

        // TODO: there should be a method to hide overlay
        persistentPlayerView.getTempPassCountdownContainer().setVisibility(GONE);
        persistentPlayerView.getSignInToWatchOverlayBackground().setVisibility(GONE);
        persistentPlayerView.getTempPassExpiredContainer().setVisibility(GONE);

        persistentPlayer.closeStreamAuthentication();

        // if auth is a temp pass, show the count down clock
        // and all the temp pass related UI
        if (auth.isTempPass(streamAuthenticationPresenter.getConfig())) {
            showTempPassCountDown(auth);
        } else {
            if (compositeDisposable != null && tempPassCountDown != null
                    && !tempPassCountDown.isDisposed()) {
                compositeDisposable.remove(tempPassCountDown);
            }
        }
        persistentPlayerView.showControllers(false);

        lastKnownAuth = auth;
    }

    @Override
    public void onError(Throwable e) {

        Timber.d("onError (%s:%s) title: %s", this.hashCode(), persistentPlayer.getCurrentView().hashCode(), mediaSource.getTitle());

        authError = new IOException(e);

        if (!Behaviour.isCellularDataAllow(mediaSource, getContext())) {
            showStandalonePlayButton();
            return;
        }

        persistentPlayer.setMediaSource(mediaSource);

        // this is to fix a weird issue where user click the to a different team view while
        // the player is still loading. It will cause playback started on the team that is out of
        // view
        if (DisplayUtils.isVisible(this)) {
            persistentPlayer.play();
        }

        if (Error.is499(authError) && mediaSource.getLive()){
            // show geo-block temp pass error if video asset is live
            showSignInToWatchOverlay(authError);
        } else if (Error.is498(authError) && mediaSource.getLive()) {
            // handles scenario if user fails entitlement check
            showStandalonePlayButton();
            getPersistentPlayerView().hideTempPassExpired();
            NotificationsManagerKt.INSTANCE.showEntitlementError(authError, mediaSource);
        }

        // Hide chromecast layout if available
        if (mediaSource.getLive()
                && mediaSource.getAsset() != null
                && !mediaSource.getAsset().isFree()
                && getChromecastHelper() != null
                && getChromecastHelper().isStateConnected()
                && persistentPlayer != null
                && streamAuthenticationPresenter != null
                && streamAuthenticationPresenter.getConfig() != null
                && (streamAuthenticationPresenter.getLastKnownAuth() == null
                || streamAuthenticationPresenter.getLastKnownAuth().isTempPassAuthN(streamAuthenticationPresenter.getConfig())
                || streamAuthenticationPresenter.getLastKnownAuth().isTempPass(streamAuthenticationPresenter.getConfig()))){
            showHideChromecastLayer(false);
        }
    }

    /**
     *
     * TODO: Confirm if match on title is OK, ideally we can use PID or ID
     */
    @Override
    public boolean showStandalonePlayButton() {
        if (persistentPlayer.getMediaSource() == null
                && Behaviour.isAutoPlayAllow(persistentPlayer, mediaSource)) {
            persistentPlayerView.getStandalonePlayButton().setVisibility(GONE);
            return false;
        }

        if (StringUtil.isNullOrEmpty(mediaSource.getStreamUrl())){
            persistentPlayerView.getStandalonePlayButton().setVisibility(VISIBLE);
            persistentPlayerView.showProgressBar(false);
            return true;
        }

        if (!Behaviour.isCellularDataAllow(mediaSource, getContext()) ||
                !Behaviour.isAutoPlayAllow(persistentPlayer, mediaSource)) {
            persistentPlayerView.getStandalonePlayButton().setVisibility(VISIBLE);
            persistentPlayerView.showProgressBar(false);
            // This will disable the landscape mode when the video is restricted with cellular data
            // or auto play
            persistentPlayer.resetOrientation();
            return true;
        }

        persistentPlayer.log(this, "trigger standalone play button");
        persistentPlayerView.getStandalonePlayButton().setVisibility(VISIBLE);
        persistentPlayerView.showProgressBar(false);
        persistentPlayer.setOrientationSensor();
        invalidate();
        return true;
    }

    public boolean isMiniShowingOnMedium(){
        if (persistentPlayer.getMediaSource() == null
                && Behaviour.isAutoPlayAllow(persistentPlayer, mediaSource)) {
            return false;
        } else if (persistentPlayer.getMediaSource() == null &&
                !Behaviour.isAutoPlayAllow(persistentPlayer, mediaSource)){
            return true;
        }
        // In case we need to use url to match
        // Uri uri = Uri.parse(persistentPlayer.getMediaSource().getStreamUrl());
        // String noToken = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
        // Match with title for now:
        String titleInMini = persistentPlayer.getMediaSource().getTitle();
        String titleInMedium = mediaSource.getTitle();

        if (persistentPlayer.getType() == PlayerConstants.Type.MINI
                && !titleInMedium.equalsIgnoreCase(titleInMini)) {
            persistentPlayer.log(this, "Mini is showing on a legit medium player");
            return true;
        } else if (!Behaviour.isAutoPlayAllow(persistentPlayer, mediaSource)){
            persistentPlayer.log(this, "medium.show auto play is not allowed");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void showPlayerControlView() {
        persistentPlayer.log(persistentPlayer, "player transit: player control show");
        if (liveControls == null){
            liveControls = new LiveTimeBarControls(this, this, persistentPlayer);
        }
        persistentPlayerView.initControls(persistentPlayer, liveControls, bottomSheet);
        persistentPlayerView.showControllers(false);
    }

    @Override
    public void enableSwitchScreen(boolean enable) {
        switchScreenEnabled = enable;
        updateSwitchScreenEnabled();
    }

    public void hideAllControls() {
        if (persistentPlayerView != null){
            if (persistentPlayerView.getStandalonePlayButton() != null){
                persistentPlayerView.getStandalonePlayButton().setVisibility(GONE);
            }
            persistentPlayerView.showControllers(false);
        }
    }

    @Override
    public ViewGroup getPrimetimeView() {
        return persistentPlayerView.getPrimetimePlayerView();
    }

    public void receiveGoLiveState(boolean isPaused, boolean isGoLive) {
        if (liveControls == null){
            persistentPlayer.log(this,"Player transit: Medium liveControls is null");
            return;
        }
        persistentPlayer.setPaused(isPaused);
        if (isGoLive){
            liveControls.setStateToGoLive();
        } else {
            liveControls.reset();
        }
    }

    @Override
    public boolean isInGoLiveState() {
        if (liveControls != null) {
            return liveControls.isInGoLiveState();
        }
        return false;
    }

    @Override
    public View getPlayerControlView() {
        return persistentPlayerView.getPlayerControlView();
    }

    @Override
    public PersistentPlayerView getPersistentPlayerView() {
        return persistentPlayerView;
    }

    private void setUpSwitchScreen() {
        updateSwitchScreenEnabled();
        persistentPlayerView.getSwitchScreenView().setVisibility(GONE); // set default to GONE.
        persistentPlayerView.getSwitchScreenView().bindPlayer(this);
        persistentPlayerView.getSwitchScreenView().setOnItemClickListener(newMediaSource -> {

            if (newMediaSource.getStreamUrl().isEmpty() || persistentPlayer.getMainContractView() == null) return;

            DeeplinkManager.getInstance().beginDeeplinkFromSwitchScreen(
                    newMediaSource.getDeeplink(),
                    (MainActivity) persistentPlayer.getMainContractView());
        });
    }

    /**
     * This method is used to update switchScreenEnabled variable base on config json
     */
    private void updateSwitchScreenEnabled() {
        if (streamAuthenticationPresenter != null
                && streamAuthenticationPresenter.getConfig() != null){
            switchScreenEnabled = streamAuthenticationPresenter.getConfig()
                    .getSwitchScreenEnabled();
        }
        persistentPlayerView.getSwitchScreenView().setVisibility(switchScreenEnabled ? VISIBLE : GONE);
        Timber.d("This is the enter point: switchScreenEnabled: "+switchScreenEnabled);
    }

    @Override
    public void updateSwitchScreenOrientation() {
        if (switchScreenEnabled){
            persistentPlayerView.getSwitchScreenView().updateSwitchScreenOrientation(false);
        }
    }

    @Override
    public void updateSwitchScreen(MediaSource mediaSource) {
        if (switchScreenEnabled){
            persistentPlayerView.getSwitchScreenView().onVideoPlay(mediaSource, streamAuthenticationPresenter.isAuthenticated());
        }
    }

    @Override
    public void onComplete() {
        if (switchScreenEnabled){
            persistentPlayerView.getSwitchScreenView().drawerOpen();
        }
        persistentPlayer.log(this, "done playing - medium");
    }

    @Override
    public void onAdBreakStart(AdBreak adBreak) {
        persistentPlayer.setAdPlaying(true);
        persistentPlayerView.showAdUI(true);
    }

    @Override
    public void onAdBreakComplete(AdBreak adBreak) {
        persistentPlayer.setAdPlaying(false);
        persistentPlayerView.showControllers(true);
        persistentPlayerView.showAdUI(false);
    }

    @Override
    public void enableCloseButton(boolean enable) {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof MainActivity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    @Override
    public IChromecastHelper getChromecastHelper() {
        return persistentPlayer.getChromecastHelper();
    }

    @Override
    public ImageView getChromecastPlayBtn() {
        return chromecastPlayBtn;
    }

    @Override
    public ImageView getChromecastPauseBtn() {
        return chromecastPauseBtn;
    }

    @Override
    public ImageButton getChromecastKebabBtn() {
        return chromecastKebab;
    }

    @Override
    public View getChromecastLayout() {
        return chromecastConnectedLayout;
    }

    @Override
    public ImageView getChromecastImage() {
        return chromecastImage;
    }

    @Override
    public MediaSource getChromecastMediaSource() {
        return mediaSource;
    }

    @Override
    public PersistentPlayer getChromecastPersistentPlayer() {
        return persistentPlayer;
    }

    @Override
    public RemoteMediaClient.Callback getChromecastCallBack() {
        return chromecastCallBack;
    }

    @Override
    public void setChromecastCallBack(RemoteMediaClient.Callback callBack) {
        this.chromecastCallBack = callBack;
    }

    public void showTempPassCountDown(Auth auth) {
        if (compositeDisposable == null){
            compositeDisposable = new CompositeDisposable();
        }
        if (tempPassCountDown != null && !tempPassCountDown.isDisposed()){
            compositeDisposable.remove(tempPassCountDown);
        }
        tempPassCountDown = persistentPlayerView.getTempPassCountdownContainer().start(auth, () -> {

            // show temp pass expired when countdown is complete
            HttpDataSource.InvalidResponseCodeException error = new HttpDataSource.InvalidResponseCodeException(410, null, null);
            streamAuthenticationPresenter.showSignInToWatchOverlay( persistentPlayer,
                    persistentPlayerView,
                    mediaSource,
                    Medium.this,
                    teamPrimaryColor,
                    error);
            streamAuthenticationPresenter.setAuthorized(false);
            persistentPlayer.release();

            // Hide chromecast layout if available
            if (mediaSource.getLive()
                    && mediaSource.getAsset() != null
                    && !mediaSource.getAsset().isFree()
                    && getChromecastHelper() != null
                    && getChromecastHelper().isStateConnected()){
                showHideChromecastLayer(false);
            }
        });

        compositeDisposable.add(tempPassCountDown);
        persistentPlayerView.showTeamPassCountdown();
    }
}
