package com.nbcsports.regional.nbc_rsn.data_bar

import com.nbcsports.regional.nbc_rsn.common.Constants
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoDataBar
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.lang.ref.WeakReference

class DataBarPresenter : DataBarContract.Presenter {

    private var view: WeakReference<DataBarContract.View>? = null

    override fun subscribe(view: DataBarContract.View) {
        this.view = WeakReference(view)
        this.view?.get()?.setPresenter(this)
    }

    override fun handleData(databar: RotoDataBar?) {
        if (databar == null) return

        if (!PreferenceUtils.getBoolean(Constants.PREF_KEY_DATABAR_ENABLED, DataBarManager?.isDataBarEnabled!!)) {
            view?.get()?.hideDataBar()
            return
        }

        if (databar.isSeasonActive()) {
            showEvent(databar)
        } else {
            view?.get()?.showOffseasonState(databar)
        }
    }

    override fun showEvent(databar: RotoDataBar) {

        val today = DateTime().withTimeAtStartOfDay()
        val event = DateTime(databar.gameDateTimeUTC).withZone(DateTimeZone.UTC).withZone(today.zone).withTimeAtStartOfDay()

        // check if they are on same day
        if (event.isAfter(today)) {
            view?.get()?.showUpcomingGameState(databar = databar, gameState = GameState.PRE_GAME)
        } else {
            when (databar.gameStatusId) {
                // Pre-Game
                GameState.PRE_GAME.eid -> view?.get()?.showGameDayState(databar = databar, gameState = GameState.PRE_GAME)
                GameState.POSTPONED.eid -> view?.get()?.showGameDayState(databar = databar, gameState = GameState.POSTPONED)

                // Live-Game
                GameState.IN_PROGRESS.eid -> view?.get()?.showLiveGameState(databar = databar, gameState = GameState.IN_PROGRESS)
                GameState.DELAYED.eid -> view?.get()?.showLiveGameState(databar = databar, gameState = GameState.DELAYED)

                // Post-Game
                GameState.SUSPENDED.eid -> view?.get()?.showPostGameState(databar = databar, gameState = GameState.SUSPENDED)
                GameState.CANCELLED.eid -> view?.get()?.showPostGameState(databar = databar, gameState = GameState.CANCELLED)
                GameState.FINAL.eid -> view?.get()?.showPostGameState(databar = databar, gameState = GameState.FINAL)

                // Unknown State
                else -> view?.get()?.hideDataBar()
            }
        }
    }

    override fun unsubscribe() {
        view?.clear()
        view = null
    }
}
