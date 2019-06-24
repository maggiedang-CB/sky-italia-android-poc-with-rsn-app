package com.nbcsports.regional.nbc_rsn.data_menu.score

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.BoxEvent
import com.nbcsports.regional.nbc_rsn.data_menu.models.BoxScoreData
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoSchedule
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.DataMenuScheduleVH
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.DataMenuScoreCardVH
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.TabClickListener
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.TabLabelViewHolder
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.*
import kotlin.math.roundToInt

open class DataMenuScoreAdapter<T : BoxScoreData>(
        protected val presenter: DataMenuScoreListPresenter<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), TabClickListener, DataMenuContract.ScoreCardFactory {

    /**
     * This is to make sure we only refresh each card when needed, so notifyDataSetUpdated() won't
     * be called to that card whenever user scrolls to it.
     */
    private var positionsWaitingRefresh: ArrayList<Int> = ArrayList()

    // region Adapter methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DataMenuScoreCommonViewType.MATCH_OVERVIEW.ordinal ->
                DataMenuScheduleVH(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_schedule, parent, false))
            DataMenuScoreCommonViewType.SCORE_SUMMARY.ordinal -> {
                val holder = DataMenuScoreCardVH(
                        LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_score_card, parent, false),
                        this,
                        SCORE_SUMMARY_UNIQUE_VIEW_TYPE
                )
                (holder.itemView.layoutParams as? RecyclerView.LayoutParams)?.apply {
                    setMargins(0, 0, 0, holder.itemView.resources.getDimension(R.dimen.datamenu_score_summary_bottom_margin).roundToInt())
                }
                holder.hideTitle()
                holder
            }
            DataMenuScoreCommonViewType.TEAM_TAB.ordinal -> {
                val holder = TabLabelViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.standings_tab_label, parent, false), this)
                holder.updateLabel(presenter.getAwayTeamName(), presenter.getHomeTeamName())
                holder.update(false)
                holder
            }
            else ->
                if (viewType >= DataMenuScoreCommonViewType.LAST.ordinal + presenter.getCardCount()) {
                    DataMenuScoringSummaryVH(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_scoring_summary, parent, false),
                            this,
                            viewType)
                } else {
                    DataMenuScoreCardVH(
                            LayoutInflater.from(parent.context).inflate(R.layout.view_holder_data_menu_score_card, parent, false),
                            this,
                            getUniqueItemViewType(viewType)
                    )
                }
        }
    }

    override fun getItemCount(): Int {
        val statsCardCount = presenter.getCardCount()
        return if (statsCardCount < 0) {
            0
        } else {
            DataMenuScoreCommonViewType.LAST.ordinal + statsCardCount + getCustomCardCount()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < DataMenuScoreCommonViewType.LAST.ordinal)
            position
        else
            DataMenuScoreCommonViewType.LAST.ordinal + getViewType(position - DataMenuScoreCommonViewType.LAST.ordinal)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataMenuScheduleVH) {
            presenter.eventOverview?.let { holder.bind(it) }
        } else if (holder is DataMenuScoreCardVH && positionsWaitingRefresh.contains(position)) {
            positionsWaitingRefresh.remove(position)
            holder.refresh()
        } else if (holder is DataMenuScoringSummaryVH) {
            holder.bind()
        }
    }
    // endregion

    open fun getCustomCardCount(): Int {
        return 0
    }

    /**
     * In the current implementation, no view holder will be shared, because most of the view holders
     * are smaller RV.
     */
    open fun getViewType(uniqueItemPosition: Int): Int {
        return uniqueItemPosition
    }

    fun update(newData: BoxEvent<T>) {
        presenter.update(newData)
        notifyDataSetChanged()
    }

    fun updateOverview(schedule: Array<RotoSchedule>?) {
        presenter.updateOverview(schedule)
        notifyOverviewChanged()
    }

    // region Interface methods
    override fun getPlayerNamePresenter(): DataMenuContract.ScorePlayerPresenter {
        return presenter
    }

    override fun getStatsPresenter(): DataMenuContract.ScoreStatsPresenter {
        return presenter
    }

    override fun getScoreSummaryPresenter(): DataMenuContract.ScoreSummaryPresenter {
        return presenter
    }

    override fun updateTabs(firstTabSelected: Boolean) {
        presenter.switchTeam(if (firstTabSelected) DataMenuScoreListTabs.AWAY else DataMenuScoreListTabs.HOME)
        notifyUniqueItemChanged()
    }

    override fun getCardTitle(viewType: Int): String {
        return presenter.getCardTitle(viewType)
    }
    // endregion

    /**
     * The return value from this method needs to match the input value of [getViewType] for the
     * same view type.
     * // todo: improved the doc for this method
     */
    private fun getUniqueItemViewType(viewType: Int): Int {
        return viewType - DataMenuScoreCommonViewType.LAST.ordinal
    }

    private fun notifyUniqueItemChanged() {
        positionsWaitingRefresh.addAll(DataMenuScoreCommonViewType.LAST.ordinal until (DataMenuScoreCommonViewType.LAST.ordinal + presenter.getCardCount()))
        notifyItemRangeChanged(DataMenuScoreCommonViewType.LAST.ordinal, presenter.getCardCount())
    }

    private fun notifyOverviewChanged() {
        notifyItemChanged(DataMenuScoreCommonViewType.MATCH_OVERVIEW.ordinal)
    }
}

val SCORE_SUMMARY_UNIQUE_VIEW_TYPE = -1
val SCORE_SUMMARY_ROW_COUNT = 2

enum class DataMenuScoreCommonViewType {
    MATCH_OVERVIEW,
    SCORE_SUMMARY,
    TEAM_TAB,
    LAST
}