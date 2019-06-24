package com.nbcsports.regional.nbc_rsn.authentication;

import android.content.Context;

import com.nbcsports.regional.nbc_rsn.chromecast.ChromecastAuthorizationListener;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

import java.io.IOException;

import io.reactivex.observers.DisposableObserver;

public interface StreamAuthenticationContract {

    interface View {
        Context getContext();

        void setPresenter(Presenter presenter);

        Presenter getPresenter();
    }

    interface Presenter {

        void checkAuthAndPlay(Context context, PersistentPlayerContract.View currentView, MediaSource mediaSource);

        void checkAuthNStatus(String requestorId, DisposableObserver<Auth> disposableObserver);

        void pollAuthNToken(String requestorId, MediaSource mediaSource, TokenListener listener);

        void pollAuthNtokenStop();

        void checkTempPassGeoBlock(MediaSource mediaSource, TokenListener listener);

        void doAuthN(String mvpdID, String requestorId);

        void doLogout(String authorizedRequestorId, DisposableObserver<Auth> disposableObserver);

        boolean isAuthorized();

        void setAuthorized(boolean authorized);

        boolean isStarted();

        boolean isAuthenticated();

        boolean isSignedIn();

        void setView(StreamAuthenticationFragment streamAuthenticationFragment);

        void setConfig(Config config);

        Config getConfig();

        String getAuthorizedRequestorId();

        void resetTempPass(String authorizedRequestorId, DisposableObserver<Auth> disposableObserver);

        Auth getLastKnownAuth();

        void showSignInToWatchOverlay(PersistentPlayer persistentPlayer,
                                      PersistentPlayerView playerView,
                                      MediaSource mediaSource,
                                      android.view.View persistentPlayerView,
                                      int teamPrimaryColor,
                                      IOException error);

        void chromecastCheckAuthAndPlay(MediaSource mediaSource,
                                        ChromecastAuthorizationListener chromecastAuthorizationListener);
    }
}
