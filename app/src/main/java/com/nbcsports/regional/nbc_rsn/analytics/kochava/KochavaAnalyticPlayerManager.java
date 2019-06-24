package com.nbcsports.regional.nbc_rsn.analytics.kochava;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class KochavaAnalyticPlayerManager {

    public static String KOCHAVA_ANALYTICS_VIDEO_PLAYING = "playing";
    public static String KOCHAVA_ANALYTICS_VIDEO_PAUSE   = "pause";
    public static String KOCHAVA_ANALYTICS_VIDEO_DONE    = "done";

    private static long KOCHAVA_ANALYTIC_PLAYER_CHECK_INTERVAL = 1L;

    private static long secondsSpentOnWatching = 0;

    private static boolean prerollAdDetected = false;

    private static CompositeDisposable compositeDisposable;

    private static List<DisposableObserver<Long>> observersList;

    private static List<String> eventsList;

    private KochavaAnalyticPlayerManager() {}

    /**
     * This method is used to reset variables
     */
    private static void reset() {
        secondsSpentOnWatching = 0;
        prerollAdDetected      = false;
    }

    /**
     * This method is used as following
     * 1. Check if secondsSpentOnWatching > 0
     * 2. If so, it means the last duration event on KOCHAVA_ANALYTICS_VIDEO_DONE
     *    is not sent out yet. So send it out here
     *
     * @param persistentPlayer
     * @param kochavaContext
     */
    private static void sendAppropriateVideoDurationEventIfAvailable(
            PersistentPlayer persistentPlayer, KochavaContract.View kochavaContext) {
        if (secondsSpentOnWatching > 0 && persistentPlayer != null && kochavaContext != null){
            // Get KochavaContract.Presenter instance
            KochavaContract.Presenter kochava = KochavaAnalytic.Injection
                    .provideKochava(kochavaContext);
            // Check if kochava from above is null or not
            // If it is not null, then send appropriate video duration event
            if (kochava != null){
                // Send appropriate video duration event
                kochava.sendAppropriateVideoDurationEvent(persistentPlayer.getMediaSource(),
                        secondsSpentOnWatching);
            }
        }
    }

    /**
     * This method is used to add kochava analytic observer into
     * compositeDisposable and observersList for management
     *
     * @param kochavaAnalyticDO
     */
    public static void addKochavaAnalyticObserver(DisposableObserver<Long> kochavaAnalyticDO) {
        // Check if composite disposable is null
        if (compositeDisposable == null){
            compositeDisposable = new CompositeDisposable();
        }
        // Check if observers list is null
        if (observersList == null){
            observersList = new ArrayList<>();
        }
        // Add kochava analytic disposable observer into
        // composite disposable and observers list
        if (kochavaAnalyticDO != null){
            Timber.d("Kochava player: Start adding kochava analytic observer...");
            if (!observersList.contains(kochavaAnalyticDO)){
                compositeDisposable.add(kochavaAnalyticDO);
                observersList.add(kochavaAnalyticDO);
                Timber.d("Kochava player: Success in adding kochava analytic observer");
            } else {
                Timber.d("Kochava player: Fail in adding kochava analytic observer (already exist)");
            }
        }
    }

    /**
     * This method is used to remove kochava analytic observer from
     * compositeDisposable and observersList for management
     *
     * @param kochavaAnalyticDO
     */
    public static void removeKochavaAnalyticObserver(DisposableObserver<Long> kochavaAnalyticDO) {
        // Check if composite disposable is null
        if (compositeDisposable == null) return;
        // Remove kochava analytic disposable observer from
        // composite disposable and observer list
        if (kochavaAnalyticDO != null && !kochavaAnalyticDO.isDisposed()){
            Timber.d("Kochava player: Start removing kochava analytic observer...");
            compositeDisposable.remove(kochavaAnalyticDO);
            if (eventsList != null){
                eventsList.clear();
            }
            if (observersList != null){
                observersList.remove(kochavaAnalyticDO);
                Timber.d("Kochava player: Success in removing kochava analytic observer");
            } else {
                Timber.d("Kochava player: Fail in removing progress bar observer (observersList is null)");
            }
        }
    }

    /**
     * This method is used to remove all kochava analytic observers from
     * compositeDisposable and observersList
     * and clear eventsList
     * and call method sendAppropriateVideoDurationEventIfAvailable(...)
     * and reset all variables
     *
     * @param persistentPlayer
     * @param kochavaContext
     */
    public static void removeAllObservers(
            PersistentPlayer persistentPlayer, KochavaContract.View kochavaContext) {
        if (compositeDisposable != null){
            compositeDisposable.clear();
        }
        if (observersList != null){
            observersList.clear();
        }
        if (eventsList != null){
            eventsList.clear();
        }
        sendAppropriateVideoDurationEventIfAvailable(persistentPlayer, kochavaContext);
        reset();
        Timber.d("Kochava player: All Kochava DOs are removed and variables are reset");
    }

    /**
     * This method is used to create a new kochava analytic disposable observer
     *
     * @param persistentPlayer
     * @param kochava
     * @return DisposableObserver<Long>
     */
    public static DisposableObserver<Long> getKochavaAnalyticDO(PersistentPlayer persistentPlayer, KochavaContract.Presenter kochava) {
        return Observable.interval(KOCHAVA_ANALYTIC_PLAYER_CHECK_INTERVAL, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .skipWhile(aLong -> persistentPlayer == null)
                .skipWhile(aLong -> kochava == null)
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        if (persistentPlayer.getPlayerEngine() != null){
                            //persistentPlayer.getPlayerEngine().log();
                            if (!prerollAdDetected){
                                // Set up events list
                                if (eventsList == null){
                                    eventsList = new ArrayList<>();
                                }
                                //Timber.d("Kochava player: eventsList: %s", eventsList);
                                // Get kochava analytic player status
                                String currentStatusString = persistentPlayer.getPlayerEngine()
                                        .getKochavaAnalyticPlayerStatus();
                                // Check the current status
                                // 1. If it is KOCHAVA_ANALYTICS_VIDEO_PLAYING
                                // 2. If it is KOCHAVA_ANALYTICS_VIDEO_PAUSE
                                // 3. If it is KOCHAVA_ANALYTICS_VIDEO_DONE
                                if (currentStatusString.equalsIgnoreCase(KOCHAVA_ANALYTICS_VIDEO_PLAYING)){
                                    // Increase secondsSpentOnWatching
                                    secondsSpentOnWatching += 1;
                                    // Check if events list is empty
                                    // 1. If it is empty then add KOCHAVA_ANALYTICS_VIDEO_PLAYING
                                    //    into events list and send appropriate video start event
                                    //    to Kochava
                                    // 2. If the last event in events list is
                                    //    KOCHAVA_ANALYTICS_VIDEO_PAUSE
                                    //    then add KOCHAVA_ANALYTICS_VIDEO_PLAYING
                                    //    into events list
                                    if (eventsList.isEmpty()){
                                        eventsList.add(KOCHAVA_ANALYTICS_VIDEO_PLAYING);
                                        // Send play start event
                                        kochava.sendAppropriateVideoStartEvent(persistentPlayer.getMediaSource());
                                    } else if (eventsList.get(eventsList.size()-1).equalsIgnoreCase(KOCHAVA_ANALYTICS_VIDEO_PAUSE)){
                                        eventsList.add(KOCHAVA_ANALYTICS_VIDEO_PLAYING);
                                    }
                                } else if (currentStatusString.equalsIgnoreCase(KOCHAVA_ANALYTICS_VIDEO_PAUSE)){
                                    // Check if secondsSpentOnWatching > 0
                                    // and if the last event in events list is
                                    // KOCHAVA_ANALYTICS_VIDEO_PLAYING
                                    // If so, send appropriate stream duration
                                    // and reset secondsSpentOnWatching to 0
                                    // and add KOCHAVA_ANALYTICS_VIDEO_PAUSE into events list
                                    if (secondsSpentOnWatching > 0
                                            && !eventsList.isEmpty()
                                            && eventsList.get(eventsList.size()-1).equalsIgnoreCase(KOCHAVA_ANALYTICS_VIDEO_PLAYING)){
                                        eventsList.add(KOCHAVA_ANALYTICS_VIDEO_PAUSE);
                                        kochava.sendAppropriateVideoDurationEvent(persistentPlayer.getMediaSource(),
                                                secondsSpentOnWatching);
                                        secondsSpentOnWatching = 0;
                                    }
                                } else if (currentStatusString.equalsIgnoreCase(KOCHAVA_ANALYTICS_VIDEO_DONE)){
                                    // Do nothing now
                                    if (secondsSpentOnWatching > 0
                                            && !eventsList.isEmpty()
                                            && !eventsList.get(eventsList.size()-1).equalsIgnoreCase(KOCHAVA_ANALYTICS_VIDEO_DONE)){
                                        eventsList.add(KOCHAVA_ANALYTICS_VIDEO_DONE);
                                        kochava.sendAppropriateVideoDurationEvent(persistentPlayer.getMediaSource(),
                                                secondsSpentOnWatching);
                                        secondsSpentOnWatching = 0;
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onError(Throwable e) {Timber.e(e);}
                    @Override
                    public void onComplete() {}
                });
    }

    /**
     * This method is used to set prerollAdDetected
     *
     * @param isPrerollAdDetected
     */
    public static void setPrerollAdDetected(boolean isPrerollAdDetected) {
        prerollAdDetected = isPrerollAdDetected;
        Timber.d("Kochava player: prerollAdDetected set to: %s", prerollAdDetected);
    }

    /**
     * This method is the enter point of live stream and VOD status checking
     *
     * @param persistentPlayer
     * @param kochavaContext
     */
    public static void startVideoTracking(PersistentPlayer persistentPlayer, KochavaContract.View kochavaContext) {
        if (persistentPlayer != null && kochavaContext != null){
            // Get KochavaContract.Presenter instance
            KochavaContract.Presenter kochava = KochavaAnalytic.Injection
                    .provideKochava(kochavaContext);
            // Get a new kochavaAnalyticDO with KochavaContract.Presenter instance
            // and PersistentPlayer
            DisposableObserver<Long> kochavaAnalyticDO = KochavaAnalyticPlayerManager
                    .getKochavaAnalyticDO(persistentPlayer, kochava);
            // Add the new kochavaAnalyticDO into compositeDisposable
            // and observersList
            addKochavaAnalyticObserver(kochavaAnalyticDO);
        }
    }

}
