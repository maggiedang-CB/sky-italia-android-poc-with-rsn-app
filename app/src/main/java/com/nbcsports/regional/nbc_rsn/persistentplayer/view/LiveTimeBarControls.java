package com.nbcsports.regional.nbc_rsn.persistentplayer.view;

import android.view.View;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import lombok.Setter;
import timber.log.Timber;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Live TimeBar is disabled for now, not included for the 10/1 release
 *
 * Please use persistentPlayerView.getPlayerControlView() to get timeBarLive,
 * exoPositionLive and exoLiveLabel, because playerView is not working
 */
public class LiveTimeBarControls {

    private PersistentPlayerContract.View persistentPlayerView;
    private View playerView;
    private PersistentPlayer persistentPlayer;

    private DefaultTimeBar timeBarLive;
    private TextView exoPositionLive;
    private TextView exoLiveLabel;

    private CompositeDisposable compositeDisposable, compositeDisposable2;
    private DisposableObserver<Long> pollLivePlaybackObservable; // observes both for medium and landscape

    // current playback window and a position in it when used moved the scrubber
    protected long posWhenScrub = C.TIME_UNSET;
    protected int windowWhenScrub = -1;

    //TODO: Later, when we get info from NBC on structure of live playback,
    //TODO: we will remove the isLiveScrubWorkaround flag and probably
    //TODO: re-implement the  condition on scrub position behaviour.
    protected boolean isLiveScrubWorkaround = true; //= true; //= false;

    @Setter
    private boolean enableTimeBar;

    private LiveTimeBarControls() {}

    public LiveTimeBarControls(
            PersistentPlayerContract.View persistentPlayerView, View playerView, PersistentPlayer persistentPlayer) {
        init(persistentPlayerView, playerView, persistentPlayer);
    }

    public void init(PersistentPlayerContract.View persistentPlayerView, View playerView, PersistentPlayer persistentPlayer) {

        this.persistentPlayerView = persistentPlayerView;
        this.playerView           = playerView;
        this.persistentPlayer     = persistentPlayer;

        this.timeBarLive     = this.playerView.findViewById(R.id.exo_progress_live);
        this.exoPositionLive = this.playerView.findViewById(R.id.exo_position_live);
        this.exoLiveLabel    = this.playerView.findViewById(R.id.exo_live_label);

        if (enableTimeBar) {
            timeBarLive.setVisibility(VISIBLE);
            this.timeBarLive.addListener(new TimeBar.OnScrubListener() {
                @Override
                public void onScrubStart(TimeBar timeBarLive, long position) {
                }

                @Override
                public void onScrubMove(TimeBar timeBarLive, long position) {
                    //LiveTimeBarControls.this.persistentPlayerView.enforceThreeSecondRule();
                }

                @Override
                public void onScrubStop(TimeBar timeBarLive, long position, boolean canceled) {
                    LiveTimeBarControls.this.onScrubStop(position);
                }
            });
        } else {
            timeBarLive.setVisibility(INVISIBLE);
        }
    }

    public void onScrubStop(long position) {

        if (persistentPlayer.getPlayerEngine() == null){ return; }

        posWhenScrub = persistentPlayer.getPlayerEngine().getCurrentPosition();
        windowWhenScrub = persistentPlayer.getPlayerEngine().getCurrentWindowIndex();
        // update player position and have the timebar position indicator shown
        persistentPlayer.getPlayerEngine().seekTo(windowWhenScrub, position);

        if (!isLiveScrubWorkaround) {
            this.exoPositionLive.setVisibility(VISIBLE);
        }

        Timber.d("player.seekTo(%d, %d)", windowWhenScrub, position);
    }

    public void startPollingLivePlaybackState() {
        //if (compositeDisposable2 != null) { //TODO: check for null or isDisposed(), to avoid creating a new if one is in use already
        //if ( ! persistentPlayer.isMiniShown()) {
        if (persistentPlayer.getPlayerEngine() != null) {
            compositeDisposable2 = new CompositeDisposable();
            pollLivePlaybackObservable = Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
                    .takeWhile(positionInWindowMs -> isShownInTeamView())
                    .map(positionInMilliseconds -> persistentPlayer.getPlayerEngine().getCurrentPosition())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(setPositionLive());
            compositeDisposable2.add(pollLivePlaybackObservable);
        }
        //}
        //}
    }
    public void stopPollingLivePlaybackState() {
        if (compositeDisposable2 != null) {
            compositeDisposable2.dispose();

            //compositeDisposable2 = null; // It seems I cannot null the reference to CompositeDisposable? Would it cause an issue?
            // TODO: null or use isDisposed() to avoid creating a new if one is in use already
        }
    }
    private boolean isShownInTeamView() {
        //TODO predicate?
        return true;
    }

    private DisposableObserver<Long> setPositionLive() {
        return new DisposableObserver<Long>() {

            private Timeline.Period period = new Timeline.Period();

            @Override
            public void onNext(Long positionInWindowMs) { // position in the current window
                PlayerEngine.Interface player = LiveTimeBarControls.this.persistentPlayer.getPlayerEngine(); //??????????
                if (player != null) {
                    if (BuildConfig.DEBUG) {
                        logPlaybackTimingInTimber(player, positionInWindowMs);
                    }

                    // Show position in format mm:ss
                    long windowDurationMs = player.getDuration(); // duration of the current window in milliseconds, or C.TIME_UNSET if unknown
                    long restOfTimeInWindowMs =  windowDurationMs - positionInWindowMs;
                    String posStr = String.format("-%02d:%02d", Math.abs(restOfTimeInWindowMs)/1000/60, Math.abs(restOfTimeInWindowMs)/1000%60);
                    LiveTimeBarControls.this.exoPositionLive.setText(posStr);

                    if (!LiveTimeBarControls.this.isLiveScrubWorkaround) {
                        //TODO: Later, when we get info from NBC on structure of live playback,
                        //TODO: we will remove the isLiveScrubWorkaround flag and probably
                        //TODO: re-implement the following condition on scrub position behaviour.

                        // If the following condition is true, it ensures the scrub is at the very end of the live timebar.
                        // If the following condition is false (it's when user dragged scrub), it ensures the scrub is moving
                        // in the current playback's window as per playback progress, until it reaches the present time
                        // of playback, so that the scrub is at the very end of the live timebar.
                        if (player.getCurrentWindowIndex() != LiveTimeBarControls.this.windowWhenScrub ||
                                positionInWindowMs >= LiveTimeBarControls.this.posWhenScrub)
                        {
                            // Reset, so when playback switches to next window (if any), it ensures the condition is good
                            LiveTimeBarControls.this.posWhenScrub = C.TIME_UNSET;
                            LiveTimeBarControls.this.windowWhenScrub = -1;

                            // Update timebar UI to have the scrub at the very end of timebar and the position indicator hidden
                            LiveTimeBarControls.this.timeBarLive.setDuration(positionInWindowMs); // Note, if argument of the setDuration() is <= 0 then scrub is not shown at all
                            //LiveTimeBarControls.thistimeBarLive.setDuration(positionInWindowMs % 9000); // a hack: I noticed the position change of RedSox's live playback is no more than 9 sec or so, so try doing the trick
                            LiveTimeBarControls.this.exoPositionLive.setVisibility(INVISIBLE);
                        } else {
                            LiveTimeBarControls.this.exoPositionLive.setVisibility(VISIBLE);
                        }
                        LiveTimeBarControls.this.timeBarLive.setPosition(positionInWindowMs);
                    }
                }
            }

            private void logPlaybackTimingInTimber(PlayerEngine.Interface player, Long positionInWindowMs) {

                // Current window

                long windowDurationMs = player.getDuration(); // duration of the current window in milliseconds, or C.TIME_UNSET if unknown
                int currentWindowIndex = player.getCurrentWindowIndex();

                // Current period

                // See https://github.com/google/ExoPlayer/issues/2118 on an issue/solution about getCurrentPosition
                long positionInPeriodMs = 0L;
                long periodDurationMs = 0L;
                // Adjust position to be relative to start of period rather than window.
                Timeline currentTimeline = player.getCurrentTimeline();
                if (!currentTimeline.isEmpty()) {
                    currentTimeline.getPeriod(player.getCurrentPeriodIndex(), period); // gets period
                    positionInPeriodMs = positionInWindowMs - period.getPositionInWindowMs();
                    periodDurationMs = period.getDurationMs(); // duration of the period in milliseconds, or C.TIME_UNSET if unknown
                }
                int currentPeriodIndex = player.getCurrentPeriodIndex();

                // Make a string about playback timing to log in Timber
                String posStrForTimber = // in seconds
                        "pos " + Long.toString(positionInWindowMs/1000) + " of " +
                                (windowDurationMs != C.TIME_UNSET ? Long.toString(windowDurationMs/1000) : "TIME_UNSET") +
                                " in window #" + currentWindowIndex +
                                ", pos " + Long.toString(positionInPeriodMs/1000) + " of " +
                                (periodDurationMs != C.TIME_UNSET ? Long.toString(periodDurationMs/1000) : "TIME_UNSET") +
                                " in period #" + currentPeriodIndex +
                                ", pos " + (LiveTimeBarControls.this.posWhenScrub != C.TIME_UNSET ? Long.toString(LiveTimeBarControls.this.posWhenScrub /1000) : "UNSET") +
                                " when user moved scrubber in window #" + (LiveTimeBarControls.this.windowWhenScrub != -1 ? LiveTimeBarControls.this.windowWhenScrub : "UNSET");

                // log it
                Timber.d("%s", posStrForTimber);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }


        };
    }

    // the findViewById in init() did not work when swipe the team into view.
    // all the live UI will now show.
    public void show(boolean show){
        View view = persistentPlayerView.getPlayerControlView();
        if (view == null){ return; }
        this.timeBarLive = view.findViewById(R.id.exo_progress_live);
        this.exoPositionLive = view.findViewById(R.id.exo_position_live);
        this.exoLiveLabel = view.findViewById(R.id.exo_live_label);

        if (show && !persistentPlayer.isAdPlaying()){
            exoPositionLive.setVisibility(INVISIBLE);
            exoLiveLabel.setVisibility(VISIBLE);
            setExoLiveLabel(persistentPlayer.isGoLive() ?
                    LocalizationManager.VideoPlayer.GoLive : LocalizationManager.VideoPlayer.Live);
            if (enableTimeBar) timeBarLive.setVisibility(VISIBLE);
        } else {
            exoPositionLive.setVisibility(INVISIBLE);
            exoLiveLabel.setVisibility(INVISIBLE);
            if (enableTimeBar) timeBarLive.setVisibility(INVISIBLE);
        }
    }

    // Since LiveTimeBarControls is a singleton, so whenever medium -> landscape
    // Or landscape -> medium, we need to get the correct view
    private void setExoLiveLabel(String exoLiveLabelText) {
        View view = persistentPlayerView.getPlayerControlView();
        if (view != null){
            exoLiveLabel = view.findViewById(R.id.exo_live_label);
        }
        exoLiveLabel.setText(exoLiveLabelText);
    }

    // Since LiveTimeBarControls is a singleton, so whenever medium -> landscape
    // Or landscape -> medium, we need to get the correct view
    private void bindExoLiveLabelOnClickAction() {
        View view = persistentPlayerView.getPlayerControlView();
        if (view != null){
            exoLiveLabel = view.findViewById(R.id.exo_live_label);
        }
        if (compositeDisposable == null){
            compositeDisposable = new CompositeDisposable();
            compositeDisposable.add(RxView.clicks(exoLiveLabel)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v -> {
                        if (persistentPlayer.getPlayerEngine() != null) {
                            persistentPlayer.getPlayerEngine().seekToDefaultPosition();
                            persistentPlayer.setGoLive(false);
                            persistentPlayer.setPaused(false);
                            persistentPlayer.setPlayWhenReady(true, PlayerConstants.Save.REMEMBER);
                        }
                        setExoLiveLabel(LocalizationManager.VideoPlayer.Live);
                        unbindExoLiveLabelOnClickAction();
                    }));
        } else {
            unbindExoLiveLabelOnClickAction();
            bindExoLiveLabelOnClickAction();
        }
    }

    private void unbindExoLiveLabelOnClickAction() {
        if (compositeDisposable != null){
            if (!compositeDisposable.isDisposed()){
                compositeDisposable.dispose();
                compositeDisposable = null;
            }
        }
    }

    public void reset() {
        unbindExoLiveLabelOnClickAction();
        setExoLiveLabel(LocalizationManager.VideoPlayer.Live);
    }

    public void setStateToGoLive() {
        persistentPlayer.setGoLive(true);
        setExoLiveLabel(LocalizationManager.VideoPlayer.GoLive);
        bindExoLiveLabelOnClickAction();
    }

    public boolean isInGoLiveState() {
        return persistentPlayer.isGoLive();
    }

}
