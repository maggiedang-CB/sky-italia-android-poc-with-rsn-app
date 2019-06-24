package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.view.MotionEvent;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.LiveTimeBarControls;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

public class OnPauseClickListener implements View.OnTouchListener {

    private final PersistentPlayer persistentPlayer;
    private final PersistentPlayerView persistentPlayerView;
    private final LiveTimeBarControls liveControls;

    public OnPauseClickListener(PersistentPlayer persistentPlayer,
                               PersistentPlayerView persistentPlayerView,
                               LiveTimeBarControls liveControls) {

        this.persistentPlayer = persistentPlayer;
        this.persistentPlayerView = persistentPlayerView;
        this.liveControls = liveControls;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // if chromecast is connected
//        if (persistentPlayer.getCastContext().getCastState() == CastState.CONNECTED){
//            CastSession castSession = persistentPlayer.getCastContext().getSessionManager().getCurrentCastSession();
//            final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
//            remoteMediaClient.pause();
//            persistentPlayer.showPlayOrPause(false);
//
//            return false;
//        }

        if (persistentPlayer.isAdPlaying()){
            // Do nothing?
        } else if (persistentPlayer.isLive() && liveControls != null){
            liveControls.setStateToGoLive();
        }
        persistentPlayer.setPaused(true);
        persistentPlayer.setPlayWhenReady(false, PlayerConstants.Save.REMEMBER);
        persistentPlayerView.enforceThreeSecondRule();
        return false;
    }
}
