package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.view.MotionEvent;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class OnControlViewTouchListener implements View.OnTouchListener {

    private final PersistentPlayerView persistentPlayerView;
    private final StreamAuthenticationContract.Presenter streamAuthenticationPresenter;

    public OnControlViewTouchListener(PersistentPlayerView persistentPlayerView,
                                      StreamAuthenticationContract.Presenter streamAuthenticationPresenter) {
        this.persistentPlayerView = persistentPlayerView;
        this.streamAuthenticationPresenter = streamAuthenticationPresenter;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (persistentPlayerView.getPlayerControlView().getVisibility() == VISIBLE) {
            persistentPlayerView.showControllers(false);
        }
        if (streamAuthenticationPresenter.getLastKnownAuth() != null
                && (streamAuthenticationPresenter.getLastKnownAuth().isTempPassAuthN(streamAuthenticationPresenter.getConfig())
                || streamAuthenticationPresenter.getLastKnownAuth().isTempPass(streamAuthenticationPresenter.getConfig()))) {

            persistentPlayerView.getTempPassCountdownContainer().setVisibility(GONE);
        }
        return true;
    }
}
