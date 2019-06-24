package com.nbcsports.regional.nbc_rsn.data_menu.datamenuftue

import com.nbcsports.regional.nbc_rsn.extensions.d
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver

class DataMenuFtueManager private constructor(){

    companion object {

        private val compositeDisposable = CompositeDisposable()

        fun addDisposable(disposableObserver: DisposableObserver<Long>?, teamName: String) {
            disposableObserver?.let {
                compositeDisposable.add(it)
                d(String.format("This is the enter point: data menu ftue add disposable succeeded: %s", teamName))
                return
            }
            d(String.format("This is the enter point: data menu ftue add disposable failed (null): %s", teamName))
        }

        fun removeDisposable(disposableObserver: DisposableObserver<Long>?, teamName: String) {
            disposableObserver?.let {
                if (!it.isDisposed){
                    compositeDisposable.remove(it)
                    d(String.format("This is the enter point: data menu ftue remove disposable succeeded: %s", teamName))
                    return
                }
            }
            d(String.format("This is the enter point: data menu ftue remove disposable failed (null or disposed): %s", teamName))
        }

        fun removeAllObservers() {
            compositeDisposable.clear()
            d("This is the enter point: data menu ftue all DOs are removed")
        }
    }
}