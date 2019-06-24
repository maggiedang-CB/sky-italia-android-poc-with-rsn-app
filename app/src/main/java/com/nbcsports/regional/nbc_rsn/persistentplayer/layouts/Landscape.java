package com.nbcsports.regional.nbc_rsn.persistentplayer.layouts;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.jakewharton.rxbinding2.view.RxView;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.MainPresenter;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationFragment;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationPresenter;
import com.nbcsports.regional.nbc_rsn.authentication.TokenListener;
import com.nbcsports.regional.nbc_rsn.chromecast.IChromecastHelper;
import com.nbcsports.regional.nbc_rsn.chromecast.PersistentPlayerViewChromecastListenerMixin;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Highlight;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.common.TimelineMarker;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Error;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Injection;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerBottomSheet;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnStandAlonePlayClickListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.LiveTimeBarControls;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.NBCSystemUtils;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import lombok.Setter;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.BOTTOM_SHEET;

public class Landscape extends BaseFragment implements PersistentPlayerContract.View,
                                                       TokenListener, PlayerEngine.EventListener,
                                                       PersistentPlayerViewChromecastListenerMixin {

    private PlayerView exoPlayerView;
    private DisposableObserver<Long> tempPassCountDown;
    private PersistentPlayerBottomSheet bottomSheet;
    private Long[] extraAdGroupTimesMs;
    private Boolean[] extraPlayedAdGroups;
    private boolean shown;
    private StreamAuthenticationContract.Presenter streamAuthenticationPresenter;
    private IOException authError;
    private PersistentPlayerContract.Presenter presenter;
    private PersistentPlayer persistentPlayer;
    NativeShareUtils.ShareInfo shareInfo;
    private boolean isKebabClickable = true;
    private String title;
    protected LiveTimeBarControls liveControls;

    @Setter
    private MediaSource mediaSource;

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

    // by default switch screen functionality is enabled on landscape
    // disabled - 24/7 player
    private boolean switchScreenEnabled = true;
    private MainContract.Presenter mainPresenter;

    private RemoteMediaClient.Callback chromecastCallBack;

    public Landscape() {
        //no-op
    }

    public static Landscape newInstance() {
        Landscape fragment = new Landscape();
        return fragment;
    }

    @Override
    public int getLayout() {
        return R.layout.persistent_player_layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exoPlayerView = getView().findViewById(R.id.exo_player_view);

        bottomSheet = new PersistentPlayerBottomSheet();
        bottomSheet.setOnDismiss(dialogInterface -> hideSystemUi());

        RxView.clicks(persistentPlayerView.getHighlightForward())
                .subscribe(playerHighlightForward());

        RxView.clicks(persistentPlayerView.getHighlightBackward())
                .subscribe(playerHighlightBackward());

        persistentPlayer = Injection.providePlayer((PersistentPlayerContract.Main.View) getContext());
        streamAuthenticationPresenter = com.nbcsports.regional.nbc_rsn.authentication.Injection.provideStreamAuthentication(
                (StreamAuthenticationContract.View) getContext());
        mainPresenter = MainPresenter.Injection.providePresenter(getContext());

        setUpSwitchScreen();

        ImageButton standAloneButton = getView().findViewById(R.id.standalone_play_button);
        standAloneButton.setOnClickListener(new OnStandAlonePlayClickListener(persistentPlayer, getContext()));
    }

    @Override
    public void onDestroyView() {
        removeSessionManagerListener();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!streamAuthenticationPresenter.isStarted()
                && persistentPlayer.getType() == PlayerConstants.Type.LANDSCAPE) {
            persistentPlayer.setPlayWhenReady(true);

            if (persistentPlayer.is247() && !(NavigationManager.getInstance().getCurrentFragment() instanceof StreamAuthenticationFragment)){
                persistentPlayer.setOrientationLandscape();
            }
        }
        hideSystemUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (persistentPlayer.getType() == PlayerConstants.Type.LANDSCAPE) {
            persistentPlayer.setPlayWhenReady(false);
        }
    }

    @OnClick({R.id.sign_in_to_watch_icon, R.id.expired_button})
    public void signInToWatch() {
        // When sign in button is clicked (either temppass or expired view)
        // 1. Hide landscape view and remove all listeners from this landscape
        // 2. Show StreamAuthenticationFragment with
        //    bundle (i.e. MediaSource and is247 flag) passes into it
        // 3. Reset is247 flag in PersistentPlayer to
        //    false (this will fix RSNAPP-934 second issue)
        persistentPlayer.showAsLandscape(false);
        persistentPlayer.showStreamAuthentication(mediaSource);
        persistentPlayer.set247(false);
    }

    @Override
    public boolean isPaused() {
        return persistentPlayer.isPaused();
    }

    public void show(FragmentManager supportFragmentManager, PlayerConstants.Type source) {

        ActivityUtils.showFragment(supportFragmentManager, this);
        shown = true;
        updateSwitchScreenEnabled();
        hideSystemUi();
        liveControls = new LiveTimeBarControls(this, getView(), persistentPlayer);
        persistentPlayer.setCurrentType(PlayerConstants.Type.LANDSCAPE);
        persistentPlayerView.initLocalization();
        persistentPlayerView.initControls(persistentPlayer, liveControls, bottomSheet);
        persistentPlayerView.initMvpdLogo(streamAuthenticationPresenter);
        liveControls.show(true);
        enableCloseButton(true);

        switch (source) {
            case MINI:
            case MEDIUM:
                persistentPlayerView.getSignInToWatchClose().setVisibility(View.GONE);
                setupTimeBars(persistentPlayer.getMediaSource().getLive());
                // Sync with medium
                if (persistentPlayer.getMediumView() != null) {
                    PersistentPlayerView.syncState(persistentPlayer.getMediumView().getPersistentPlayerView(), persistentPlayerView);
                }
                break;
            case _247:
                persistentPlayerView.getSignInToWatchClose().setVisibility(View.VISIBLE);
                if (persistentPlayer.getMediaSource() == null){
                    persistentPlayer.setMediaSource(mediaSource);
                }
                setupTimeBars(persistentPlayer.getMediaSource().getLive());
                checkAuthAndPlay();
                persistentPlayerView.getTempPassExpiredContainer().setVisibility(GONE);
                persistentPlayerView.getTempPassCountdownContainer().setVisibility(GONE);
                break;
        }

        if (persistentPlayer.getPlayerEngine() != null) {
            persistentPlayer.getPlayerEngine().addListener(this);
        }

        if (persistentPlayer.getMediaSource() != null && persistentPlayer.getMediaSource().getLive()
                && streamAuthenticationPresenter.getLastKnownAuth() != null
                && (streamAuthenticationPresenter.getLastKnownAuth().isTempPassAuthN(mainPresenter.getLastKnownConfig())
                || streamAuthenticationPresenter.getLastKnownAuth().isTempPass(mainPresenter.getLastKnownConfig()))) {
            persistentPlayerView.getTempPassCountdownContainer().setVisibility(VISIBLE);
        } else {
            persistentPlayerView.getTempPassCountdownContainer().setVisibility(GONE);
            if (compositeDisposable != null && tempPassCountDown != null
                    && !tempPassCountDown.isDisposed()) {
                compositeDisposable.remove(tempPassCountDown);
            }
        }
        if (persistentPlayer.isPlaying() || persistentPlayer.isSuspended()) {
            persistentPlayerView.getTempPassExpiredContainer().setVisibility(GONE);
            persistentPlayerView.getSignInToWatchOverlayBackground().setVisibility(GONE);
        }

        // Start progress bar show and hide observer
        if (persistentPlayer != null && persistentPlayerView != null){
            // Remove all progress bar observers first
            PersistentPlayerProgressBarManager.removeAllObservers();
            // Start and add progress bar observer into
            // PersistentPlayerProgressBarManager
            persistentPlayerView.startProgressBarObserver(persistentPlayer, this);
        }

        // Initialize chromecast's listeners and layouts
        if (IChromecastHelper.isChromecastEnabled((MainActivity)getActivity())
                && NBCSystemUtils.INSTANCE.getPLAY_SERVICES_AVAILABLE()) {
            initChromecast(bottomSheet, getContext());
        }
    }

    public void hide(FragmentManager supportFragmentManager) {

        ActivityUtils.hideFragment(supportFragmentManager, this);
        shown = false;

        // Stop progress bar show and hide observer
        if (persistentPlayerView != null){
            persistentPlayerView.stopProgressBarObserver(this);
        }

        // Stop temp pass count down observer when landscape hide
        if (compositeDisposable != null && tempPassCountDown != null
                && !tempPassCountDown.isDisposed()){
            compositeDisposable.remove(tempPassCountDown);
        }

        // Also, the listener can be removed and exoPlayer released and null'ed, in the signInToWatch() method.
        if (persistentPlayer.getPlayerEngine() != null) {
            persistentPlayer.getPlayerEngine().removeListener(this);
        }

        enableCloseButton(false);

        // Clean up chromecast's listener
        if (IChromecastHelper.isChromecastEnabled((MainActivity)getActivity())
                && NBCSystemUtils.INSTANCE.getPLAY_SERVICES_AVAILABLE()) {
            cleanUpChromecastListener();
        }
    }

    public void setupTimeBars(boolean isLive) {

        liveControls = new LiveTimeBarControls(this, getView(), persistentPlayer);
        if (isLive) {
            persistentPlayerView.showNonLiveControlUI(false);
        } else {
            persistentPlayerView.showNonLiveControlUI(true);
        }
        liveControls.show(isLive);
    }
    @Deprecated
    public void setupTimeBarComponentsVisibilityOnSigninSuccess() {
        // TODO: Check if this method is redundant
        persistentPlayerView.showNonLiveControlUI(false);
        liveControls.show(true);
    }
    @Deprecated
    public void setupTimeBarComponentsVisibility() {
        // TODO: Check if this method is redundant
        if (mediaSource.getLive()) {
            setupTimeBarComponentsVisibilityOnSigninSuccess();
        } else {
            liveControls.show(false);
        }
    }

    public void checkAuthAndPlay() {
        persistentPlayer.setCurrentType(PlayerConstants.Type.LANDSCAPE);

        // This piece of code is used to fix an issue where
        // VOD ended -> Stand alone button show on Landscape -> click sab
        // and nothing happens
        if (persistentPlayer.getMediaSource() != null){
            this.mediaSource = persistentPlayer.getMediaSource();
            // Replace stream url with the one in asset
            // It it because persistentPlayer.getMediaSource().getStreamUrl()
            // may be a tokenizedUrl, thus need to reset it back to original one
            if (mediaSource.getAsset() != null
                    && mediaSource.getAsset().getAndroidStreamUrl() != null
                    && !mediaSource.getAsset().getAndroidStreamUrl().isEmpty()){
                mediaSource.setStreamUrl(mediaSource.getAsset().getAndroidStreamUrl());
            }
        }

        ((StreamAuthenticationPresenter) streamAuthenticationPresenter).checkAuthAndPlay(
                getContext(),
                this,
                mediaSource
        );
    }

    @Override
    public boolean isAuthorized() {
        return streamAuthenticationPresenter.isAuthorized();
    }

    @Override
    public void showProgress(boolean show) {
        if (persistentPlayerView != null) {
            persistentPlayerView.showProgressBar(show);
        }
    }

    @Override
    public void onSuccess(Auth auth) {
        Timber.d("onSuccess title: %s auth: %s", mediaSource.getTitle(), auth);

        persistentPlayerView.displayHeader(auth);

        MediaSource newMediaSource = new MediaSource(mediaSource);
        newMediaSource.setStreamUrl(auth.getNbcToken().getTokenizedUrl());
        persistentPlayer.setMediaSource(newMediaSource);
        persistentPlayer.setCurrentType(PlayerConstants.Type.LANDSCAPE);
        persistentPlayer.play();
        if (!persistentPlayer.is247()) {
            persistentPlayer.setOrientationSensor();
        }

        // TODO: there should be a method to hide overlay
        persistentPlayerView.getTempPassCountdownContainer().setVisibility(GONE);
        persistentPlayerView.getSignInToWatchOverlayBackground().setVisibility(GONE);
        persistentPlayerView.getTempPassExpiredContainer().setVisibility(GONE);

        //setupTimeBarComponentsVisibilityOnSigninSuccess();

        persistentPlayer.closeStreamAuthentication();

        // if auth is a temp pass, show the count down clock
        // and all the temp pass related UI
        if (auth.isTempPass(mainPresenter.getLastKnownConfig())) {
            showTempPassCountDown(auth);
        }
        persistentPlayerView.showControllers(false);

        // This is different than Medium, where Landscape need to be brought up again
        // since it was closed when Stream Authentication was opened
        ActivityUtils.showFragment(getActivity().getSupportFragmentManager(), this);
        ((MainActivity) getActivity()).hideFab();
    }

    @Override
    public void onError(Throwable e) {
        persistentPlayerView.getMvpdTopBarLogo().setVisibility(View.GONE);
        authError = new IOException(e);

        persistentPlayer.setMediaSource(mediaSource);

        if (Error.is499(authError) && mediaSource.getLive()) {
            // show geo-block temp pass error if video asset is live
            showSignInToWatchOverlay(authError);
        } else if (Error.is498(authError) && mediaSource.getLive()) {
            // handles scenario if user fails entitlement check
            showStandalonePlayButton();
            getPersistentPlayerView().hideTempPassExpired();
            NotificationsManagerKt.INSTANCE.showEntitlementError(authError, mediaSource);
        } else if (DisplayUtils.isVisible(this.getView())) {
            // this is to fix a weird issue where user click the to a different team view while
            // the player is still loading. It will cause playback started on the team that is out of
            // view
            persistentPlayer.play();
            if (!persistentPlayer.is247()) {
                persistentPlayer.setOrientationSensor();
            }
        }

        persistentPlayerView.showControllers(false);

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

    @Override
    public void enableSwitchScreen(boolean enable) {
        switchScreenEnabled = enable;
        updateSwitchScreenEnabled();
    }

    @Override
    public void enableCloseButton(boolean enable) {
        persistentPlayerView.getCloseButton().setVisibility(enable ? VISIBLE : GONE);
    }

    @Override
    public ViewGroup getPrimetimeView() {
        return persistentPlayerView.getPrimetimePlayerView();
    }

    @OnClick(R.id.persistent_player_kebab_button_click_area)
    public void openKebabMenu() {
        if (isKebabClickable) {
            bottomSheet.show(getChildFragmentManager(), BOTTOM_SHEET);
        }
    }

    public void playInExoPlayer(MediaSource newMediaSource) {
        hideSystemUi();

        title = newMediaSource.getTitle();

        checkAuthAndPlay(newMediaSource);

        // This fixes the issue where time bar does not counting time
        // if last viewed video is selected in landscape
        persistentPlayerView.startTimebar();
        // Reset time bar listener
        if (persistentPlayer != null){
            persistentPlayerView.resetTimeBarListener(persistentPlayer);
        }

        // No title for now as per design, but i think we should
        // titleView.setText(title);
        if (switchScreenEnabled) {
            persistentPlayerView.getSwitchScreenView().onVideoPlay(mediaSource, streamAuthenticationPresenter.isAuthenticated());
        }
    }

    @Override
    public void updateNativeShareInfo(NativeShareUtils.ShareInfo shareInfo) {
        this.shareInfo = shareInfo;
        bottomSheet.updateShareInfo(shareInfo);
    }

    @Override
    public void setKebabClickable(boolean clickable) {
        isKebabClickable = clickable;
    }

    @Override
    public NativeShareUtils.ShareInfo getShareInfo() {
        return shareInfo;
    }

    public void hideSystemUi() {
        exoPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public boolean isShown() {
        return shown;
    }

    @Override
    public boolean isSignedIn() {
        return streamAuthenticationPresenter.isAuthorized();
    }

    @Override
    public void reset() {
        persistentPlayer.setPaused(false);
        if (liveControls != null){
            liveControls.reset();
        }
        if (persistentPlayerView != null && persistentPlayerView.getCloseClickListener() != null) {
            persistentPlayerView.getCloseClickListener().reset();
        }
        if (persistentPlayerView != null
                && persistentPlayerView.getAdsLabel() != null){
            persistentPlayerView.getAdsLabel().setVisibility(GONE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        exoPlayerView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void setMainPresenter(PersistentPlayerContract.Presenter mainPresenter) {
        this.presenter = mainPresenter;
    }

    public void onDrawerStateChanged(boolean isDrawerOpen) {
//        if(isDrawerOpen){
//            playerControlView.hide();
//        } else {
//            playerControlView.show();
//        }
    }

    private void setUpSwitchScreen() {
        // Because the landscape fragment is created before the config json is loaded
        // So updateSwitchScreenEnabled() is useless here
        // But if in the feature, if fragments are created after config json is loaded
        // Then we need to use updateSwitchScreenEnabled()
        // Right now just adding it as place holder
        updateSwitchScreenEnabled();
        persistentPlayerView.getSwitchScreenView().setVisibility(GONE);
        persistentPlayerView.getSwitchScreenView().bindPlayer(this);
        persistentPlayerView.getSwitchScreenView().setOnItemClickListener(newMediaSource -> {
            if (newMediaSource.getStreamUrl().isEmpty()) return;

            playInExoPlayer(newMediaSource);
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
    public PlayerView getPlayerView() {
        return exoPlayerView;
    }

    @Override
    public void showSignInToWatchOverlay(IOException error) {

        Timber.d("streamauth Is the Landscape.showSignInToWatchOverlay() intended for calling?");
        if (mediaSource == null) {
            return;
        }
        if (error != null) {
            authError = error;
        }

        // prevent weird signin overlay shows up in 24/7 player when user is authenticated
        if (persistentPlayer.is247() && streamAuthenticationPresenter.isAuthorized()
                && ! Error.is499(authError)){
            return;
        }

        String teamPrimaryColor = TeamManager.Companion.getInstance().getSelectedTeam().getPrimaryColor();
        int colorInt = Color.parseColor(teamPrimaryColor);
        if (error != null) {
            streamAuthenticationPresenter.showSignInToWatchOverlay( persistentPlayer,
                    persistentPlayerView,
                    mediaSource,
                    this.getView(),
                    colorInt,
                    error);
            if(persistentPlayerView.getTempPassExpiredContainer().getVisibility() == VISIBLE) {
                isKebabClickable = false;
            }

            if (persistentPlayer.is247()){
                persistentPlayer.setOrientationLandscape();
            }
        }
    }

    @Override
    public void showSignIn(IOException error) {
        Timber.d("showSignIn(IOException error)");
        showSignInToWatchOverlay(error);
    }

    // Highlights (Chapter Markers)
    //region highlights
    @Override
    public void showHighlightMarker(Highlight highlight) {

        persistentPlayerView.getHighlightForward().setVisibility(View.VISIBLE);
        persistentPlayerView.getHighlightBackward().setVisibility(View.VISIBLE);

        List<Long> timeArray = new ArrayList<>();
        List<Boolean> booleanArray = new ArrayList<>();
        List<TimelineMarker> markers = highlight.getTimelineMarkers();
        checkNotNull(markers);
        DateTime programStartTime = new DateTime(presenter.getProgramStartTime() / 1000);
        Timber.d("highlight: %s", highlight);
        Timber.d("highlight programStartTime: %s", programStartTime);

        for (TimelineMarker marker : markers) {

            DateTime timestamp = new DateTime(marker.getEpochTime());
            Timber.d("highlight marker timestamp: %s", timestamp);

            try {
                Duration interval = new Interval(programStartTime, timestamp).toDuration();
                Timber.d("highlight marker: %s, interval: %s", marker.getEpochTime(), interval.getMillis());
                timeArray.add(interval.getMillis());
                booleanArray.add(true);
            } catch (Exception e) {
                Timber.e("highlight execption: %s", e.toString());
            }
        }

        extraAdGroupTimesMs = timeArray.toArray(new Long[timeArray.size()]);
        long[] primitiveTime = ArrayUtils.toPrimitive(extraAdGroupTimesMs);
        extraPlayedAdGroups = booleanArray.toArray(new Boolean[booleanArray.size()]);
        boolean[] primitiveBoolean = ArrayUtils.toPrimitive(extraPlayedAdGroups);

//        persistentPlayerView.getPlayerControlView().setExtraAdGroupMarkers(
//                primitiveTime,
//                primitiveBoolean);

        // Highlight labels
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
                .map(positionInMilliseconds -> persistentPlayer.getPlayerEngine().getCurrentPosition())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(setHighlightLabelVisibility());
    }

    private Observer<? super Object> playerHighlightBackward() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object o) {
                PlayerEngine.Interface player = persistentPlayer.getPlayerEngine();
                for (int i = extraAdGroupTimesMs.length - 1; i >= 0; i--) {
                    long adGroupTime = extraAdGroupTimesMs[i];
                    if (adGroupTime < player.getCurrentPosition() && player.getCurrentPosition() < adGroupTime + 2000 && i - 1 > -1) {
                        player.seekTo(extraAdGroupTimesMs[i - 1]);
                        updateTitleWithHighlight(i - 1);
                        break;
                    } else if (adGroupTime < player.getCurrentPosition()) {
                        player.seekTo(adGroupTime);
                        updateTitleWithHighlight(i);
                        break;
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private Observer<? super Object> playerHighlightForward() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object o) {
                for (int i = 0; i < extraAdGroupTimesMs.length; i++) {
                    long adGroupTime = extraAdGroupTimesMs[i];
                    if (persistentPlayer.getPlayerEngine().getCurrentPosition() < adGroupTime) {
                        persistentPlayer.getPlayerEngine().seekTo(adGroupTime);
                        updateTitleWithHighlight(i);
                        break;
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private Observer<Long> setHighlightLabelVisibility() {
        return new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long currentPosition) {
                //Timber.e("setHighlightLabelVisibility %s:", currentPosition);

                try {
                    checkNotNull(extraAdGroupTimesMs);
                    checkPositionIndex(0, extraAdGroupTimesMs.length);

                    long first = extraAdGroupTimesMs[0];
                    long last = extraAdGroupTimesMs[extraAdGroupTimesMs.length - 1];

                    persistentPlayerView.getHighlightBackward().setVisibility(first > currentPosition ? View.GONE : View.VISIBLE);
                    persistentPlayerView.getHighlightForward().setVisibility(last < currentPosition ? View.GONE : View.VISIBLE);
                } catch (Exception e) {
                    Timber.d(e);
                }

                // Ensure highlight label check at 0s
                //setHighlightLabelVisibility().onNext( 0l );
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    private void updateTitleWithHighlight(int positionInTimeline) {
        if (presenter.getTimelineMarkers() != null
                && presenter.getTimelineMarkers().get(positionInTimeline) != null) {
            persistentPlayerView.getTitleView().setText(presenter.getTimelineMarkers().get(positionInTimeline).getDescription());
        }
    }
    // endregion

    private void checkAuthAndPlay(MediaSource newMediaSource) { // See Landscape.checkAuthAndPlay()
        mediaSource = newMediaSource;

        // after click on a video in switch screen,
        // lock to landscape so that user cannot get back to team view while player is loading
        // reset to sensor in onSuccess and onError
        persistentPlayer.setOrientationLandscape();

        ((StreamAuthenticationPresenter) streamAuthenticationPresenter).checkAuthAndPlay(
                getContext(),
                this,
                mediaSource
        );
    }

    public void onClose() {
        persistentPlayerView.getCloseButton().performClick();
    }

    public void showUI(Auth auth) {
        if (auth != null) {
            if (auth.isTempPass(mainPresenter.getLastKnownConfig())
                    && persistentPlayer != null && persistentPlayer.getMediaSource() != null
                    && persistentPlayer.getMediaSource().getLive()
                    && persistentPlayer.getMediaSource().getAsset() != null
                    && !persistentPlayer.getMediaSource().getAsset().isFree()) {
                showTempPassCountDown(auth);
            }
        } else {

        }
    }

    public void showTempPassCountDown(Auth auth) {
        checkNotNull(auth);
        String teamPrimaryColor = TeamManager.Companion.getInstance().getSelectedTeam().getPrimaryColor();
        int colorInt = Color.parseColor(teamPrimaryColor);
        if (compositeDisposable == null){
            compositeDisposable = new CompositeDisposable();
        }
        tempPassCountDown = persistentPlayerView.getTempPassCountdownContainer().start(auth, () -> {

            // show temp pass expired when countdown is complete
            HttpDataSource.InvalidResponseCodeException error = new HttpDataSource.InvalidResponseCodeException(410, null, null);
            streamAuthenticationPresenter.showSignInToWatchOverlay( persistentPlayer,
                    persistentPlayerView,
                    persistentPlayer.getMediaSource(),
                    Landscape.this.getView(),
                    colorInt,
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

        compositeDisposable.clear();
        compositeDisposable.add(tempPassCountDown);
        persistentPlayerView.showTeamPassCountdown();
    }

    @Override
    public void receiveGoLiveState(boolean isPaused, boolean isGoLive) {
        if (liveControls == null){
            persistentPlayer.log(this,"Player transit: Landscape liveControls is null");
            setupTimeBars(persistentPlayer.isLive());
            receiveGoLiveState(isPaused, isGoLive);
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

    @Override
    public boolean showStandalonePlayButton() {
        return false;
    }

    @Override
    public void showPlayerControlView() {
        
    }

    @Override
    public void updateSwitchScreen(MediaSource mediaSource) {
        if (switchScreenEnabled){
            persistentPlayerView.getSwitchScreenView().onVideoPlay(mediaSource, streamAuthenticationPresenter.isAuthenticated());
        }
    }

    @Override
    public void updateSwitchScreenOrientation() {
        if (switchScreenEnabled){
            persistentPlayerView.getSwitchScreenView().updateSwitchScreenOrientation(true);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        Timber.d("onTimelineChanged manifest: %s", manifest);
        if (manifest instanceof HlsManifest) {
            HlsManifest hlsManifest = (HlsManifest) manifest;
            long startTimeUs = hlsManifest.mediaPlaylist.startTimeUs;
            presenter.setProgramStartTime(startTimeUs);
            if (mediaSource != null) {
                presenter.getHighlightData(mediaSource);
            }
        }
    }

    @Override
    public void onComplete() {
        if (switchScreenEnabled){
            persistentPlayerView.getSwitchScreenView().drawerOpen();
        }
        persistentPlayer.log(this, "done playing - landscape");
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
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "", "", "", "", "", "", "", "");
    }



    @Override
    public ImageView getChromecastImage() {
        return chromecastImage;
    }

    @Override
    public View getChromecastLayout() {
        return chromecastConnectedLayout;
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
    public IChromecastHelper getChromecastHelper() {
        return persistentPlayer.getChromecastHelper();
    }

    @Override
    public MediaSource getChromecastMediaSource() {
        // May be change because not sure when the media source is set on landscape
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
}
