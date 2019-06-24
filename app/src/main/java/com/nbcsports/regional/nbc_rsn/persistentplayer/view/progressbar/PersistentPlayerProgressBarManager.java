package com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class PersistentPlayerProgressBarManager {

    private static CompositeDisposable compositeDisposable;

    private static List<DisposableObserver<Long>> observersList;

    private PersistentPlayerProgressBarManager() {}

    public static void addProgressBarObserver(DisposableObserver<Long> progressBarDO,
                                              int hashCode) {
        // Check if composite disposable is null
        if (compositeDisposable == null){
            compositeDisposable = new CompositeDisposable();
        }
        // Check if observer list is null
        if (observersList == null){
            observersList = new ArrayList<>();
        }
        // Add progress bar disposable observer into
        // composite disposable and observers list
        if (progressBarDO != null){
            Timber.d("Player loading animation: Start adding progress bar observer...");
            if (!observersList.contains(progressBarDO)){
                compositeDisposable.add(progressBarDO);
                observersList.add(progressBarDO);
                Timber.d("Player loading animation: Success in adding progress bar observer: %d",
                        hashCode);
            } else {
                Timber.d("Player loading animation: Fail in adding progress bar observer: %d (already exist)",
                        hashCode);
            }
        }
    }

    public static void removeProgressBarObserver(DisposableObserver<Long> progressBarDO,
                                                 int hashCode) {
        // Check if composite disposable is null
        if (compositeDisposable == null) return;
        // Remove progress bar disposable observer from
        // composite disposable and observer list
        if (progressBarDO != null && !progressBarDO.isDisposed()){
            Timber.d("Player loading animation: Start removing progress bar observer...");
            compositeDisposable.remove(progressBarDO);
            if (observersList != null){
                observersList.remove(progressBarDO);
                Timber.d("Player loading animation: Success in removing progress bar observer: %d",
                        hashCode);
            } else {
                Timber.d("Player loading animation: Fail in removing progress bar observer: %d (observersList is null)",
                        hashCode);
            }
        }
    }

    public static void removeAllObservers() {
        if (compositeDisposable != null){
            compositeDisposable.clear();
        }
        if (observersList != null){
            observersList.clear();
        }
        Timber.d("Player loading animation: All progress bar DOs are removed");
    }

}
