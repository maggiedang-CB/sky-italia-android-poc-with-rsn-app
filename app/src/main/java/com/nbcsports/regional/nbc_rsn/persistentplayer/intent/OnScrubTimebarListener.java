package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import com.google.android.exoplayer2.ui.TimeBar;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.PersistentPlayerView;

import lombok.Getter;
import lombok.Setter;

public class OnScrubTimebarListener implements TimeBar.OnScrubListener {

    private final PersistentPlayerView persistentPlayerView;
    @Setter
    private PersistentPlayer persistentPlayer;
    @Getter
    private long seekToPosition;

    public OnScrubTimebarListener(PersistentPlayerView persistentPlayerView) {
        this.persistentPlayerView = persistentPlayerView;
    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        persistentPlayerView.stopTimebar();
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
        persistentPlayerView.updateTimebar(position);
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        seekToPosition = position;
        if (persistentPlayer != null && persistentPlayer.getPlayerEngine() != null){
            persistentPlayer.getPlayerEngine().seekTo(position);
        }
        persistentPlayerView.showProgressBar(true);
        persistentPlayerView.startTimebar();
        persistentPlayerView.enforceThreeSecondRule();
    }
}
