package com.nbcsports.regional.nbc_rsn.data_menu

import android.os.Parcelable
import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewDataModel
import com.nbcsports.regional.nbc_rsn.data_menu.models.PeriodDetail

interface DataMenuContract {

    interface View {
        fun setPresenter(presenter: Presenter)
        fun setDataMenuColor(percent: Float)
        fun setMainRecyclerViewData(itemList: List<DataMenuOverviewDataModel>)
        fun isDataBarGameStateOffseason(): Boolean
        fun getConfig(): Config?
        fun refreshAdapterContent(position: Int)
    }

    interface Presenter {
        fun subscribe(team: Team?)
        fun unsubscribe()
    }

    interface DataManager {
    }

    /**
     * Instead of extending this class directly, please use one of the interfaces from
     * file DataMenuDataWatchers.kt
     *
     * If want to add a new type of watcher, please add it to the same file and make sure
     * it is registered in [DataMenuDataManager]
     */
    interface DataWatcher<T: Parcelable> {
        fun onDataReady(data: T)
    }

    interface ScheduleFilterListener {
        fun getFilterOptions(): List<String>
        fun getStatsTeamId(): Int
        fun getInitFilterIndex(): Int
        fun onNewFilterSelected(position: Int)
    }

    interface ScheduleList {
        fun notifyDataSetUpdated()
    }

    interface ScoreCardFactory {
        fun getPlayerNamePresenter(): ScorePlayerPresenter
        fun getStatsPresenter(): ScoreStatsPresenter
        fun getScoreSummaryPresenter(): ScoreSummaryPresenter
        fun getCardTitle(viewType: Int): String
    }

    interface ScoreSummaryPresenter {
        fun getScoringSummaryPeriodDetailsList(): List<PeriodDetail>
        fun getAwayTeamAbbreviation(): String
        fun getHomeTeamAbbreviation(): String
    }

    interface ScorePlayerPresenter {
        fun getNameCount(viewType: Int): Int
        fun getLongestName(viewType: Int): String
        fun bindView(view: ScorePlayerView, position: Int, viewType: Int)
    }

    interface ScorePlayerView {
        fun bind(name: String)
    }

    interface ScoreStatsPresenter {
        fun getRowCount(viewType: Int): Int
        fun getColumnCount(viewType: Int): Int
        fun bindView(view: ScoreStatsView, position: Int, viewType: Int)
        fun bindHeader(view: ScoreStatsView, position: Int, viewType: Int)
        fun getColumnLongestString(position: Int, viewType: Int): String
    }

    interface ScoreStatsView {
        fun bind(content: String)
        fun bind(value: Float)
        fun bind(value: Int)
    }
}