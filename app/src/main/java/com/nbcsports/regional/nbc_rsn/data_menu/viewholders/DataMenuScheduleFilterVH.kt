package com.nbcsports.regional.nbc_rsn.data_menu.viewholders

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.data_bar.DataBarUtil
import com.nbcsports.regional.nbc_rsn.data_bar.Record
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoSchedule
import com.nbcsports.regional.nbc_rsn.data_menu.schedule.DataMenuScheduleItemView
import kotlinx.android.synthetic.main.view_holder_data_menu_schedule_filter.view.*

class DataMenuScheduleFilterVH(
        root: View,
        private val filterListener: DataMenuContract.ScheduleFilterListener,
        private val statsTeamId: Int
) : DataMenuScheduleItemView(root) {

    init {
        itemView.season_selector.adapter = ArrayAdapter<String>(itemView.context, R.layout.data_menu_spinner_item, filterListener.getFilterOptions())
        itemView.season_selector.setSelection(filterListener.getInitFilterIndex())
        itemView.season_selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterListener.onNewFilterSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // clicking anywhere on filter bar will trigger selector
        itemView.schedule_filter_bar_bg.setOnClickListener {
            itemView.season_selector.performClick()
        }
    }

    override fun bind(event: RotoSchedule, vararg others: Any?) {

        val ties: Int? = if (statsTeamId == event.homeGlobalId) {
            event.homeTies
        } else {
            event.awayTies
        }

        val otLosses: Int? = if (statsTeamId == event.homeGlobalId) {
            event.homeOtLosses
        } else {
            event.awayOtLosses
        }

        val latestRecord = Record(
                wins = if (statsTeamId == event.homeGlobalId) event.homeWins else event.awayWins,
                losses = if (statsTeamId == event.homeGlobalId) event.homeLosses else event.awayLosses,
                ties = if (ties != -1) ties else if (otLosses != -1) otLosses else null
        )
        itemView.record.text = DataBarUtil.getRecordForTeam(latestRecord, false, false, false)
    }
}