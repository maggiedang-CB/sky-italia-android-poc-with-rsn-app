package com.nbcsports.regional.nbc_rsn.persistentplayer;

import android.content.Context;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ui.PlayerView;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.BaseView;
import com.nbcsports.regional.nbc_rsn.common.Highlight;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.TimelineMarker;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;

import java.io.IOException;
import java.util.List;

public interface PersistentPlayerContract {

    interface View extends BaseView<Presenter> {

        void showHighlightMarker(Highlight highlight);

        void updateNativeShareInfo(NativeShareUtils.ShareInfo shareInfo);

        void setKebabClickable(boolean clickable);

        NativeShareUtils.ShareInfo getShareInfo();

        PlayerView getPlayerView();

        void showSignInToWatchOverlay(IOException error);

        boolean isShown();

        boolean isPaused();

        void showSignIn(IOException error);

        boolean isSignedIn();

        void reset();

        void showProgress(boolean show);

        void onSuccess(Auth auth);

        void enableSwitchScreen(boolean enable);

        void enableCloseButton(boolean enable);

        void updateSwitchScreen(MediaSource mediaSource);

        void updateSwitchScreenOrientation();

        ViewGroup getPrimetimeView();

        void receiveGoLiveState(boolean isPaused, boolean isGoLive);

        boolean isInGoLiveState();

        android.view.View getPlayerControlView();

        PersistentPlayerView getPersistentPlayerView();

        boolean showStandalonePlayButton();

        void showPlayerControlView();

        void checkAuthAndPlay();

        boolean isAuthorized();
    }

    interface Presenter {
        void play(String url);

        void getHighlightData(MediaSource mediaSource);

        void setProgramStartTime(long startTimeUs);

        long getProgramStartTime();

        List<TimelineMarker> getTimelineMarkers();
    }

    interface Main {

        interface View {

            PersistentPlayer getPersistentPlayer();

            void showMini();

            void hideMini();

            void fadeInAndFadeOurMini(float percent);

            void showLandscape(PlayerConstants.Type source);

            void hideLandscape();

            Context getContext();

            void closeStreamAuthenticationFragment();
        }
    }
}
