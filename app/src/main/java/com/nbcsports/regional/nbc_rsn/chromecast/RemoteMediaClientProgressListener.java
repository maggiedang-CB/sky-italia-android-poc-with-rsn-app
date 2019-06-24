package com.nbcsports.regional.nbc_rsn.chromecast;

import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;

/* package */ class RemoteMediaClientProgressListener implements RemoteMediaClient.ProgressListener {

    private PersistentPlayer persistentPlayer;

    public RemoteMediaClientProgressListener(final PersistentPlayer persistentPlayer) {
        this.persistentPlayer = persistentPlayer;
    }

    @Override
    public void onProgressUpdated(final long progressMs, final long durationMs) {
        if (persistentPlayer.getPlayerEngine() != null) {
            persistentPlayer.getPlayerEngine().seekTo(progressMs);
        }
    }
}