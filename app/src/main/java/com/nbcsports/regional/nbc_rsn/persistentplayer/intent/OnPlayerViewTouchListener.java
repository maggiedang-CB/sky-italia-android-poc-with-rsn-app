package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.view.MotionEvent;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

import static android.view.View.VISIBLE;

public class OnPlayerViewTouchListener implements View.OnTouchListener {

    private final PersistentPlayerView persistentPlayerView;
    private final StreamAuthenticationContract.Presenter streamAuthenticationPresenter;
    private final MediaSource mediaSource;

    public OnPlayerViewTouchListener(PersistentPlayerView persistentPlayerView,
                                     StreamAuthenticationContract.Presenter streamAuthenticationPresenter,
                                     MediaSource mediaSource) {
        this.persistentPlayerView = persistentPlayerView;
        this.streamAuthenticationPresenter = streamAuthenticationPresenter;
        this.mediaSource = mediaSource;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (persistentPlayerView.getPlayerControlView().getVisibility() != VISIBLE) {
            persistentPlayerView.showControllers(true, PlayerConstants.Controller.Mode.AUTO_HIDE);
        }
        if (mediaSource != null && mediaSource.getLive()
                && streamAuthenticationPresenter.getLastKnownAuth() != null
                && ( streamAuthenticationPresenter.getLastKnownAuth().isTempPassAuthN(streamAuthenticationPresenter.getConfig())
                || streamAuthenticationPresenter.getLastKnownAuth().isTempPass(streamAuthenticationPresenter.getConfig()))) {

            persistentPlayerView.getTempPassCountdownContainer().setVisibility(VISIBLE);
        }
        return true;
    }
}
