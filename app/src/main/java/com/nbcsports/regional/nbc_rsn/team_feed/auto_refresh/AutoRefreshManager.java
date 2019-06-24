package com.nbcsports.regional.nbc_rsn.team_feed.auto_refresh;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class AutoRefreshManager {

    private final static CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final static List<DisposableObserver<DateTime>> observersList = new ArrayList<>();

    private AutoRefreshManager() {}

    public static void addDisposable(@Nullable DisposableObserver<DateTime> disposableObserver, String teamName) {
        if (disposableObserver != null) {
            compositeDisposable.add(disposableObserver);
            observersList.add(disposableObserver);
            Timber.i("This is the enter point: add disposable succeeded: %s", teamName);
        } else {
            Timber.i("This is the enter point: add disposable failed (null): %s", teamName);
        }
    }

    public static void removeDisposable(@Nullable DisposableObserver<DateTime> disposableObserver, String teamName) {
        if (disposableObserver != null && !disposableObserver.isDisposed()) {
            compositeDisposable.remove(disposableObserver);
            observersList.remove(disposableObserver);
            Timber.i("This is the enter point: remove disposable succeeded: %s", teamName);
        } else {
            Timber.i("This is the enter point: remove disposable failed (null or disposed): %s", teamName);
        }
    }

    /**
     * Remove all live assets and team feed auto refresh observers
     * from composite disposable
     */
    public static void removeAllObservers() {
        compositeDisposable.clear();
        observersList.clear();
        Timber.i("This is the enter point: All live assets and team feed auto refresh DOs are removed");
    }

}
