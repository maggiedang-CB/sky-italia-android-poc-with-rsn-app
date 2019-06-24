package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.view.View;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class OnCloseClickListener implements View.OnClickListener {

    private final PersistentPlayer persistentPlayer;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public OnCloseClickListener(PersistentPlayer persistentPlayer) {

        this.persistentPlayer = persistentPlayer;
    }

    @Override
    public void onClick(View view) {
        // reset back to portrait so that landscape to medium can be triggered
        persistentPlayer.resetOrientation();

        if (persistentPlayer.is247()){
            persistentPlayer.showAsLandscape(false);
            persistentPlayer.set247(false);
            persistentPlayer.release();
        }
        // after 2 seconds
        // if current layout is medium, use orientation sensor
        // if current type is 247, use orientation portrait
        compositeDisposable.add(Completable.timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (persistentPlayer.getType() == PlayerConstants.Type.MEDIUM) {
                        persistentPlayer.setOrientationSensor();
                    } else if (persistentPlayer.is247()) {
                        persistentPlayer.resetOrientation();
                    }
                }));
    }

    public void reset() {
        compositeDisposable.dispose();
    }
}
