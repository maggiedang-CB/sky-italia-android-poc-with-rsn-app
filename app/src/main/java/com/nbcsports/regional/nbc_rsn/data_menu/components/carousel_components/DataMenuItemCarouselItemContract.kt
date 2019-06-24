package com.nbcsports.regional.nbc_rsn.data_menu.components.carousel_components

import com.nbcsports.regional.nbc_rsn.common.Team

interface DataMenuItemCarouselItemContract {

    interface View {
        fun setPresenter(presenter: Presenter)
        fun setScale(scaleFactor: Float)
    }

    interface Presenter {
        fun setUpCircleProgressBarColor(team: Team?, vararg views: android.view.View?)
    }

}