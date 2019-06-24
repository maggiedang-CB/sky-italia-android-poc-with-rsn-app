package com.nbcsports.regional.nbc_rsn.data_bar

import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoDataBar

interface DataBarContract {
    interface View {
        fun showGameDayState(databar: RotoDataBar, gameState: GameState)
        fun showLiveGameState(databar: RotoDataBar, gameState: GameState)
        fun showPostGameState(databar: RotoDataBar, gameState: GameState)
        fun showUpcomingGameState(databar: RotoDataBar, gameState: GameState)
        fun showOffseasonState(databar: RotoDataBar)
        fun setPresenter(presenter: Presenter)
        fun hideDataBar()
        fun showDataBar()
    }

    interface Presenter {
        fun subscribe(view: View)
        fun handleData(databar: RotoDataBar?)
        fun showEvent(databar: RotoDataBar)
        fun unsubscribe()
    }
}