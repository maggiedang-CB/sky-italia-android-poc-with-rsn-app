package com.nbcsports.regional.nbc_rsn.persistentplayer.layouts;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Highlight;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.Injection;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnMiniScrollListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnMiniDragListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;

import java.io.IOException;

import butterknife.BindView;
import lombok.Getter;
import lombok.Setter;

import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.PlayerEngine.Config.ALPHA_ANIMATION;

public class Mini extends BaseFragment implements PersistentPlayerContract.View, PlayerEngine.EventListener {

    NativeShareUtils.ShareInfo shareInfo;
    private OnMiniScrollListener onMiniScrollListener;
    private OnMiniDragListener onMiniDragListener;

    @BindView(R.id.persistent_player_view)
    protected PersistentPlayerView persistentPlayerView;

    @Getter
    private PersistentPlayer persistentPlayer;

    @Setter
    private boolean shown;

    @Getter
    private PlayerView exoPlayerView;

    @Getter
    private int screenHeight;

    @Getter
    private float viewPositionX;

    @Getter
    private float viewPositionY;

    public Mini() {
        //no-op
    }

    public static Mini newInstance() {
        Mini fragment = new Mini();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //this.trackingPageName = "MiniFragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayout() {
        return R.layout.persistent_player_layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        exoPlayerView = getView().findViewById(R.id.exo_player_view);

        ViewGroup parentView = (ViewGroup) getView().getParent();
        // by default hide the exoPlayer, mini will only be visible if it's triggered from:
        // Mini > Landscape > Mini
        // Medium > scroll content until medium exoPlayer is not visible
        // To hide it:
        // set the view below screenHeight using setY(screenHeight)
        screenHeight = DisplayUtils.getScreenHeight(getContext());
        ViewTreeObserver vto = parentView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                parentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                viewPositionX = parentView.getX();
                viewPositionY = parentView.getY();
                parentView.animate().y(screenHeight).setDuration(0).start();
            }
        });
        persistentPlayerView.showProgressBar(false);
        persistentPlayer = Injection.providePlayer((PersistentPlayerContract.Main.View) getContext());
        onMiniScrollListener = new OnMiniScrollListener(this);
        onMiniDragListener = new OnMiniDragListener(this, () -> {
            ActivityUtils.hideFragment(getActivity().getSupportFragmentManager(), Mini.this);
            // Closing mini player on this instance actually closes persistent player
            persistentPlayer.setPlayWhenReady(false, PlayerConstants.Save.ONE_TIME_ONLY);
            // save scrubber position when mini is closed so we can continue when going back up to medium
            persistentPlayer.saveCurrentScrubberPosition();
            persistentPlayer.release();
            persistentPlayer.setType(null);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

    }

    public void show(FragmentManager supportFragmentManager) {
        onMiniScrollListener.show(supportFragmentManager);
    }

    public void hide(FragmentManager supportFragmentManager) {
        onMiniScrollListener.hide(supportFragmentManager);
    }

    public void changeMiniAlpha(float percent) {
        if (getView() != null){
            getView().setAlpha(1.0f - percent);
        }
    }

    @Override
    public boolean isPaused() {
        return persistentPlayer.isPaused();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {

            ViewGroup parentView = (ViewGroup) getView().getParent();
            parentView.setOnTouchListener(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        persistentPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        persistentPlayer.setPlayWhenReady(false);
    }

    @Override
    public void showHighlightMarker(Highlight highlight) {
    }

    @Override
    public void updateNativeShareInfo(NativeShareUtils.ShareInfo shareInfo) {
        this.shareInfo = shareInfo;
    }

    @Override
    public void setKebabClickable(boolean clickable) {

    }

    @Override
    public NativeShareUtils.ShareInfo getShareInfo() {
        return shareInfo;
    }

    @Override
    public PlayerView getPlayerView() {
        return exoPlayerView;
    }

    @Override
    public void showSignInToWatchOverlay(IOException error) {
    }

    @Override
    public boolean isShown() {
        return shown;
    }

    @Override
    public void showSignIn(IOException error) {

    }

    @Override
    public boolean isSignedIn() {
        return false;
    }

    @Override
    public void reset() {
        persistentPlayer.setPaused(false);
    }

    @Override
    public void showProgress(boolean show) {
        persistentPlayerView.showProgressBar(show);
    }

    @Override
    public void onSuccess(Auth auth) {

    }

    @Override
    public void enableSwitchScreen(boolean enable) {

    }

    @Override
    public void enableCloseButton(boolean enable) {

    }

    @Override
    public ViewGroup getPrimetimeView() {
        return persistentPlayerView.getPrimetimePlayerView();
    }

    @Override
    public void setMainPresenter(PersistentPlayerContract.Presenter mainPresenter) {

    }

    @Override
    public void receiveGoLiveState(boolean isPaused, boolean isGoLive) {
        persistentPlayer.setPaused(isPaused);
    }

    @Override
    public boolean isInGoLiveState() {
        return persistentPlayer.isGoLive();
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
    public void checkAuthAndPlay() {

    }

    @Override
    public boolean isAuthorized() {
        return false;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onComplete() {
        if (persistentPlayer.getType() == PlayerConstants.Type.MINI) {
            persistentPlayer.setPlayWhenReady(false);
            persistentPlayer.release();
            persistentPlayer.setType(null);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            hide(getActivity().getSupportFragmentManager());
        }
    }

    @Override
    public void onAdBreakStart(AdBreak adBreak) {
        persistentPlayer.setAdPlaying(true);
    }

    @Override
    public void onAdBreakComplete(AdBreak adBreak) {
        persistentPlayer.setAdPlaying(false);
    }

    public void updateSwitchScreen(MediaSource mediaSource) {

    }


    @Override
    public void updateSwitchScreenOrientation() {}

    public void setUpMiniOnTouchListener(boolean enable) {
        ViewGroup parentView = (ViewGroup) getView().getParent();
        if (enable){
            if (persistentPlayer.getPlayerEngine() != null) {
                onMiniDragListener.allowAlphaAnimation(persistentPlayer.getPlayerEngine().getConfig(ALPHA_ANIMATION));
            }
            parentView.setOnTouchListener(onMiniDragListener);
        } else {
            parentView.setOnTouchListener(null);
        }
    }

    /**
     * This method is used to control mini touch event
     * enable or disable
     *
     * @param enable
     */
    public void enableMiniOnTouchListener(boolean enable) {
        setUpMiniOnTouchListener(enable);
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "", "", "", "", "", "", "", "");
    }
}
