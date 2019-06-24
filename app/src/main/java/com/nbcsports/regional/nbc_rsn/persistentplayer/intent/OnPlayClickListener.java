package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.view.MotionEvent;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.LiveTimeBarControls;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

public class OnPlayClickListener implements View.OnTouchListener {

    private final PersistentPlayer persistentPlayer;
    private final PersistentPlayerView persistentPlayerView;
    private final LiveTimeBarControls liveControls;

    public OnPlayClickListener(PersistentPlayer persistentPlayer,
                               PersistentPlayerView persistentPlayerView,
                               LiveTimeBarControls liveControls) {

        this.persistentPlayer = persistentPlayer;
        this.persistentPlayerView = persistentPlayerView;
        this.liveControls = liveControls;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (persistentPlayer.isAdPlaying()){
            // Do nothing?
        } else if (persistentPlayer.isLive()
                    && persistentPlayer.isPaused()
                    && liveControls != null) {
                liveControls.setStateToGoLive();
        }
        persistentPlayer.setPaused(false);
        persistentPlayer.setPlayWhenReady(true, PlayerConstants.Save.REMEMBER);
        persistentPlayerView.enforceThreeSecondRule();
        return false;
    }
}
