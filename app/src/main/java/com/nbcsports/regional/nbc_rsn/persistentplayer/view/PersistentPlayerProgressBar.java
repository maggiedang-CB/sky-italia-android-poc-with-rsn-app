package com.nbcsports.regional.nbc_rsn.persistentplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class PersistentPlayerProgressBar extends ProgressBar {

    private long PROGRESS_BAR_CHECK_INTERVAL = 1L;

    public PersistentPlayerProgressBar(Context context) {
        super(context);
    }

    public PersistentPlayerProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PersistentPlayerProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PersistentPlayerProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * This method is used to create a progress bar disposable observer
     *
     * The observer will going to check whether it is time to show the
     * progress bar on video player for every PROGRESS_BAR_CHECK_INTERVAL
     * second
     *
     * @param persistentPlayer
     * @param ppcView
     * @return DisposableObserver<Long>
     */
    public DisposableObserver<Long> getProgressBarDO(PersistentPlayer persistentPlayer, PersistentPlayerContract.View ppcView) {
        return Observable.interval(PROGRESS_BAR_CHECK_INTERVAL, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .skipWhile(aLong -> persistentPlayer == null)
                .skipWhile(aLong -> ppcView == null)
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        if (persistentPlayer.getPlayerEngine() != null) {
                            // persistentPlayer.getPlayerEngine().log();
                            if (persistentPlayer.getPlayerEngine().isAllowToShowLoadingProgressBar()){
                                //Timber.d("Player loading animation: showing loading spinner is allowed");
                                // Show loading animation here
                                ppcView.showProgress(true);
                            } else {
                                //Timber.d("Player loading animation: showing loading spinner is declined");
                                // Hide loading animation here
                                ppcView.showProgress(false);
                            }
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }
                    @Override
                    public void onComplete() {}
                });
    }
}
